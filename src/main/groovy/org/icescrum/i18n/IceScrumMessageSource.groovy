/*
 * Copyright (c) 2014 Kagilum SAS.
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
 *
 * Nicolas Noullet (nnoullet@kagilum.com)
 * Vincent Barrier (vbarrier@kagilum.com)
 *
 */

package org.icescrum.i18n

import org.grails.spring.context.support.PluginAwareResourceBundleMessageSource
import org.grails.spring.context.support.ReloadableResourceBundleMessageSource.PropertiesHolder

class IceScrumMessageSource extends PluginAwareResourceBundleMessageSource {

    // Grails 7: the parent afterPropertiesSet() recomputes basenames by scanning
    // classpath *.properties resources and deriving basenames from their filenames,
    // discarding whatever was configured on the bean. In a packaged fat WAR the scan
    // happens to yield [messages, report], but when the WAR runs exploded (unzipped
    // dir + WarLauncher, as deployed for classloading performance) it only yields
    // [report], so every app message rendered as its raw key (is.ui.*, is.login...).
    // Re-assert the intended basenames after the parent scan so both layouts behave
    // identically. This class owns the basenames; the bean definition must not set them.
    @Override
    void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet()
        setBasenames('messages', 'report')
    }

    Map<String, String> getAllMessages(Locale locale) {
        def propertiesHolders = ([] << getMergedProperties(locale)) << getMergedPluginProperties(locale)
        def messages = [:]
        propertiesHolders.each { PropertiesHolder holder ->
            holder.properties.each { key, val ->
                messages[key] = val
            }
        }
        messages
    }
}