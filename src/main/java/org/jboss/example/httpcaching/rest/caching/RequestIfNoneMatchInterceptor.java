/**
 * Copyright (c) 2014 Xavier Coulon and contributors (see git log)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.jboss.example.httpcaching.rest.caching;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ContainerRequestFilter} that will look for a {@code If-None-Match} header in the incoming request headers
 * and abort the request and return a {@code 304} response  
 * 
 * @author Xavier Coulon
 *
 */
@Provider
@PreMatching
@RequestIfNoneMatchBinding
public class RequestIfNoneMatchInterceptor implements ContainerRequestFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestIfNoneMatchInterceptor.class);

	public static final String IF_NONE_MATCH = "If-None-Match";

	@Inject
	private CacheService cacheService;

	@Override
	public void filter(final ContainerRequestContext requestContext) throws IOException {
		final URI requestUri = requestContext.getUriInfo().getRequestUri();
		final String etag = cacheService.getETag(requestUri);
		requestContext.getHeaders();
		final String ifNoneMatchHeader = requestContext.getHeaderString(IF_NONE_MATCH);
		// nothing changed: abort with 304 NOT MODIFIED response
		if (ifNoneMatchHeader != null && ifNoneMatchHeader.equals(etag)) {
			LOGGER.debug("Aborting request since If-None-Match / ETag values match for {} {} ",
					requestContext.getMethod(), requestUri.toASCIIString());
			requestContext.abortWith(Response.status(Status.NOT_MODIFIED).build());
		}
	}

}
