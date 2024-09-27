package commands.adminCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.Database;
import database.DatabaseSong;
import database.collectionsOfSongs.DatabaseAlbum;
import database.collectionsOfSongs.DatabasePlaylist;
import database.userData.ArtistUser;
import database.userData.DatabaseUser;
import database.userData.HostUser;
import fileio.input.PodcastInput;

import java.util.ArrayList;
import java.util.HashMap;

public final class DeleteUserCommand extends Command {
    private String username;

    /**
     *
     * @param users list of users
     * @param artists list of artists
     * @param hosts list of hosts
     * @param albums list of albums
     * @param songs list of songs
     * @param playlists list of playlists
     * @param podcasts list of podcasts
     *
     * The method first checks if the user exists, and shows the respective message if it doesn't.
     * Otherwise, it checks what kind of user it is, if it can be deleted and then calls the
     * respective methods to handle the deletion.
     */

    public void execute(final HashMap<String, DatabaseUser> users,
                        final HashMap<String, ArtistUser> artists,
                        final HashMap<String, HostUser> hosts,
                        final ArrayList<DatabaseAlbum> albums,
                        final ArrayList<DatabaseSong> songs,
                        final ArrayList<DatabasePlaylist> playlists,
                        final ArrayList<PodcastInput> podcasts) {

        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("deleteUser", JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));

        if (!users.containsKey(username)) {
            node.set("message", currentMapper.convertValue("The username " + username
                    + " doesn't exist.", JsonNode.class));
            Database.outputs.add(node);
            return;
        }

        if (artists.containsKey(username) && artists.get(username)
                .canArtistBeDeleted(users, playlists)) {

            handleArtistDeletion(node, artists, users, albums, songs, currentMapper);
        } else if (!artists.containsKey(username) && !hosts.containsKey(username)
                && users.get(username).canUserBeDeleted(users, playlists)) {

            handleUserDeletion(node, users, playlists, currentMapper);
        } else if (hosts.containsKey(username) && hosts
                .get(username).canHostBeDeleted(users)) {

            handleHostDeletion(node, hosts, podcasts, users, currentMapper);
        } else {
            node.set("message", currentMapper.convertValue(username
                    + " can't be deleted.", JsonNode.class));
        }

        Database.outputs.add(node);
    }

    /**
     *
     * @param node the output node
     * @param artists list of artists
     * @param users list of users
     * @param albums list of albums
     * @param songs list of songs
     * @param currentMapper the object mapper
     *
     * Firstly, the method , makes sure that to delete anything related to the artist. For each
     * user, it deletes the liked songs created by that artist, then it removes the albums of the
     * artist from the albums list and the songs from the list of songs. Finally, it deletes the
     * artist from the list of artists and the list of users and puts the success message in
     * the output node.
     */

    private void handleArtistDeletion(final ObjectNode node,
                                      final HashMap<String, ArtistUser> artists,
                                      final HashMap<String, DatabaseUser> users,
                                      final ArrayList<DatabaseAlbum> albums,
                                      final ArrayList<DatabaseSong> songs,
                                      final ObjectMapper currentMapper) {

        for (DatabaseUser currentUser : users.values()) {
            currentUser.deleteLikedContentFromArtist(username);
        }

        albums.removeIf(album -> album.getOwner().equals(username));
        songs.removeIf(song -> song.getSong().getArtist().equals(username));

        artists.remove(username);
        users.remove(username);

        node.set("message", currentMapper.convertValue(username
                + " was successfully deleted.", JsonNode.class));
    }

    /**
     *
     * @param node the output node
     * @param users the list of users
     * @param playlists the list of playlists
     * @param currentMapper the object mapper
     *
     * The method starts by getting the current user then, for every playlist that the user
     * followed, it decreases the number of follows. When deleting a normal user, the playlists
     * created by him need to be deleted, so the method does that by removing them from the
     * list of playlists and also from the list of followed playlists of each user.
     * Finally, it deletes the artist from the list of users and outputs the success message.
     */

    private void handleUserDeletion(final ObjectNode node,
                                    final HashMap<String, DatabaseUser> users,
                                    final ArrayList<DatabasePlaylist> playlists,
                                    final ObjectMapper currentMapper) {
        DatabaseUser currentUser = users.get(username);

        for (DatabasePlaylist playlist : currentUser.getFollowedPlaylists()) {
            playlist.setNrOfFollows(playlist.getNrOfFollows() - 1);
        }

        for (DatabaseUser entry : users.values()) {
            entry.deleteFollowedPlaylistsFromUser(username);
        }

        playlists.removeIf(databasePlaylist -> databasePlaylist.getOwner().equals(username));

        users.remove(username);
        node.set("message", currentMapper.convertValue(username
                + " was successfully deleted.", JsonNode.class));
    }

    /**
     *
     * @param node the output node
     * @param hosts the list of hosts
     * @param podcasts the list of podcasts
     * @param users the list of users
     * @param currentMapper the object mapper
     *
     * The method removes the podcasts, that are owned by the host, from the list of podcasts
     * and the removes the user from the list of hosts and users and outputs the success message.
     */

    private void handleHostDeletion(final ObjectNode node, final HashMap<String, HostUser> hosts,
                                    final ArrayList<PodcastInput> podcasts,
                                    final HashMap<String, DatabaseUser> users,
                                    final ObjectMapper currentMapper) {
        podcasts.removeIf(podcastInput -> podcastInput.getOwner().equals(username));
        users.remove(username);
        hosts.remove(username);
        node.set("message", currentMapper.convertValue(username
                + " was successfully deleted.", JsonNode.class));
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }
}
