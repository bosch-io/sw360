package org.eclipse.sw360.rest.resourceserver.user;

import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.rest.resourceserver.core.HalResource;
import org.eclipse.sw360.rest.resourceserver.core.helper.RestHelper;
import org.springframework.hateoas.Link;

import java.util.Collection;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

public class Sw360UserHelper extends RestHelper<User> {
    @Override
    public User convertToEmbedded(User user) {
        User embeddedUser = new User();
        embeddedUser.setId(user.getId());
        embeddedUser.setEmail(user.getEmail());
        embeddedUser.setType(null);
        return embeddedUser;
    }

    @Override
    public User convertToEmbedded(User user, List<String> fields) {
        return user; // default to returning to much
    }

    @Override
    public Link mkSelfLink(User user) {
        return mkSelfLinkToId(user.getId());
    }

    @Override
    public Link mkSelfLinkToId(String id) {
        return linkTo(UserController.class).slash("api" + UserController.USERS_URL + "/" + id).withSelfRel();
    }

    @Override
    protected String getEmbeddedResourceKey() {
        return "sw360:users";
    }


    public void addEmbedded(HalResource halResource, Collection<String> userEmails, Sw360UserService userService, String relation){
        for (String userEmail : userEmails) {
            User sw360User = userService.getUserByEmail(userEmail);
            addEmbedded(halResource, sw360User, relation);
        }
    }


    public void addEmbeddedModerators(HalResource halResource, Collection<String> userEmails, Sw360UserService userService){
        addEmbedded(halResource, userEmails, userService, "sw360:moderators");
    }
}
