package commands.generalStatisticsCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.Database;
import database.userData.DatabaseUser;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public final class GetOnlineUsersCommand extends Command {
    /**
     *
     * @param users list of users
     *
     * The method creates a list of usernames, by filtering the users by the isNormalUser field,
     * then the list is put in the output node.
     */
    public void execute(final HashMap<String, DatabaseUser> users) {
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("getOnlineUsers", JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        List<String> onlineUsers = users.values()
                .stream()
                .filter(user -> user.isOnline() && user.isNormalUser())
                .map(user -> user.getUserInput().getUsername())
                .collect(Collectors.toList());

        node.set("result", currentMapper.convertValue(onlineUsers, JsonNode.class));
        Database.outputs.add(node);
    }
}
