package org.eclipse.sw360.rest.resourceserver.core.helper;

import org.eclipse.sw360.datahandler.thrift.components.Component;
import org.eclipse.sw360.rest.resourceserver.core.resourcelist.PaginationResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.HateoasSortHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class PagingAwareRestHelper<T> extends RestHelper<T> {
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

    public List<T> applyPaging(List<T> objects, Pageable pageable) {
        return applySorting(objects, pageable.getSort())
                .skip(pageable.getPageNumber() * pageable.getPageSize())
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
    }

    private static final String PAGINATION_PARAM_PAGE = "page";
    private static final String PAGINATION_PARAM_PAGE_ENTRIES = "page_entries";

    protected boolean requestContainsPaging(HttpServletRequest request) {
        return request.getParameterMap().containsKey(PAGINATION_PARAM_PAGE) ||
                request.getParameterMap().containsKey(PAGINATION_PARAM_PAGE_ENTRIES);
    }

    protected Resources<Resource<T>> convertToResourcesOfEmbeddeds(List<T> objects, Pageable pageable) {
        return convertToResourcesOfEmbeddeds(objects, Collections.emptyList(), pageable);
    }

    protected Resources<Resource<T>> convertToResourcesOfEmbeddeds(List<T> objects, List<String> fields, Pageable pageable) {
        if (pageable == null) {
            return convertToResourcesOfEmbeddeds(objects, fields);
        }

        final UriComponents uri = ServletUriComponentsBuilder.fromCurrentRequest().build(); // TODO

        List<T> pagedObjects = applyPaging(objects, pageable);
        List<T> embeddedObjects = convertToEmbedded(pagedObjects, fields);

        final HateoasSortHandlerMethodArgumentResolver sortResolver = new HateoasSortHandlerMethodArgumentResolver();
        final HateoasPageableHandlerMethodArgumentResolver argumentResolver = new HateoasPageableHandlerMethodArgumentResolver(sortResolver);

        final PagedResourcesAssembler<T> componentPagedResourcesAssembler = new PagedResourcesAssembler<>(argumentResolver, uri);
        componentPagedResourcesAssembler.setForceFirstAndLastRels(true);

        final PaginationResult<T> paginationResult = new PaginationResult<>(embeddedObjects, embeddedObjects.size(), pageable);
        return componentPagedResourcesAssembler.toResource(paginationResult, new Link(uri.toString()));
    }

    public ResponseEntity<Resources<Resource<T>>> buildResponse(List<T> ts, List<String> fields, Pageable pageable){
        return new ResponseEntity<>(convertToResourcesOfEmbeddeds(ts, fields, pageable), HttpStatus.OK);
    }
}
