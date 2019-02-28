/*
 * Copyright Siemens AG, 2017-2018.
 * Copyright Bosch Software Innovations GmbH, 2017.
 * Part of the SW360 Portal Project.
 *
 * SPDX-License-Identifier: EPL-1.0
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.rest.resourceserver.release;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.thrift.RequestStatus;
import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.datahandler.thrift.vendors.Vendor;
import org.eclipse.sw360.rest.resourceserver.attachment.AttachmentInfo;
import org.eclipse.sw360.rest.resourceserver.attachment.Sw360AttachmentHelper;
import org.eclipse.sw360.rest.resourceserver.attachment.Sw360AttachmentService;
import org.eclipse.sw360.rest.resourceserver.component.ComponentController;
import org.eclipse.sw360.rest.resourceserver.core.*;
import org.eclipse.sw360.rest.resourceserver.core.helper.RestControllerHelper;
import org.eclipse.sw360.rest.resourceserver.license.Sw360LicenseHelper;
import org.eclipse.sw360.rest.resourceserver.license.Sw360LicenseService;
import org.eclipse.sw360.rest.resourceserver.user.Sw360UserHelper;
import org.eclipse.sw360.rest.resourceserver.user.Sw360UserService;
import org.eclipse.sw360.rest.resourceserver.vendor.Sw360VendorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@BasePathAwareController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReleaseController extends BasicController<Release> {
    public static final String RELEASES_URL = "/releases";

    @NonNull
    private final Sw360ReleaseService releaseService;
    @NonNull
    private final Sw360ReleaseHelper releaseHelper;

    @NonNull
    private final Sw360AttachmentService attachmentService;
    @NonNull
    private final Sw360AttachmentHelper attachmentHelper;

    @NonNull
    private final Sw360UserService userService;
    @NonNull
    private final Sw360UserHelper userHelper;

    @NonNull
    private final Sw360VendorHelper vendorHelper;

    @NonNull
    private final Sw360LicenseService licenseService;
    @NonNull
    private final Sw360LicenseHelper licenseHelper;

    @NonNull
    private final RestControllerHelper restControllerHelper;

    @RequestMapping(value = RELEASES_URL, method = RequestMethod.GET)
    public ResponseEntity<Resources<Resource<Release>>> getReleasesForUser(
            Pageable pageable,
            @RequestParam(value = "sha1", required = false) String sha1,
            @RequestParam(value = "fields", required = false) List<String> fields,
            HttpServletRequest request)
            throws TException {
        List<Release> releases = getReleasesInternal(sha1, fields);
        return releaseHelper.buildResponse(releases, fields, requestContainsPaging(request) ? pageable : null);
    }

    private List<Release> getReleasesInternal(String sha1) throws TException {
        User sw360User = restControllerHelper.getSw360UserFromAuthentication();
        if (sha1 != null && !sha1.isEmpty()) {
            return Collections.singletonList(searchReleaseBySha1(sha1, sw360User));
        } else {
            return releaseService.getReleasesForUser(sw360User);
        }
    }

    private List<Release> getReleasesInternal(String name, List<String> fields) throws TException {
        return getReleasesInternal(name).stream()
                .map(c -> releaseHelper.convertToEmbedded(c, fields))
                .collect(Collectors.toList());
    }

    private Release searchReleaseBySha1(String sha1, User sw360User) throws TException {
        AttachmentInfo sw360AttachmentInfo = attachmentService.getAttachmentBySha1(sha1);
        return releaseService.getReleaseForUserById(sw360AttachmentInfo.getOwner().getReleaseId(), sw360User);
    }

    @RequestMapping(value = RELEASES_URL + "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Resource> getRelease(
            @PathVariable("id") String id) throws TException {
        User sw360User = restControllerHelper.getSw360UserFromAuthentication();
        Release sw360Release = releaseService.getReleaseForUserById(id, sw360User);
        HalResource halRelease = createHalReleaseResource(sw360Release, true);
        return new ResponseEntity<>(halRelease, HttpStatus.OK);
    }

    @RequestMapping(value = RELEASES_URL + "/searchByExternalIds", method = RequestMethod.GET)
    public ResponseEntity searchByExternalIds(@RequestParam MultiValueMap<String, String> externalIdsMultiMap) throws TException {
        final Set<Release> releases = releaseService.searchByExternalIds(externalIdsMultiMap);
        return releaseHelper.buildResponse(releases, Collections.singletonList("externalIds"));
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @RequestMapping(value = RELEASES_URL + "/{ids}", method = RequestMethod.DELETE)
    public ResponseEntity<List<MultiStatus>> deleteReleases(
            @PathVariable("ids") List<String> idsToDelete) throws TException {
        User user = restControllerHelper.getSw360UserFromAuthentication();
        List<MultiStatus> results = new ArrayList<>();
        for(String id:idsToDelete) {
            RequestStatus requestStatus = releaseService.deleteRelease(id, user);
            if(requestStatus == RequestStatus.SUCCESS) {
                results.add(new MultiStatus(id, HttpStatus.OK));
            } else if(requestStatus == RequestStatus.IN_USE) {
                results.add(new MultiStatus(id, HttpStatus.CONFLICT));
            } else {
                results.add(new MultiStatus(id, HttpStatus.INTERNAL_SERVER_ERROR));
            }
        }
        return new ResponseEntity<>(results, HttpStatus.MULTI_STATUS);
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @RequestMapping(value = RELEASES_URL + "/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<Resource<Release>> patchComponent(
            @PathVariable("id") String id,
            @RequestBody Release updateRelease) throws TException {
        User user = restControllerHelper.getSw360UserFromAuthentication();
        Release sw360Release = releaseService.getReleaseForUserById(id, user);
        sw360Release = releaseHelper.updateRelease(sw360Release, updateRelease);
        releaseService.updateRelease(sw360Release, user);
        HalResource<Release> halRelease = createHalReleaseResource(sw360Release, true);
        return new ResponseEntity<>(halRelease, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @RequestMapping(value = RELEASES_URL, method = RequestMethod.POST)
    public ResponseEntity<Resource<Release>> createRelease(
            @RequestBody Release release) throws URISyntaxException, TException {
        User sw360User = restControllerHelper.getSw360UserFromAuthentication();

        if (release.isSetComponentId()) {
            URI componentURI = new URI(release.getComponentId());
            String path = componentURI.getPath();
            String componentId = path.substring(path.lastIndexOf('/') + 1);
            release.setComponentId(componentId);
        }
        if (release.isSetVendorId()) {
            URI vendorURI = new URI(release.getVendorId());
            String path = vendorURI.getPath();
            String vendorId = path.substring(path.lastIndexOf('/') + 1);
            release.setVendorId(vendorId);
        }

        if (release.getMainLicenseIds() != null) {
            Set<String> mainLicenseIds = new HashSet<>();
            Set<String> mainLicenseUris = release.getMainLicenseIds();
            for (String licenseURIString : mainLicenseUris.toArray(new String[mainLicenseUris.size()])) {
                URI licenseURI = new URI(licenseURIString);
                String path = licenseURI.getPath();
                String licenseId = path.substring(path.lastIndexOf('/') + 1);
                mainLicenseIds.add(licenseId);
            }
            release.setMainLicenseIds(mainLicenseIds);
        }

        Release sw360Release = releaseService.createRelease(release, sw360User);
        HalResource<Release> halResource = createHalReleaseResource(sw360Release, true);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(sw360Release.getId()).toUri();

        return ResponseEntity.created(location).body(halResource);
    }

    @RequestMapping(value = RELEASES_URL + "/{id}/attachments", method = RequestMethod.GET)
    public ResponseEntity<Resources<Resource<Attachment>>> getReleaseAttachments(
            @PathVariable("id") String id) throws TException {
        final User sw360User = restControllerHelper.getSw360UserFromAuthentication();
        final Release sw360Release = releaseService.getReleaseForUserById(id, sw360User);
        return attachmentHelper.buildResponse(sw360Release.getAttachments());
    }

    @RequestMapping(value = RELEASES_URL + "/{releaseId}/attachments", method = RequestMethod.POST, consumes = {"multipart/mixed", "multipart/form-data"})
    public ResponseEntity<HalResource> addAttachmentToRelease(@PathVariable("releaseId") String releaseId,
                                                              @RequestPart("file") MultipartFile file,
                                                              @RequestPart("attachment") Attachment newAttachment) throws TException {
        final User sw360User = restControllerHelper.getSw360UserFromAuthentication();

        Attachment attachment;
        try {
            attachment = attachmentService.uploadAttachment(file, newAttachment, sw360User);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        final Release release = releaseService.getReleaseForUserById(releaseId, sw360User);
        release.addToAttachments(attachment);
        releaseService.updateRelease(release, sw360User);

        final HalResource halRelease = createHalReleaseResource(release, true);

        return new ResponseEntity<>(halRelease, HttpStatus.OK);
    }

    @RequestMapping(value = RELEASES_URL + "/{releaseId}/attachments/{attachmentId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadAttachmentFromRelease(
            @PathVariable("releaseId") String releaseId,
            @PathVariable("attachmentId") String attachmentId,
            HttpServletResponse response) throws TException {
        User sw360User = restControllerHelper.getSw360UserFromAuthentication();
        Release release = releaseService.getReleaseForUserById(releaseId, sw360User);
        attachmentService.downloadAttachmentWithContext(release, attachmentId, response, sw360User);
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(linkTo(ReleaseController.class).slash("api" + RELEASES_URL).withRel("releases"));
        return resource;
    }

    private HalResource<Release> createHalReleaseResource(Release release, boolean verbose) {
        HalResource<Release> halRelease = new HalResource<>(release);
        Link componentLink = linkTo(ReleaseController.class)
                .slash("api" + ComponentController.COMPONENTS_URL + "/" + release.getComponentId()).withRel("component");
        halRelease.add(componentLink);
        release.setComponentId(null);

        if (verbose) {
            if (release.getModerators() != null) {
                Set<String> moderators = release.getModerators();
                userHelper.addEmbeddedModerators(halRelease, moderators, userService);
                release.setModerators(null);
            }
            if (release.getAttachments() != null) {
                Set<Attachment> attachments = release.getAttachments();
                attachmentHelper.addEmbedded(halRelease, attachments);
                release.setAttachments(null);
            }
            if (release.getVendor() != null) {
                Vendor vendor = release.getVendor();
                vendorHelper.addEmbedded(halRelease, vendor);
                release.setVendor(null);
            }
            if (release.getMainLicenseIds() != null) {
                licenseHelper.addEmbedded(halRelease, release.getMainLicenseIds(), licenseService);
                release.setMainLicenseIds(null);
            }
        }
        return halRelease;
    }
}
