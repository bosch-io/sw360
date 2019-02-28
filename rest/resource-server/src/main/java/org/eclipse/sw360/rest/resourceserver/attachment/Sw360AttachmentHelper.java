package org.eclipse.sw360.rest.resourceserver.attachment;

import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.rest.resourceserver.core.helper.RestHelper;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

public class Sw360AttachmentHelper extends RestHelper<Attachment> {
    @Override
    public Attachment convertToEmbedded(Attachment attachment) {
        attachment.setCreatedTeam(null);
        attachment.setCreatedComment(null);
        attachment.setCreatedOn(null);
        attachment.setCreatedBy(null);
        attachment.setCheckedBy(null);
        attachment.setCheckedOn(null);
        attachment.setCheckedTeam(null);
        attachment.setCheckedComment(null);
        attachment.setCheckStatus(null);
        return attachment;
    }

    @Override
    public Attachment convertToEmbedded(Attachment attachment, List<String> fields) {
        return null;
    }

    @Override
    public Link mkSelfLink(Attachment attachment) {
        return mkSelfLinkToId(attachment.getAttachmentContentId());
    }

    @Override
    public Link mkSelfLinkToId(String id) {
        return linkTo(AttachmentController.class)
                .slash("api" + AttachmentController.ATTACHMENTS_URL + "/" + id).withSelfRel();
    }

    @Override
    protected String getEmbeddedResourceKey() {
        return "sw360:attachments";
    }
}
