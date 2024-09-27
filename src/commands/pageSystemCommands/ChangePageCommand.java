package commands.pageSystemCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.Database;
import database.userData.DatabaseUser;


public final class ChangePageCommand extends Command {
    private String username;
    private String nextPage;

    /**
     *
     * @param user the user
     *
     * The method verifies if the user is accessing a valid page and if it does, then it sets
     * the page accordingly.
     */

    public void execute(final DatabaseUser user) {
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("changePage", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (!nextPage.equals("Home") && !nextPage.equals("LikedContent")) {
            node.set("message", currentMapper.convertValue(username + " is trying to access"
                    + " a non-existent page.", JsonNode.class));
        } else {
            user.setCurrentPage(nextPage);
            node.set("message", currentMapper.convertValue(username + " accessed " + nextPage
                    + " successfully.", JsonNode.class));
        }
        Database.outputs.add(node);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getNextPage() {
        return nextPage;
    }

    public void setNextPage(final String nextPage) {
        this.nextPage = nextPage;
    }
}
