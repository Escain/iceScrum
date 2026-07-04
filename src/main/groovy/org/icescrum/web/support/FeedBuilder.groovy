/*
 * Copyright (c) 2026 Kagilum SAS.
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
 */

package org.icescrum.web.support

import com.rometools.rome.feed.synd.SyndContentImpl
import com.rometools.rome.feed.synd.SyndEntryImpl
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.feed.synd.SyndFeedImpl

/**
 * Minimal replacement for the dead Grails 2 "feeds" plugin FeedBuilder (feedsplugin.FeedBuilder),
 * implemented on top of rome (com.rometools). It supports exactly the DSL used by iceScrum:
 *
 *   def builder = new FeedBuilder()
 *   builder.feed(description: '...', title: '...', link: '...') {
 *       entry('title') { e ->
 *           e.link = '...'
 *           e.publishedDate = date
 *       }
 *   }
 *   SyndFeed feed = builder.makeFeed(FeedBuilder.TYPE_RSS, FeedBuilder.DEFAULT_VERSIONS[FeedBuilder.TYPE_RSS])
 */
class FeedBuilder {

    static final String TYPE_RSS = 'rss'
    static final String TYPE_ATOM = 'atom'
    static final Map<String, String> DEFAULT_VERSIONS = [(TYPE_RSS): 'rss_2.0', (TYPE_ATOM): 'atom_1.0'].asImmutable()

    private Map feedAttributes = [:]
    private List<Expando> feedEntries = []

    def feed(Map attributes, Closure body) {
        feedAttributes = attributes ?: [:]
        feedEntries = []
        body.delegate = this
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.call()
        return this
    }

    def entry(String title, Closure body) {
        def e = new Expando()
        e.title = title
        def result = body.call(e)
        if ((result instanceof CharSequence) && !e.getProperty('content')) {
            e.content = result.toString()
        }
        feedEntries << e
        return e
    }

    SyndFeed makeFeed(String type, String version = null) {
        def feed = new SyndFeedImpl()
        feed.feedType = version ?: DEFAULT_VERSIONS[type]
        feed.title = feedAttributes.title?.toString() ?: ''
        feed.link = feedAttributes.link?.toString() ?: ''
        feed.description = feedAttributes.description?.toString() ?: ''
        feed.entries = feedEntries.collect { e ->
            def entry = new SyndEntryImpl()
            entry.title = e.getProperty('title')?.toString() ?: ''
            if (e.getProperty('link')) {
                entry.link = e.getProperty('link').toString()
            }
            if (e.getProperty('publishedDate')) {
                entry.publishedDate = e.getProperty('publishedDate')
            }
            if (e.getProperty('content')) {
                def content = new SyndContentImpl()
                content.type = 'text/html'
                content.value = e.getProperty('content').toString()
                entry.description = content
            }
            return entry
        }
        return feed
    }
}
