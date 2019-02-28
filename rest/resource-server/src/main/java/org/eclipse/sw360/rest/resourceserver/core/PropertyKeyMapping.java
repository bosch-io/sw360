/*
 * Copyright Bosch Software Innovations GmbH, 2018.
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

public class PropertyKeyMapping {

    private static final String COMPONENT_VENDOR_KEY_THRIFT = "vendorNames";
    static final String COMPONENT_VENDOR_KEY_JSON = "vendors";

    private static final String RELEASE_CPEID_KEY_THRIFT = "cpeid";
    static final String RELEASE_CPEID_KEY_JSON = "cpeId";

    public static String componentThriftKeyFromJSONKey(String jsonKey) {
        switch (jsonKey) {
            case COMPONENT_VENDOR_KEY_JSON:
                return COMPONENT_VENDOR_KEY_THRIFT;
            default:
                return jsonKey;
        }
    }

    public static String releaseThriftKeyFromJSONKey(String jsonKey) {
        switch (jsonKey) {
            case RELEASE_CPEID_KEY_JSON:
                return RELEASE_CPEID_KEY_THRIFT;
            default:
                return jsonKey;
        }
    }

}
