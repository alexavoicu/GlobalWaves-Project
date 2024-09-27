package commands.specialUsersCommands.artistCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.specialUsersCommands.SpecialUserCommand;
import database.Database;
import database.DatabaseSong;
import database.collectionsOfSongs.DatabaseAlbum;
import database.userData.ArtistUser;
import database.userData.DatabaseUser;
import fileio.input.SongInput;

import java.util.ArrayList;
import java.util.HashMap;


public final class AddAlbumCommand extends SpecialUserCommand {
    private String username;
    private String name;
    private int releaseYear;
    private String description;
    private ArrayList<SongInput> songs;

    /**
     *
     * @param users list of users
     * @param artists list of artists
     * @param albums list of albums
     * @param databaseSongs list of songs
     *
     * The method verifies if the user exists and if the user is an artist, after that it gets the
     * artist and verifies if an album with the same name already exists. If it does, an error
     * message is shown, otherwise if no songs appears twice in the input, it creates the list of
     * DatabaseSongs and the new album. After that, the album is added to the artist's page and
     * the albums list and all the songs are added to the songs list.
     */

    public void execute(final HashMap<String, DatabaseUser> users,
                        final HashMap<String, ArtistUser> artists,
                        final ArrayList<DatabaseAlbum> albums,
                        final ArrayList<DatabaseSong> databaseSongs) {

        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("addAlbum", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (!userExists(users, username, node, currentMapper)
                || !isUserArtist(artists, username, node, currentMapper)) {
            Database.outputs.add(node);
            return;
        }

        ArtistUser currentArtist = artists.get(username);
        DatabaseAlbum existingAlbum = findExistingAlbum(currentArtist, name);

        if (existingAlbum != null) {
            node.set("message", currentMapper.convertValue(username
                    + " has another album with the same name.", JsonNode.class));
        } else if (validateSongIsUnique(currentMapper, node)) {
            ArrayList<DatabaseSong> albumSongs = createDatabaseSongs();
            DatabaseAlbum newAlbum = new DatabaseAlbum(name, albumSongs, username, description);
            node.set("message", currentMapper.convertValue(username
                    + " has added new album successfully.", JsonNode.class));

            currentArtist.getPage().getAlbums().add(newAlbum);
            albums.add(newAlbum);
            databaseSongs.addAll(albumSongs);
        }

        Database.outputs.add(node);
    }

    /**
     *
     * @param currentArtist the artist adding the album
     * @param albumName the name of the album
     * @return the album if it exists, or null if it doesn't
     *
     * The method filters through the albums from the artist's page, if there is an album with the
     * same name then it returns it, otherwise it returns null.
     */
    private DatabaseAlbum findExistingAlbum(final ArtistUser currentArtist,
                                            final String albumName) {
        return currentArtist.getPage().getAlbums()
                .stream()
                .filter(album1 -> album1.getName().equals(albumName))
                .findFirst()
                .orElse(null);
    }

    /**
     *
     * @param currentMapper object mapper
     * @param node the output node
     * @return if any song is included at least twice on the album
     *
     * The method iterates through all the songs in the input and adds the song in a hashmap,
     * if the song name already exists in the hashmap, then the song appears twice in the list
     * and returns false, otherwise returns true.
     */

    private boolean validateSongIsUnique(final ObjectMapper currentMapper, final ObjectNode node) {
        HashMap<String, Integer> nrOfTimesASongIsIncluded = new HashMap<>();
        for (SongInput songInput : songs) {
            if (nrOfTimesASongIsIncluded.containsKey(songInput.getName())) {
                node.set("message", currentMapper.convertValue(username
                        + " has the same song at least twice in this album.", JsonNode.class));
                return false;
            }
            nrOfTimesASongIsIncluded.put(songInput.getName(), 1);
        }
        return true;
    }

    /**
     *
     * @return list of DatabaseSong
     *
     * The method iterates through the songs given in the input of the command and creates
     * a DatabaseSong object for each song, which then is added to the list of databaseSongs.
     * The list is returned.
     */

    private ArrayList<DatabaseSong> createDatabaseSongs() {
        ArrayList<DatabaseSong> albumSongs = new ArrayList<>();
        for (SongInput songInput : songs) {
            albumSongs.add(new DatabaseSong(songInput));
        }
        return albumSongs;
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

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(final int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public ArrayList<SongInput> getSongs() {
        return songs;
    }

    public void setSongs(final ArrayList<SongInput> songs) {
        this.songs = songs;
    }
}
