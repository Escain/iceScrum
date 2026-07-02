/*
 * Copyright (c) 2015 Kagilum SAS
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
 * Vincent Barrier (vbarrier@kagilum.com)
 */

import org.icescrum.core.security.MethodScrumExpressionHandler
import org.icescrum.core.security.WebScrumExpressionHandler
import org.icescrum.core.utils.TimeoutHttpSessionListener
import org.icescrum.i18n.IceScrumMessageSource

// Grails 7 migration notes:
// - task:annotation-driven XML namespace replaced by @EnableAsync/@EnableScheduling
//   on the Application class
// - the dev-mode liquibase migrationResourceAccessor bean was dropped: the
//   database-migration plugin resolves grails-app/migrations itself now
beans = {

    webExpressionHandler(WebScrumExpressionHandler) {
        roleHierarchy = ref('roleHierarchy')
    }

    expressionHandler(MethodScrumExpressionHandler) {
        parameterNameDiscoverer = ref('parameterNameDiscoverer')
        permissionEvaluator = ref('permissionEvaluator')
        roleHierarchy = ref('roleHierarchy')
        trustResolver = ref('authenticationTrustResolver')
    }

    messageSource(IceScrumMessageSource) {
        basenames = "classpath:messages"
    }

    timeoutHttpSessionListener(TimeoutHttpSessionListener) {
        config = grailsApplication.config
    }

}
