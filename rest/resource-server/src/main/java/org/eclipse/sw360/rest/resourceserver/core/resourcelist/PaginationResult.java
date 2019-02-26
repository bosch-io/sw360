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

package org.eclipse.sw360.rest.resourceserver.core.resourcelist;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class PaginationResult<T> extends PageImpl<T> {

    private final Boolean pagingActive;
    private final Pageable pageable;

    public PaginationResult(List<T> resources) {
        super(resources, new PageRequest(0, resources.size()), resources.size());
        pagingActive = false;
        this.pageable = null;
    }

    public PaginationResult(List<T> resources, int totalCount, Pageable pageable) {
        super(resources, pageable, totalCount);
        this.pagingActive = true;
        this.pageable = pageable;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public boolean isPagingActive() {
        return pagingActive;
    }
}
