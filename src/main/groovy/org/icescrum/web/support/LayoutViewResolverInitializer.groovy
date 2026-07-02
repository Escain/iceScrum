/*
 * Copyright (c) 2026 iceScrum community.
 *
 * This file is part of iceScrum.
 *
 * iceScrum is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * Grails 7 migration helper: the sitemesh ContentProcessor of the (lazy)
 * GrailsLayoutViewResolver is created in its init() method, which is only
 * triggered by a ContextRefreshedEvent the lazy bean can miss — leaving
 * layout rendering to fail with a null ContentProcessor. Force init() once
 * the web application context (with its ServletContext) is ready.
 */
package org.icescrum.web.support

import groovy.util.logging.Slf4j
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.web.context.WebApplicationContext

@Slf4j
class LayoutViewResolverInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    void onApplicationEvent(ContextRefreshedEvent event) {
        def ctx = event.applicationContext
        if (!ctx.containsBean('jspViewResolver')) {
            return
        }
        try {
            def resolver = ctx.getBean('jspViewResolver')
            if (resolver.hasProperty('servletContext') && resolver.servletContext == null && ctx instanceof WebApplicationContext) {
                resolver.servletContext = ((WebApplicationContext) ctx).servletContext
            }
            if (resolver.respondsTo('init')) {
                resolver.init()
            }
        } catch (Exception e) {
            log.warn("Could not initialize layout view resolver: ${e.message}")
        }
    }
}
