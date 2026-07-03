/*
 * Copyright (c) 2026 iceScrum community.
 *
 * This file is part of iceScrum.
 *
 * iceScrum is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * Minimal replacement for the dead cache-headers Grails 2 plugin: only the
 * withCacheHeaders { lastModified {} / etag {} / generate {} } DSL and
 * cache(validFor:/shared:) that iceScrum uses.
 */
package org.icescrum.web.support

trait CacheHeadersSupport {

    def withCacheHeaders(Closure dsl) {
        def spec = new CacheHeadersSpec()
        dsl.delegate = spec
        dsl.resolveStrategy = Closure.DELEGATE_FIRST
        dsl.call()

        Date lastModified = spec.lastModifiedClosure ? (Date) spec.lastModifiedClosure.call() : null
        String etagValue = spec.etagClosure ? spec.etagClosure.call()?.toString() : null

        if (etagValue != null) {
            if (request.getHeader('If-None-Match') == etagValue) {
                response.status = 304
                return null
            }
            response.setHeader('ETag', etagValue)
        }
        if (lastModified != null) {
            long ifModifiedSince = request.getDateHeader('If-Modified-Since')
            if (ifModifiedSince != -1 && (lastModified.time / 1000 as long) <= (ifModifiedSince / 1000 as long)) {
                response.status = 304
                return null
            }
            response.setDateHeader('Last-Modified', lastModified.time)
        }
        Closure generate = spec.generateClosure
        generate.delegate = this
        generate.resolveStrategy = Closure.DELEGATE_FIRST
        return generate.call()
    }

    void cache(Map args) {
        def directives = []
        directives << (args.shared ? 'public' : 'private')
        if (args.validFor != null) {
            directives << "max-age=${args.validFor}"
        }
        response.setHeader('Cache-Control', directives.join(', '))
    }

    static class CacheHeadersSpec {
        Closure lastModifiedClosure
        Closure etagClosure
        Closure generateClosure

        void lastModified(Closure c) { lastModifiedClosure = c }

        void etag(Closure c) { etagClosure = c }

        void generate(Closure c) { generateClosure = c }
    }
}
