package commands.specialUsersCommands.hostCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.specialUsersCommands.SpecialUserCommand;
import database.Database;
import database.userData.DatabaseUser;
import database.userData.HostUser;
import database.userData.hostPageElements.Announcement;

import java.util.HashMap;

public final class RemoveAnnouncementCommand extends SpecialUserCommand {
    private String username;
    private String name;

    /**
     *
     * @param users list of users
     * @param hosts list of hosts
     *
     * The method verifies if the user exists and if it is a host, then it finds if there is an
     * announcement with the name on the host's page. If there is, the announcement is removed
     * from the page, otherwise, an error message is shown.
     */

    public void execute(final HashMap<String, DatabaseUser> users,
                        final HashMap<String, HostUser> hosts) {
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("removeAnnouncement", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (!userExists(users, username, node, currentMapper)
                || !isUserHost(hosts, username, node, currentMapper)) {
            Database.outputs.add(node);
            return;
        }

        HostUser currentHost = hosts.get(username);
        Announcement foundAnnouncement = currentHost.getPage().getAnnouncements()
                .stream()
                .filter(announcement1 -> announcement1.getName().equals(name))
                .findFirst()
                .orElse(null);

        if (foundAnnouncement == null) {
            node.set("message", currentMapper.convertValue(username
                    + " has no announcement with the given name.", JsonNode.class));
        } else {
            currentHost.getPage().getAnnouncements().remove(foundAnnouncement);
            node.set("message", currentMapper.convertValue(username
                    + " has successfully deleted the announcement.", JsonNode.class));
        }
        Database.outputs.add(node);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
