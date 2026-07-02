/*
 * Copyright (c) 2026 iceScrum community.
 *
 * This file is part of iceScrum.
 *
 * iceScrum is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * Minimal replacement for the dead browser-detection Grails 2 plugin's
 * userAgentIdentService, over the eu.bitwalker UserAgentUtils library.
 * Only the methods iceScrum uses are provided.
 */
package org.icescrum.web.services

import eu.bitwalker.useragentutils.Browser
import eu.bitwalker.useragentutils.UserAgent
import org.springframework.web.context.request.RequestContextHolder

class UserAgentIdentService {

    static transactional = false

    boolean isBrowser(Browser browser) {
        Browser current = currentUserAgent()?.browser
        return current ? (current == browser || current.group == browser) : false
    }

    boolean isMobile() {
        return currentUserAgent()?.operatingSystem?.isMobileDevice() ?: false
    }

    private UserAgent currentUserAgent() {
        String header = RequestContextHolder.requestAttributes?.currentRequest?.getHeader('user-agent')
        return header ? UserAgent.parseUserAgentString(header) : null
    }
}
