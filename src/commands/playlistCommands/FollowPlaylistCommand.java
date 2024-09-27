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
public final class FollowPlaylistCommand extends Command {
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     *
     * @param users list of users
     * @param playlists list of playlists
     *
     * If there isn't a selected item, or the selected item is not a playlist, then an error
     * message is shown. Otherwise, the method verifies if the selected playlist is created by the
     * user, if it is, then an error message is shown. After that, the method finds the playlist
     * and based on its existence in the user's followed playlists list, it adds or removes it
     * from there.
     */

    public void execute(final HashMap<String, DatabaseUser> users,
                        final ArrayList<DatabasePlaylist> playlists) {
        DatabaseUser currentUser = users.get(username);
        ArrayList<DatabasePlaylist> playlistToFollow = new ArrayList<>(playlists.stream()
                .filter((p) -> p.getName().equals(currentUser.getDatabaseSearchbar()
                        .getSelectedItem())).toList());

        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("follow", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (currentUser.getDatabaseSearchbar().getSelectedItem() == null) {
            node.set("message", currentMapper.convertValue("Please select a source before"
                            + " following or unfollowing.", JsonNode.class));
        } else if (!currentUser.getDatabaseSearchbar().getSearchSelectedType().equals("playlist")) {
            node.set("message", currentMapper.convertValue("The selected source is not a "
                    + "playlist.", JsonNode.class));
        } else {
            ArrayList<DatabasePlaylist> createdPlaylist = new ArrayList<>(currentUser
                    .getCreatedPlaylists().stream().filter((p) -> p.getName().equals(currentUser
                            .getDatabaseSearchbar().getSelectedItem())).toList());
            if (!createdPlaylist.isEmpty()) {
                node.set("message", currentMapper.convertValue("You cannot follow or unfollow your"
                        + " own playlist.", JsonNode.class));
            } else {
                ArrayList<DatabasePlaylist> isPlaylistFollowed = new ArrayList<>(currentUser
                        .getFollowedPlaylists().stream().filter((p) -> p.getName()
                                .equals(currentUser.getDatabaseSearchbar()
                                        .getSelectedItem())).toList());
                if (isPlaylistFollowed.isEmpty()) {
                    currentUser.getFollowedPlaylists().add(playlistToFollow.get(0));
                    node.set("message", currentMapper.convertValue("Playlist followed "
                            + "successfully.", JsonNode.class));
                    int nrOfFollows = playlistToFollow.get(0).getNrOfFollows();
                    playlistToFollow.get(0).setNrOfFollows(nrOfFollows + 1);
                } else {
                    currentUser.getFollowedPlaylists().remove(playlistToFollow.get(0));
                    node.set("message", currentMapper.convertValue("Playlist unfollowed "
                            + "successfully.", JsonNode.class));
                    int nrOfFollows = playlistToFollow.get(0).getNrOfFollows();
                    playlistToFollow.get(0).setNrOfFollows(nrOfFollows - 1);
                }
            }
        }
        Database.outputs.add(node);
    }
}
