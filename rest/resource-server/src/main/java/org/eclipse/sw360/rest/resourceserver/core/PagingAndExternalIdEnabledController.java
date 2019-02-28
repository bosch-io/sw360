package org.eclipse.sw360.rest.resourceserver.core;

import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.rest.resourceserver.core.helper.PagingAwareRestHelper;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.stream.Collectors;

public abstract class PagingAndExternalIdEnabledController<T> extends BasicController<T> {

    protected abstract Set<T> searchByExternalIds(Map<String, Set<String>> externalIds, User user);

    /**
     * Generic Entity response method to get externalIds (projects, components, releases)
     */
    public ResponseEntity searchByExternalIds(MultiValueMap<String, String> externalIdsMultiMap,
                                              PagingAwareRestHelper<T> helper,
                                              User user) throws TException {

        Map<String, Set<String>> externalIds = getExternalIdsFromMultiMap(externalIdsMultiMap);
        Set<T> sw360Objects = searchByExternalIds(externalIds, user);
        List<Resource> resourceList = sw360Objects.stream().map(sw360Object -> {
            T embeddedResource = helper.convertToEmbedded(sw360Object, Collections.singletonList("externalIds"));
            return new Resource<>(embeddedResource);
        })
        .collect(Collectors.toList());

        Resources<Resource> resources = new Resources<>(resourceList);
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    private Map<String, Set<String>> getExternalIdsFromMultiMap(MultiValueMap<String, String> externalIdsMultiMap) {
        Map<String, Set<String>> externalIds = new HashMap<>();
        for (String externalIdKey : externalIdsMultiMap.keySet()) {
            externalIds.put(externalIdKey, new HashSet<>(externalIdsMultiMap.get(externalIdKey)));
        }

        return externalIds;
    }
}
