package commands.specialUsersCommands.artistCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.specialUsersCommands.SpecialUserCommand;
import database.Database;
import database.collectionsOfSongs.DatabaseAlbum;
import database.collectionsOfSongs.DatabasePlaylist;
import database.userData.ArtistUser;
import database.userData.DatabaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class RemoveAlbumCommand extends SpecialUserCommand {
    private String username;
    private String name;

    /**
     *
     * @param users list of users
     * @param artists list of artists
     * @param albums list of albums
     * @param playlists list of playlists
     *
     * The method verifies if the user exists and if it is an artist, then it finds the album
     * that needs to be deleted. If there is no such album, then an error message is shown,
     * otherwise, it calls the method that verifies if the album can be deleted or not.
     * If the return is true, then it removes the album from the list of albums and from the
     * artist's page, otherwise an error message is shown.
     */

    public void execute(final HashMap<String, DatabaseUser> users,
                        final HashMap<String, ArtistUser> artists,
                        final ArrayList<DatabaseAlbum> albums,
                        final ArrayList<DatabasePlaylist> playlists) {

        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("removeAlbum", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (!userExists(users, username, node, currentMapper)
                || !isUserArtist(artists, username, node, currentMapper)) {
            Database.outputs.add(node);
            return;
        }

        ArtistUser currentArtist = artists.get(username);
        DatabaseAlbum albumToRemove = findAlbum(currentArtist, name);

        if (albumToRemove == null) {
            node.set("message", currentMapper.convertValue(username
                    + " doesn't have an album with the given name.", JsonNode.class));
        } else {
            if (canAlbumBeDeleted(users, playlists)) {
                node.set("message", currentMapper.convertValue(username
                        + " deleted the album successfully.", JsonNode.class));

                albums.removeIf(album1 -> album1.getName().equals(name));
                currentArtist.getPage().getAlbums()
                        .removeIf(album1 -> album1.getName().equals(name));
            } else {
                node.set("message", currentMapper.convertValue(username
                        + " can't delete this album.", JsonNode.class));
            }
        }

        Database.outputs.add(node);
    }

    /**
     *
     * @param currentArtist the artist
     * @param albumName the name of the album
     * @return the album if found, otherwise null
     *
     * The method filters through the albums from the host's page and if the name matches the
     * album name, it returns it, otherwise it returns null.
     */

    private DatabaseAlbum findAlbum(final ArtistUser currentArtist, final String albumName) {
        return currentArtist.getPage().getAlbums()
                .stream()
                .filter(album -> album.getName().equals(albumName))
                .findFirst()
                .orElse(null);
    }

    /**
     *
     * @param users list of users
     * @param playlists list of playlists
     * @return if album can be safely deleted or not
     *
     * The method iterates through the users and if the loaded audio is the album, then it returns
     * false and if  there is a playlist loaded, and the playlist contains songs from the album,
     * then it returns false. Otherwise, it returns true.
     */

    private boolean canAlbumBeDeleted(final HashMap<String, DatabaseUser> users,
                                      final ArrayList<DatabasePlaylist> playlists) {
        for (Map.Entry<String, DatabaseUser> entry : users.entrySet()) {
            DatabaseUser currentUser = entry.getValue();
            String audioPlaying = currentUser.getDatabaseMusicPlayer().getLoaded();
            if (audioPlaying != null && audioPlaying.equals(name)) {
                return false;
            }
            if (currentUser.getDatabaseMusicPlayer().getLoadedType() != null
                    && currentUser.getDatabaseMusicPlayer().getLoadedType().equals("playlist")) {
                DatabasePlaylist playlist = new ArrayList<>(playlists.stream()
                        .filter((p) -> p.getName().equals(audioPlaying)).toList()).get(0);
                if (playlist.containsSongsFromAlbum(name)) {
                    return false;
                }
            }
        }
        return true;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
