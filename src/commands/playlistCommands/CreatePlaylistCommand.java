package commands.playlistCommands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.Database;
import database.collectionsOfSongs.DatabasePlaylist;
import database.userData.DatabaseUser;

import java.util.ArrayList;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class CreatePlaylistCommand extends Command {
    private String username;
    private String playlistName;

    /**
     *
     * @param users the list of users
     * @param playlists the list of playlists
     *
     * The method first verifies if the playlist already exists, if it does, then an error
     * message is shown. Otherwise, it creates it and add it to the list of playlists
     * from the database and to the list of playlists created by the user.
     */

    public void execute(final HashMap<String, DatabaseUser> users,
                        final ArrayList<DatabasePlaylist> playlists) {
        DatabaseUser currentUser = users.get(username);
        ArrayList<DatabasePlaylist> filteredPlaylists = new ArrayList<>(playlists.stream()
                .filter((playlist) -> playlist.getName().equals(playlistName)).toList());

        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("createPlaylist", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (filteredPlaylists.isEmpty()) {
            DatabasePlaylist newPlaylist = new DatabasePlaylist(playlistName,
                    new ArrayList<>(), username, getTimestamp());
            playlists.add(newPlaylist);
            currentUser.getCreatedPlaylists().add(newPlaylist);
            node.set("message", currentMapper.convertValue("Playlist created successfully.",
                    JsonNode.class));

        } else {
            node.set("message", currentMapper.convertValue("A playlist with the same"
                    + " name already exists.", JsonNode.class));
        }

        Database.outputs.add(node);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(final String playlistName) {
        this.playlistName = playlistName;
    }
}
