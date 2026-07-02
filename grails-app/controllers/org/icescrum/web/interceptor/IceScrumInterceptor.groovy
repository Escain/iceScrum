/*
 * Copyright (c) 2013/2014 Kagilum SAS.
 *
 * This file is part of iceScrum.
 *
 * iceScrum is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * iceScrum is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with iceScrum.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Authors:
 * Vincent Barrier (vbarrier@kagilum.com)
 *
 * Grails 7 migration: replaces grails-app/conf/IceScrumFilters.groovy (the
 * filters mechanism was removed after Grails 2). The legacy Internet Explorer
 * handling (browser-detection plugin) was dropped.
 */
package org.icescrum.web.interceptor

import grails.plugin.springsecurity.SpringSecurityUtils
import org.grails.web.converters.exceptions.ConverterException
import org.grails.web.json.JSONException
import org.icescrum.core.domain.User
import org.icescrum.core.domain.security.Authority
import org.icescrum.core.support.ApplicationSupport
import org.icescrum.core.support.ProfilingSupport
import org.springframework.web.servlet.support.RequestContextUtils

class IceScrumInterceptor {

    def pushService
    def securityService
    def springSecurityService

    int order = HIGHEST_PRECEDENCE + 50

    IceScrumInterceptor() {
        matchAll()
    }

    boolean before() {
        // -- all --
        pushService.bufferPushForThisThread()
        if (grailsApplication.config.icescrum.profiling.enable && (params.profiler || request.getHeader('x-icescrum-profiler'))) {
            def ajax = request.getHeader('x-icescrum-profiler') ? true : false
            ProfilingSupport.enableProfiling(ajax, controllerName, actionName)
        }
        // -- permissions --
        def keysOk = securityService.decodeKeys(params)
        if (!keysOk) {
            forward(controller: 'errors', action: 'error404')
            return false
        }
        if (controllerName != 'errors') { // Avoid filtering request if error to avoid nasty loop
            securityService.filterRequest()
        }
        // -- feature toggles --
        if (controllerName == 'project' && actionName in ['save', 'add', 'createSample']) {
            if (!ApplicationSupport.booleanValue(grailsApplication.config.icescrum.project.creation.enable) && !SpringSecurityUtils.ifAnyGranted(Authority.ROLE_ADMIN)) {
                forward(controller: "errors", action: "error403")
                return false
            }
        }
        if (controllerName == 'project' && actionName == 'import') {
            if (!ApplicationSupport.booleanValue(grailsApplication.config.icescrum.project.import.enable) && !SpringSecurityUtils.ifAnyGranted(Authority.ROLE_ADMIN)) {
                render(status: 403)
                return false
            }
        }
        if (controllerName == 'project' && actionName == 'export') {
            if (!ApplicationSupport.booleanValue(grailsApplication.config.icescrum.project.export.enable) && !SpringSecurityUtils.ifAnyGranted(Authority.ROLE_ADMIN)) {
                forward(controller: "errors", action: "error403")
                return false
            }
        }
        if (controllerName == 'user' && actionName == 'register') {
            if (!ApplicationSupport.booleanValue(grailsApplication.config.icescrum.registration.enable)) {
                forward(controller: "errors", action: "error403")
                return false
            }
        }
        if (controllerName == 'user' && actionName == 'save') {
            if (!ApplicationSupport.booleanValue(grailsApplication.config.icescrum.registration.enable) && !SpringSecurityUtils.ifAnyGranted(Authority.ROLE_ADMIN)) {
                forward(controller: "errors", action: "error403")
                return false
            }
        }
        if (controllerName == 'user' && actionName == 'retrieve') {
            if (!ApplicationSupport.booleanValue(grailsApplication.config.icescrum.login.retrieve.enable)) {
                forward(controller: "errors", action: "error403")
                return false
            }
        }
        if (controllerName == 'attachmentable' && actionName == 'download') {
            redirect(controller: "errors", action: "error403")
            return false
        }
        // -- webservices (/ws/**) & locale --
        boolean webservice = request.requestURI.contains('/ws/')
        if (webservice) {
            boolean ok = true
            request.withFormat {
                json {
                    try {
                        def data = request.JSON
                        data.remove('project') // Project cannot be provided in body, it must be provided in URL
                        params << data
                        request.restAPI = true
                    } catch (ConverterException e) {
                        if (e.cause instanceof JSONException) {
                            render(status: 400, text: e.cause.message)
                            ok = false
                        } else {
                            throw e
                        }
                    }
                }
            }
            if (!ok) {
                return false
            }
        } else {
            try {
                Locale locale
                if (params.lang) {
                    locale = new Locale(params.lang)
                } else if (springSecurityService.isLoggedIn()) {
                    locale = User.getLocale(springSecurityService.principal.id) // May be executed for every incoming request, so it is optimized and cached
                } else {
                    def acceptLanguage = request.getHeader("accept-language")?.split(",")
                    if (acceptLanguage) {
                        locale = new Locale(*acceptLanguage[0].split('-', 3))
                    }
                }
                if (locale) {
                    RequestContextUtils.getLocaleResolver(request).setLocale(request, response, locale) // Stored in Session because LocaleResolver is a SessionLocaleResolver
                }
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
        return true
    }

    boolean after() {
        pushService.resumePushForThisThread()
        if (grailsApplication.config.icescrum.profiling.enable) {
            ProfilingSupport.reportProfiling()
        }
        return true
    }
}
