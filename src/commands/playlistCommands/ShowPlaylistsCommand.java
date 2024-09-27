package commands.playlistCommands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.Database;
import database.collectionsOfSongs.DatabasePlaylist;
import database.DatabaseSong;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ShowPlaylistsCommand extends Command {
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     *
     * @param playlists list of playlists
     *
     * The method verifies if there are any playlists owned by the user. If there aren't, then an
     * error message is shown. Otherwise, an iteration is made through all of them and their
     * characteristics are printed.
     */

    public void execute(final ArrayList<DatabasePlaylist> playlists) {
        ArrayList<DatabasePlaylist> ownedPlaylists = new ArrayList<>(playlists.stream()
                .filter((playlist) -> playlist
                .getOwner().equals(username)).toList());

        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("showPlaylists", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (ownedPlaylists.isEmpty()) {
            node.set("message", currentMapper.convertValue("No playlists to show.",
                    JsonNode.class));
        } else {
                ArrayList<ObjectNode> results = new ArrayList<>();
            for (DatabasePlaylist playlist : ownedPlaylists) {
                ObjectNode playlistInfo = currentMapper.createObjectNode();
                playlistInfo.set("name", currentMapper.convertValue(playlist.getName(),
                        JsonNode.class));

                ArrayList<String> songList = new ArrayList<>();
                for (DatabaseSong song : playlist.getSongs()) {
                    songList.add(song.getSong().getName());
                }
                playlistInfo.set("songs", currentMapper.convertValue(songList, JsonNode.class));

                if (playlist.getPrivate()) {
                    playlistInfo.set("visibility", currentMapper.convertValue("private",
                            JsonNode.class));
                } else {
                    playlistInfo.set("visibility", currentMapper.convertValue("public",
                            JsonNode.class));
                }
                playlistInfo.set("followers", currentMapper.convertValue(playlist.getNrOfFollows(),
                        JsonNode.class));

                results.add(playlistInfo);
            }
            node.set("result", currentMapper.convertValue(results, JsonNode.class));
        }
        Database.outputs.add(node);
    }
}
