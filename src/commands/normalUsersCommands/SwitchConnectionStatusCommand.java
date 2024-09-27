package commands.normalUsersCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.Database;
import database.userData.DatabaseUser;

import java.util.HashMap;

public final class SwitchConnectionStatusCommand extends Command {
    private String username;

    /**
     *
     * @param users the list of users
     *
     * The method verifies if the user exists and is a normal user, then sets the user
     * to online/offline accordingly and sets the timestamp when the user went offline
     * and updates the start time so that it can simulate that it was never offline.
     */

    public void execute(final HashMap<String, DatabaseUser> users) {
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("switchConnectionStatus", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));
        if (!users.containsKey(username)) {
            node.set("message", currentMapper.convertValue("The username " + username
                            + " doesn't exist.", JsonNode.class));
            Database.outputs.add(node);
            return;
        }

        if (!users.get(username).isNormalUser()) {
            node.set("message", currentMapper.convertValue(username + " is not a normal user.",
                    JsonNode.class));
            Database.outputs.add(node);
            return;
        }

        DatabaseUser currentUser = users.get(username);
        if (currentUser.isOnline()) {
            currentUser.setOnline(false);
            currentUser.setSwitchToOfflineTimestamp(getTimestamp());
        } else {
            currentUser.setOnline(true);

            if (currentUser.getDatabaseMusicPlayer().getCurrentStatus() != null) {
                int startTimestamp = currentUser.getDatabaseMusicPlayer()
                        .getCurrentStatus().getStartTime();
                currentUser.getDatabaseMusicPlayer().getCurrentStatus()
                        .setStartTime(startTimestamp + getTimestamp()
                                - currentUser.getSwitchToOfflineTimestamp());
                currentUser.setSwitchToOfflineTimestamp(0);
            }
        }
        node.set("message", currentMapper.convertValue(username
                        + " has changed status successfully.", JsonNode.class));
        Database.outputs.add(node);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }
}
