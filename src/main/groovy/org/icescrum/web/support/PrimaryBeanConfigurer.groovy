/*
 * Copyright (c) 2026 iceScrum community.
 *
 * This file is part of iceScrum.
 *
 * iceScrum is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * Grails 7 migration helper: both the cache plugin (grailsCacheManager) and the
 * spring-security-acl plugin (aclCacheManager) register a CacheManager, which
 * breaks by-type injection. Mark the Grails one primary.
 */
package org.icescrum.web.support

import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory

class PrimaryBeanConfigurer implements BeanFactoryPostProcessor {

    List<String> primaryBeanNames = []

    @Override
    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        primaryBeanNames.each { String name ->
            if (beanFactory.containsBeanDefinition(name)) {
                beanFactory.getBeanDefinition(name).primary = true
            }
        }
    }
}
