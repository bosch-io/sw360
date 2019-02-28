package org.eclipse.sw360.rest.resourceserver.core.helper;

import org.eclipse.sw360.rest.resourceserver.core.HalResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class RestHelper<T> {

    public abstract T convertToEmbedded(T t);

    /*
     * Law:
     *   forall t:
     *     convertToEmbedded(t, Collections.emptyList()) == convertToEmbedded(t)
     */
    public abstract T convertToEmbedded(T t, List<String> fields);

    public List<T> convertToEmbedded(Collection<T> ts, List<String> fields){
        return ts.stream()
                .map(t -> convertToEmbedded(t, fields))
                .collect(Collectors.toList());
    }

    public abstract Link mkSelfLink(T t);

    public abstract Link mkSelfLinkToId(String id);

    // TODO: extract Id from self link

    protected abstract String getEmbeddedResourceKey();

    public void addEmbedded(HalResource halResource, T t) {
        addEmbedded(halResource, t, getEmbeddedResourceKey());
    }

    public void addEmbedded(HalResource halResource, T t, String relation) {
        T embeddedT = convertToEmbedded(t);
        HalResource<T> halComponent = new HalResource<>(embeddedT);
        halComponent.add(mkSelfLink(t));
        halResource.addEmbeddedResource(relation, halComponent);
    }

    public void addEmbedded(HalResource halResource, Collection<T> ts) {
        addEmbedded(halResource, ts, getEmbeddedResourceKey());
    }

    public void addEmbedded(HalResource halResource, Collection<T> ts, String relation) {
        ts.forEach(t -> addEmbedded(halResource, t, relation));
    }

    public Resources<Resource<T>> convertToResourcesOfEmbeddeds(Collection<T> ts) {
        return convertToResourcesOfEmbeddeds(ts, Collections.emptyList());
    }

    public Resources<Resource<T>> convertToResourcesOfEmbeddeds(Collection<T> ts, List<String> fields) {
        return new Resources<>(ts.stream()
                .map(t -> this.convertToEmbedded(t, fields))
                .map(r -> new Resource<>(r))
                .collect(Collectors.toList()));
    }

    public ResponseEntity<Resources<Resource<T>>> buildResponse(Collection<T> ts) {
        return new ResponseEntity<>(convertToResourcesOfEmbeddeds(ts), HttpStatus.OK);
    }

    public ResponseEntity<Resources<Resource<T>>> buildResponse(Collection<T> ts, List<String> fields) {
        return new ResponseEntity<>(convertToResourcesOfEmbeddeds(ts, fields), HttpStatus.OK);
    }
}
