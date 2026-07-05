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
 * Nicolas Noullet (nnoullet@kagilum.com)
 *
 */

package org.icescrum.codecs

import org.icescrum.core.domain.Project

class ProjectKeyCodec {

    static final numeric = /[0-9]*/

    static decode = { theTarget ->
        // A request param can arrive as a String[] (params get duplicated/arrayified when the error layout
        // re-includes the URL, e.g. a logged-out hit on /p/KEY/); collapse to a single value so the criteria
        // query binds a String, not an array (which throws ClassCastException in Hibernate's string binder).
        if (theTarget instanceof Object[] || theTarget instanceof Collection) {
            theTarget = theTarget ? theTarget[0] : null
        }
        if (!theTarget || theTarget instanceof Project || theTarget instanceof Map || theTarget ==~ numeric) {
            return theTarget
        }
        Project.createCriteria().get {
            eq 'pkey', theTarget
            projections {
                property 'id'
            }
            cache true
        }
    }
}
