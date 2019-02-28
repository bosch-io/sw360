/*
 * Copyright Bosch Software Innovations GmbH, 2019.
 * Part of the SW360 Portal Project.
 *
 * SPDX-License-Identifier: EPL-1.0
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.rest.resourceserver.core;

import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.*;

import javax.servlet.http.HttpServletRequest;

public abstract class BasicController<T> implements ResourceProcessor<RepositoryLinksResource> {
    private static final String PAGINATION_PARAM_PAGE = "page";
    private static final String PAGINATION_PARAM_PAGE_ENTRIES = "page_entries";

    protected boolean requestContainsPaging(HttpServletRequest request) {
        return request.getParameterMap().containsKey(PAGINATION_PARAM_PAGE) ||
                request.getParameterMap().containsKey(PAGINATION_PARAM_PAGE_ENTRIES);
    }
}
