package commands.playlistCommands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.Database;
import database.collectionsOfSongs.DatabasePlaylist;
import database.userData.DatabaseUser;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class SwitchVisibilityCommand extends Command {
    private String username;
    private int playlistId;

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public int getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(final int playlistId) {
        this.playlistId = playlistId;
    }

    /**
     *
     * @param users list of users
     *
     * The verifies if the playlist id exists, if it doesn't, then an error message is shown.
     * Otherwise, it finds the playlist and switches the visibility.
     */

    public void execute(final HashMap<String, DatabaseUser> users) {
        DatabaseUser currentUser = users.get(username);
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("switchVisibility", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (currentUser.getCreatedPlaylists().size() < playlistId) {
            node.set("message", currentMapper.convertValue("The specified playlist ID is too "
                    + "high.", JsonNode.class));
        } else {
            DatabasePlaylist playlist = currentUser.getCreatedPlaylists().get(playlistId - 1);
            playlist.setPrivate(!playlist.getPrivate());
            if (playlist.getPrivate()) {
                node.set("message", currentMapper.convertValue("Visibility status updated "
                        + "successfully to private.", JsonNode.class));
            } else {
                node.set("message", currentMapper.convertValue("Visibility status updated "
                        + "successfully to public.", JsonNode.class));
            }
        }
        Database.outputs.add(node);
    }

}
