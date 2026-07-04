%{--
- Copyright (c) Kagilum SAS.
-
- This file is part of iceScrum.
-
- iceScrum is free software: you can redistribute it and/or modify
- it under the terms of the GNU Affero General Public License as published by
- the Free Software Foundation, either version 3 of the License.
-
- iceScrum is distributed in the hope that it will be useful,
- but WITHOUT ANY WARRANTY; without even the implied warranty of
- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
- GNU General Public License for more details.
-
- You should have received a copy of the GNU Affero General Public License
- along with iceScrum.  If not, see <http://www.gnu.org/licenses/>.
-
- Authors:
-
- Vincent Barrier (vbarrier@kagilum.com)
--}%
<head>
    <meta name='layout' content='main'/>
    <g:if test="${project}">
        %{-- Grails 7 migration: the feeds plugin (feed:meta) was not ported. Emit the RSS autodiscovery <link> directly.
             Must stay a void <link> element: an unclosed non-void tag here forces the parser out of <head> early and
             wraps the whole body, collapsing the flex layout height chain. --}%
        <link rel="alternate" type="application/rss+xml" title="${project.name.encodeAsHTML()}"
              href="${createLink(controller: 'project', action: 'feed', params: [project: project.pkey.encodeAsHTML(), lang: lang])}"/>
        <title>${project.name.encodeAsHTML()}</title>
    </g:if>
    <g:elseif test="${portfolio}">
        <title>${message(code: 'is.portfolio')} - ${portfolio.name.encodeAsHTML()}</title>
    </g:elseif>
</head>
<body>
</body>
