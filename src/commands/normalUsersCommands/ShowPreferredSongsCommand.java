package commands.normalUsersCommands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.Database;
import database.DatabaseSong;
import database.userData.DatabaseUser;
import java.util.ArrayList;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ShowPreferredSongsCommand extends Command {
    private String username;

    /**
     *
     * @param users list of users
     *
     * The method gets the user and creates a list of the preferred songs and then puts it in the
     * output.
     */

    public void execute(final HashMap<String, DatabaseUser> users) {
        DatabaseUser currentUser = users.get(username);

        ArrayList<String> songList = new ArrayList<>();
        for (DatabaseSong song : currentUser.getLikedSongs()) {
            songList.add(song.getSong().getName());
        }

        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("showPreferredSongs", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        node.set("result", currentMapper.convertValue(songList, JsonNode.class));

        Database.outputs.add(node);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }
}
