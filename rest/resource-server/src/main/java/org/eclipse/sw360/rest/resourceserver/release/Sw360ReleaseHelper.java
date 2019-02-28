package org.eclipse.sw360.rest.resourceserver.release;

import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.rest.resourceserver.core.HalResource;
import org.eclipse.sw360.rest.resourceserver.core.helper.PagingAwareRestHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.hateoas.Link;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@BasePathAwareController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Sw360ReleaseHelper extends PagingAwareRestHelper<Release> {
    @Override
    protected Comparator<Release> mkComparatorFromPropertyName(String name) {
        return Comparator.comparing(c -> c.getFieldValue(Release._Fields.findByName(name)).toString());
    }

    @Override
    public Release convertToEmbedded(Release release) {
        Release embeddedRelease = new Release();
        embeddedRelease.setId(release.getId());
        embeddedRelease.setName(release.getName());
        embeddedRelease.setVersion(release.getVersion());
        embeddedRelease.setType(null);
        return embeddedRelease;
    }

    private String getThriftKeyFromJSONKey(String jsonKey) {
        final String RELEASE_CPEID_KEY_THRIFT = "cpeid";
        final String RELEASE_CPEID_KEY_JSON = "cpeId";

        switch (jsonKey) {
            case RELEASE_CPEID_KEY_JSON:
                return RELEASE_CPEID_KEY_THRIFT;
            default:
                return jsonKey;
        }
    }

    @Override
    public Release convertToEmbedded(Release release, List<String> fields) {
        Release embeddedRelease = this.convertToEmbedded(release);
        if (fields != null) {
            fields.stream()
                    .map(this::getThriftKeyFromJSONKey)
                    .map(Release._Fields::findByName)
                    .filter(Objects::nonNull)
                    .forEach(componentFiled -> embeddedRelease.setFieldValue(componentFiled, release.getFieldValue(componentFiled)));
        }
        return embeddedRelease;
    }

    @Override
    public Link mkSelfLink(Release release) {
        return mkSelfLinkToId(release.getId());
    }

    @Override
    public Link mkSelfLinkToId(String id) {
        return linkTo(ReleaseController.class)
                .slash("api" + ReleaseController.RELEASES_URL + "/" + id).withSelfRel();
    }

    @Override
    protected String getEmbeddedResourceKey() {
        return "sw360:releases";
    }

    public void addEmbedded(HalResource halResource, Set<String> releases, Sw360ReleaseService releaseService, User user) throws TException {
        for (String releaseId : releases) {
            final Release release = releaseService.getReleaseForUserById(releaseId, user);
            addEmbedded(halResource, release);
        }
    }

    public Release updateRelease(Release releaseToUpdate, Release requestBodyRelease) {
        for(Release._Fields field:Release._Fields.values()) {
            Object fieldValue = requestBodyRelease.getFieldValue(field);
            if(fieldValue != null) {
                releaseToUpdate.setFieldValue(field, fieldValue);
            }
        }
        return releaseToUpdate;
    }
}
