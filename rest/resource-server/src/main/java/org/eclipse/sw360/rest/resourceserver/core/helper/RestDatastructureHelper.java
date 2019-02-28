package org.eclipse.sw360.rest.resourceserver.core.helper;

import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RestDatastructureHelper {
    private RestDatastructureHelper() {
        // only static methods
    }

    public static Map<String, Set<String>> getExternalIdsFromMultiMap(MultiValueMap<String, String> externalIdsMultiMap) {
        Map<String, Set<String>> externalIds = new HashMap<>();
        for (String externalIdKey : externalIdsMultiMap.keySet()) {
            externalIds.put(externalIdKey, new HashSet<>(externalIdsMultiMap.get(externalIdKey)));
        }

        return externalIds;
    }
}
