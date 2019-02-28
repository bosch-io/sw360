package org.eclipse.sw360.rest.resourceserver.vendor;

import org.eclipse.sw360.datahandler.thrift.vendors.Vendor;
import org.eclipse.sw360.rest.resourceserver.core.HalResource;
import org.eclipse.sw360.rest.resourceserver.core.helper.RestHelper;
import org.springframework.hateoas.Link;

import java.util.List;
import java.util.Set;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

public class Sw360VendorHelper extends RestHelper<Vendor> {
    @Override
    public Vendor convertToEmbedded(Vendor vendor) {
        Vendor embeddedVendor = convertToEmbedded(vendor.getFullname());
        embeddedVendor.setId(vendor.getId());
        return embeddedVendor;
    }

    public Vendor convertToEmbedded(String fullName) {
        Vendor embeddedVendor = new Vendor();
        embeddedVendor.setFullname(fullName);
        embeddedVendor.setType(null);
        return embeddedVendor;
    }

    @Override
    public Vendor convertToEmbedded(Vendor vendor, List<String> fields) {
        return vendor; // default to returning to much
    }

    @Override
    public Link mkSelfLink(Vendor vendor) {
        return mkSelfLinkToId(vendor.getId());
    }

    @Override
    public Link mkSelfLinkToId(String id) {
        return linkTo(VendorController.class)
                .slash("api" + VendorController.VENDORS_URL + "/" + id).withSelfRel();
    }

    @Override
    protected String getEmbeddedResourceKey() {
        return "sw360:vendors";
    }

    public void addEmbedded(HalResource halResource, String vendorFullName) {
        addEmbedded(halResource, convertToEmbedded(vendorFullName));
    }

    public void addEmbedded(HalResource halResource, Set<String> vendors) {
        vendors.forEach(vendor -> addEmbedded(halResource, vendor));
    }
}
