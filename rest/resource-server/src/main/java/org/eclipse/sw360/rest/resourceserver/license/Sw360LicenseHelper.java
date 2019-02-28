package org.eclipse.sw360.rest.resourceserver.license;

import lombok.extern.log4j.Log4j;
import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.thrift.licenses.License;
import org.eclipse.sw360.rest.resourceserver.core.HalResource;
import org.eclipse.sw360.rest.resourceserver.core.helper.RestHelper;
import org.springframework.hateoas.Link;

import java.util.List;
import java.util.Set;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Log4j
public class Sw360LicenseHelper extends RestHelper<License> {
    @Override
    public License convertToEmbedded(License license) {
        License embeddedLicense = new License();
        embeddedLicense.setId(license.getId());
        embeddedLicense.setFullname(license.getFullname());
        embeddedLicense.setType(null);
        return embeddedLicense;
    }

    public License convertToEmbedded(String licenseId) {
        License embeddedLicense = new License();
        embeddedLicense.setId(licenseId);
        embeddedLicense.setType(null);
        return embeddedLicense;
    }

    @Override
    public License convertToEmbedded(License license, List<String> fields) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Link mkSelfLink(License license) {
        return mkSelfLinkToId(license.getId());
    }

    @Override
    public Link mkSelfLinkToId(String id) {
        return linkTo(LicenseController.class)
                .slash("api" + LicenseController.LICENSES_URL + "/" + id).withSelfRel();
    }

    @Override
    protected String getEmbeddedResourceKey() {
        return "sw360:licenses";
    }

    public void addEmbedded(HalResource halResource, Set<String> licenseIds, Sw360LicenseService licenseService) {
        for (String licenseId : licenseIds) {
            addEmbedded(halResource, licenseId, licenseService);
        }
    }

    public void addEmbedded(HalResource halResource, String licenseId, Sw360LicenseService licenseService) {
        License licenseById;
        try {
            licenseById = licenseService.getLicenseById(licenseId);
        } catch (TException e) {
            log.error("cannot create self link for license with id: " + licenseId);
            licenseById = convertToEmbedded(licenseId);
        }
        addEmbedded(halResource, licenseById);
    }
}
