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

import org.eclipse.sw360.rest.resourceserver.core.resourcelist.PaginationResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.HateoasSortHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public abstract class PagingEnabledController<T, S extends ResourceSupport> extends BasicController<T,S> {
    private static final String PAGINATION_PARAM_PAGE = "page";
    private static final String PAGINATION_PARAM_PAGE_ENTRIES = "page_entries";

    protected boolean requestContainsPaging(HttpServletRequest request) {
        return request.getParameterMap().containsKey(PAGINATION_PARAM_PAGE) ||
                request.getParameterMap().containsKey(PAGINATION_PARAM_PAGE_ENTRIES);
    }

    protected ResponseEntity<Resources<Resource<T>>> mkResponse(List<T> objects, Pageable pageable) {
        if (pageable == null) {
            return mkResponse(objects);
        }

        final HateoasSortHandlerMethodArgumentResolver sortResolver = new HateoasSortHandlerMethodArgumentResolver();
        final HateoasPageableHandlerMethodArgumentResolver argumentResolver = new HateoasPageableHandlerMethodArgumentResolver(sortResolver);

        final PagedResourcesAssembler<T> componentPagedResourcesAssembler = new PagedResourcesAssembler<>(argumentResolver,
                ServletUriComponentsBuilder.fromCurrentRequest().build());

        final PaginationResult<T> paginationResult = new PaginationResult<>(objects, objects.size(), pageable);
        final PagedResources<Resource<T>> resources = componentPagedResourcesAssembler.toResource(paginationResult);

        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

/*    protected Comparator<T> generateComparator(Class<T> type) throws ResourceClassNotFoundException {
        return generateComparator(type, Collections.emptyList());
    }

    protected Comparator<T> generateComparator(Class<T> type, String property) throws ResourceClassNotFoundException {
        return generateComparator(type, Collections.singletonList(property));
    }

    abstract protected Comparator<T> generateComparator(Class<T> type,  List<String> properties) throws ResourceClassNotFoundException;*/
}
