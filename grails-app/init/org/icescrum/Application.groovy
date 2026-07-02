/*
 * Copyright (c) 2026 iceScrum community.
 *
 * This file is part of iceScrum.
 *
 * iceScrum is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 */
package org.icescrum

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import groovy.transform.CompileStatic
import org.icescrum.atmosphere.IceScrumMeteorServlet
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@EnableAsync
@EnableScheduling
@CompileStatic
class Application extends GrailsAutoConfiguration {

    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    /**
     * Replaces the Grails 2 atmosphere-meteor plugin registration
     * (former grails-app/conf/AtmosphereMeteorConfig.groovy).
     */
    @Bean
    ServletRegistrationBean<IceScrumMeteorServlet> meteorServlet() {
        def registration = new ServletRegistrationBean<IceScrumMeteorServlet>(new IceScrumMeteorServlet(), IceScrumMeteorServlet.MAPPING)
        registration.name = 'MeteorServletDefault'
        registration.loadOnStartup = 0
        registration.asyncSupported = true
        registration.initParameters = [
                'org.atmosphere.cpr.broadcasterCacheClass'                                   : 'org.atmosphere.cache.UUIDBroadcasterCache',
                'org.atmosphere.cpr.AtmosphereFramework.analytics'                           : 'false',
                'org.atmosphere.interceptor.HeartbeatInterceptor.heartbeatFrequencyInSeconds': '30',
                'org.atmosphere.cpr.CometSupport.maxInactiveActivity'                        : (30 * 60000).toString(),
                'org.atmosphere.cpr.broadcasterClass'                                        : 'org.icescrum.atmosphere.IceScrumBroadcaster',
                'org.atmosphere.cpr.Broadcaster.sharedListenersList'                         : 'true',
                'org.atmosphere.cpr.AtmosphereInterceptor'                                   : 'org.atmosphere.client.TrackMessageSizeInterceptor,org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor',
        ]
        return registration
    }
}
