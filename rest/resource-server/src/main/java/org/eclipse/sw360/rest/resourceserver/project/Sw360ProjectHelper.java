package org.eclipse.sw360.rest.resourceserver.project;

import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.thrift.projects.Project;
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
public class Sw360ProjectHelper extends PagingAwareRestHelper<Project> {
    @Override
    protected Comparator<Project> mkComparatorFromPropertyName(String name) {
        return Comparator.comparing(c -> c.getFieldValue(Project._Fields.findByName(name)).toString());
    }

    @Override
    public Project convertToEmbedded(Project project) {
        Project embeddedProject = new Project(project.getName());
        embeddedProject.setId(project.getId());
        embeddedProject.setProjectType(project.getProjectType());
        embeddedProject.setVersion(project.getVersion());
        embeddedProject.setType(null);
        return embeddedProject;
    }

    private String getThriftKeyFromJSONKey(String s) {
        return s;
    }

    @Override
    public Project convertToEmbedded(Project project, List<String> fields) {
        Project embeddedProject = this.convertToEmbedded(project);
        if (fields != null) {
            fields.stream()
                    .map(this::getThriftKeyFromJSONKey)
                    .map(Project._Fields::findByName)
                    .filter(Objects::nonNull)
                    .forEach(componentFiled -> embeddedProject.setFieldValue(componentFiled, project.getFieldValue(componentFiled)));
        }
        return embeddedProject;
    }

    @Override
    public Link mkSelfLink(Project project) {
        return mkSelfLinkToId(project.getId());
    }

    @Override
    public Link mkSelfLinkToId(String id) {
        return linkTo(ProjectController.class)
                .slash("api" + ProjectController.PROJECTS_URL + "/" + id).withSelfRel();
    }

    @Override
    protected String getEmbeddedResourceKey() {
        return "sw360:projects";
    }


    public void addEmbedded(HalResource halResource, Set<String> projectIds, Sw360ProjectService sw360ProjectService, User user) throws TException {
        for (String projectId : projectIds) {
            final Project project = sw360ProjectService.getProjectForUserById(projectId, user);
            addEmbedded(halResource, project);
        }
    }

}
