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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.HateoasSortHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class PagingEnabledController<T> extends BasicController<T> {
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

        List<T> pagedObjects = applyPaging(objects, pageable);

        final HateoasSortHandlerMethodArgumentResolver sortResolver = new HateoasSortHandlerMethodArgumentResolver();
        final HateoasPageableHandlerMethodArgumentResolver argumentResolver = new HateoasPageableHandlerMethodArgumentResolver(sortResolver);

        final PagedResourcesAssembler<T> componentPagedResourcesAssembler = new PagedResourcesAssembler<>(argumentResolver,
                ServletUriComponentsBuilder.fromCurrentRequest().build()); // TODO
        componentPagedResourcesAssembler.setForceFirstAndLastRels(true);

        final PaginationResult<T> paginationResult = new PaginationResult<>(pagedObjects, pagedObjects.size(), pageable);
        final PagedResources<Resource<T>> resources = componentPagedResourcesAssembler.toResource(paginationResult,
                new Link(ServletUriComponentsBuilder.fromCurrentRequest().build().toString())); // TODO

        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    protected abstract Comparator<T> mkComparatorFromPropertyName(String name);

    private Comparator<T> mkComparatorFromOrder(Sort.Order order) {
        final Comparator<T> tComparator = mkComparatorFromPropertyName(order.getProperty());
        if(order.isAscending()) {
            return tComparator;
        } else {
            return tComparator.reversed();
        }
    }

    private Comparator<T> mkComparatorFromOrders(Stream<Sort.Order> orderings) {
        return orderings.map(this::mkComparatorFromOrder)
                .reduce((t1,t2) -> 0, Comparator::thenComparing);
    }

    private Comparator<T> mkComparatorFromSort(Sort sort) {
        return mkComparatorFromOrders(StreamSupport.stream(Spliterators.spliteratorUnknownSize(sort.iterator(), Spliterator.ORDERED), false));
    }

    private Stream<T> applySorting(List<T> objects, Sort sort) {
        return objects.stream()
                .sorted(mkComparatorFromSort(sort));
    }

    private List<T> applyPaging(List<T> objects, Pageable pageable) {
        return applySorting(objects, pageable.getSort())
                .skip(pageable.getPageNumber() * pageable.getPageSize())
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
    }

/*    protected Comparator<T> generateComparator(Class<T> type) throws ResourceClassNotFoundException {
        return generateComparator(type, Collections.emptyList());
    }

    protected Comparator<T> generateComparator(Class<T> type, String property) throws ResourceClassNotFoundException {
        return generateComparator(type, Collections.singletonList(property));
    }

    abstract protected Comparator<T> generateComparator(Class<T> type,  List<String> properties) throws ResourceClassNotFoundException;*/

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
    } */

/*
    private PagedResources.PageMetadata createPageMetadata(PaginationResult paginationResult) {
        Pageable pageable = paginationResult.getPageable();
        return new PagedResources.PageMetadata(
                pageable.getPageSize(),
                pageable.getPageNumber(),
                paginationResult.getNumberOfElements(),
                paginationResult.getTotalPages());
    }

    private List<Link> getPaginationLinks(PaginationResult paginationResult, String baseUrl) {
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

    private String createPaginationLink(String baseUrl, Pageable pageable) {
        return createPaginationLink(baseUrl, pageable.getPageNumber(), pageable.getPageSize());
    }

    private String createPaginationLink(String baseUrl, int page, int pageSize) {
        return baseUrl + "?" + PAGINATION_PARAM_PAGE + "=" + page + "&" + PAGINATION_PARAM_PAGE_ENTRIES + "=" + pageSize;
    }

    private String getAPIBaseUrl() throws URISyntaxException {
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return new URI(uri.getScheme(),
                uri.getAuthority(),
                uri.getPath(),
                null,
                uri.getFragment()).toString();
    } */

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
