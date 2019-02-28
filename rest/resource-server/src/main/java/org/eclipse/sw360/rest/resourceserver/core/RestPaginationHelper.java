/*
 * Copyright Siemens AG, 2017-2018.
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

import org.apache.thrift.TBase;
import org.apache.thrift.TFieldIdEnum;
import org.eclipse.sw360.rest.resourceserver.core.resourcelist.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class RestPaginationHelper {

    private static final String PAGINATION_KEY_FIRST = "first";
    private static final String PAGINATION_KEY_PREVIOUS = "previous";
    private static final String PAGINATION_KEY_NEXT = "next";
    private static final String PAGINATION_KEY_LAST = "last";
    private static final String PAGINATION_PARAM_PAGE = "page";
    public static final String PAGINATION_PARAM_PAGE_ENTRIES = "page_entries";

    private RestPaginationHelper() {
        // only static methods
    }

/*    public static <T extends TBase<?, ? extends TFieldIdEnum>> Resources<Resource<T>> generatePagesResource(PaginationResult<T> paginationResult,
                                                                                                            List<Resource<T>> resources)
            throws URISyntaxException {
        if (paginationResult.isPagingActive()) {
            PagedResources.PageMetadata pageMetadata = createPageMetadata(paginationResult);
            List<Link> pagingLinks = getPaginationLinks(paginationResult, getAPIBaseUrl());
            return new PagedResources<>(resources, pageMetadata, pagingLinks);
        } else {
            return new Resources<>(resources);
        }
    }

    private static PagedResources.PageMetadata createPageMetadata(PaginationResult paginationResult) {
        Pageable pageable = paginationResult.getPageable();
        return new PagedResources.PageMetadata(
                pageable.getPageSize(),
                pageable.getPageNumber(),
                paginationResult.getNumberOfElements(),
                paginationResult.getTotalPages());
    }

    private static List<Link> getPaginationLinks(PaginationResult paginationResult, String baseUrl) {
        Pageable pageable = paginationResult.getPageable();
        List<Link> paginationLinks = new ArrayList<>();

        paginationLinks.add(new Link(createPaginationLink(baseUrl, pageable.first()),PAGINATION_KEY_FIRST));
        if(pageable.hasPrevious()) {
            paginationLinks.add(new Link(createPaginationLink(baseUrl, pageable.previousOrFirst()),PAGINATION_KEY_PREVIOUS));
        }
        if(pageable.getPageNumber() >= paginationResult.getTotalPages()) {
            paginationLinks.add(new Link(createPaginationLink(baseUrl, pageable.next()),PAGINATION_KEY_NEXT));
        }
        paginationLinks.add(new Link(createPaginationLink(baseUrl, paginationResult.getTotalPages() - 1, pageable.getPageSize()),PAGINATION_KEY_LAST));

        return paginationLinks;
    }

    private static String createPaginationLink(String baseUrl, Pageable pageable) {
        return createPaginationLink(baseUrl, pageable.getPageNumber(), pageable.getPageSize());
    }

    private static String createPaginationLink(String baseUrl, int page, int pageSize) {
        return baseUrl + "?" + PAGINATION_PARAM_PAGE + "=" + page + "&" + PAGINATION_PARAM_PAGE_ENTRIES + "=" + pageSize;
    }

    private static String getAPIBaseUrl() throws URISyntaxException {
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return new URI(uri.getScheme(),
                uri.getAuthority(),
                uri.getPath(),
                null,
                uri.getFragment()).toString();
    }*/

/*    public static <T extends TBase<?, ? extends TFieldIdEnum>> Comparator<T> comparatorFromPageable(Pageable pageable, Class<T> resourceClass) throws ResourceClassNotFoundException {
        final Optional<Sort.Order> order = firstOrderFromPageable(pageable);
        if(! order.isPresent()) {
            return ResourceComparatorGenerator.generateComparator(resourceClass);
        }
        Comparator<T> comparator = ResourceComparatorGenerator.generateComparator(resourceClass, order.get().getProperty());
        if(order.get().isDescending()) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    private static Optional<Sort.Order> firstOrderFromPageable(Pageable pageable) {
        return Optional.ofNullable(pageable.getSort())
                .map(Sort::iterator)
                .filter(Iterator::hasNext)
                .map(Iterator::next);
    }*/
}