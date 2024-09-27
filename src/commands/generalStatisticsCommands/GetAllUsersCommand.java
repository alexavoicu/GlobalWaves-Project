package commands.generalStatisticsCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.Database;
import database.userData.DatabaseUser;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public final class GetAllUsersCommand extends Command {
    /**
     *
     * @param users list of users
     *
     * The method creates a list of usernames, by sorting the hashmap of users by the user
     * priority for the output, then results are put in the output node.
     */
    public void execute(final HashMap<String, DatabaseUser> users) {
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("getAllUsers", JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        List<String> allUsernames = users.values().stream()
                .sorted(Comparator.comparingInt(DatabaseUser::userTypePriority))
                .map(user -> user.getUserInput().getUsername())
                .toList();

        node.set("result", currentMapper.convertValue(allUsernames, JsonNode.class));
        Database.outputs.add(node);
    }
}
