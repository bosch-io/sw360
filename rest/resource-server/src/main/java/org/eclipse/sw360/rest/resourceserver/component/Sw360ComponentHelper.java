package org.eclipse.sw360.rest.resourceserver.component;

import lombok.RequiredArgsConstructor;
import org.eclipse.sw360.datahandler.thrift.components.Component;
import org.eclipse.sw360.rest.resourceserver.core.helper.PagingAwareRestHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.hateoas.Link;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@BasePathAwareController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Sw360ComponentHelper extends PagingAwareRestHelper<Component> {
    @Override
    protected Comparator<Component> mkComparatorFromPropertyName(String name) {
        return Comparator.comparing(c -> c.getFieldValue(Component._Fields.findByName(name)).toString());
    }

    @Override
    public Component convertToEmbedded(Component component) {
        Component embeddedComponent = new Component();
        embeddedComponent.setId(component.getId());
        embeddedComponent.setName(component.getName());
        embeddedComponent.setComponentType(component.getComponentType());
        embeddedComponent.setType(null);
        return embeddedComponent;
    }


    private String getThriftKeyFromJSONKey(String jsonKey) {
        final String COMPONENT_VENDOR_KEY_THRIFT = "vendorNames";
        final String COMPONENT_VENDOR_KEY_JSON = "vendors";

        switch (jsonKey) {
            case COMPONENT_VENDOR_KEY_JSON:
                return COMPONENT_VENDOR_KEY_THRIFT;
            default:
                return jsonKey;
        }
    }

    @Override
    public Component convertToEmbedded(Component component, List<String> fields) {
        Component embeddedComponent = this.convertToEmbedded(component);
        if (fields != null) {
            fields.stream()
                    .map(this::getThriftKeyFromJSONKey)
                    .map(Component._Fields::findByName)
                    .filter(Objects::nonNull)
                    .forEach(componentFiled -> embeddedComponent.setFieldValue(componentFiled, component.getFieldValue(componentFiled)));
        }
        return embeddedComponent;
    }

    @Override
    public Link mkSelfLink(Component component) {
        return mkSelfLinkToId(component.getId());
    }

    @Override
    public Link mkSelfLinkToId(String id) {
        return linkTo(ComponentController.class)
                .slash("api" + ComponentController.COMPONENTS_URL + "/" + id).withSelfRel();
    }

    @Override
    protected String getEmbeddedResourceKey() {
        return "sw360:components";
    }

    public Component updateComponent(Component componentToUpdate, Component requestBodyComponent) {
        for(Component._Fields field:Component._Fields.values()) {
            Object fieldValue = requestBodyComponent.getFieldValue(field);
            if(fieldValue != null) {
                componentToUpdate.setFieldValue(field, fieldValue);
            }
        }
        return componentToUpdate;
    }
}
