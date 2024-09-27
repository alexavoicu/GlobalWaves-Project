package commands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import database.Database;
import database.DatabaseSong;
import database.collectionsOfSongs.DatabaseAlbum;
import database.collectionsOfSongs.DatabasePlaylist;
import database.collectionsOfSongs.SongsCollection;
import database.userData.DatabaseMusicPlayer;
import database.userData.DatabaseUser;
import database.userData.SongsCollectionTuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ShuffleCommand extends Command {
    private String username;
    private int seed;

    /**
     *
     * @param user the user that gave the command
     * @param playlists the list of playlists
     *
     * The method verifies if the music player is loaded and if the audio loaded is a playlist,
     * if the conditions are not met, an error message is shown. Otherwise, if the playlist is not
     * already shuffled, then the shuffle method is called.
     * If the playlist is already shuffled, then the un-shuffle method is called.
     */

    public void execute(final DatabaseUser user, final ArrayList<DatabasePlaylist> playlists,
                        final ArrayList<DatabaseAlbum> albums) {
        DatabaseMusicPlayer currentPlayer = user.getDatabaseMusicPlayer();
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("shuffle", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (currentPlayer.getLoaded() == null) {
            node.set("message", currentMapper.convertValue("Please load a source before using the"
                            + " shuffle function.", JsonNode.class));
            Database.outputs.add(node);
            return;
        }

        if (!currentPlayer.getLoadedType().equals("playlist")
                && !currentPlayer.getLoadedType().equals("album")) {
            node.set("message", currentMapper.convertValue("The loaded source is not a playlist"
                    + " or an album.", JsonNode.class));
            Database.outputs.add(node);
            return;
        }

        SongsCollection songsCollection;
        if (currentPlayer.getLoadedType().equals("playlist")) {
            songsCollection = new ArrayList<>(playlists.stream()
                    .filter((p) -> p.getName().equals(currentPlayer.getLoaded())).toList()).get(0);
        } else {
            songsCollection = new ArrayList<>(albums.stream()
                    .filter((p) -> p.getName().equals(currentPlayer.getLoaded())).toList()).get(0);
        }

        int passedTime = getTimestamp() - currentPlayer.getCurrentStatus().getStartTime();

        if (currentPlayer.isShuffled()) {
            unshuffleForSongCollection(songsCollection, user, passedTime, node, currentMapper);
        } else {
            shuffleForSongCollection(songsCollection, user, passedTime, node, currentMapper);
        }

        Database.outputs.add(node);
    }

    /**
     *
     * @param songsCollection collection of songs
     * @param user the user
     * @param passedTime the time passed
     * @param node the output node
     * @param currentMapper the object mapper
     *
     * The method finds the song currently playing in the
     * original playlist and the total time until that song plays, so that it can update the start
     * time accordingly. At last, the player status, start time and time left are modified. The
     * shuffled playlist is erased.
     */


    public void unshuffleForSongCollection(final SongsCollection songsCollection,
                                           final DatabaseUser user,
                                           final int passedTime, final ObjectNode node,
                                           final ObjectMapper currentMapper) {
        DatabaseMusicPlayer currentPlayer = user.getDatabaseMusicPlayer();

        int finalPassedTime = passedTime;
        if (user.getSongsCollectionPassedTimeBeforeShuffle()
                .containsKey(songsCollection.getName())) {
            finalPassedTime = finalPassedTime - user.getSongsCollectionPassedTimeBeforeShuffle()
                    .get(songsCollection.getName());
        } else return;
        DatabasePlaylist shuffledPlaylist = user.getShuffledSongsCollections()
                .get(songsCollection.getName());
        SongsCollectionTuple playlistSongInfo = shuffledPlaylist
                .findSongPlaying(finalPassedTime);

        if (playlistSongInfo != null) {
            int passedTimeFromSong = playlistSongInfo.getPassedTimeSong();
            int timePassToSongPlayingNormalPlaylist = songsCollection.
                    findTotalTimePlayedUntilSong(playlistSongInfo
                            .getSong().getSong().getName());

            currentPlayer.getCurrentStatus().setStartTime(getTimestamp()
                    - timePassToSongPlayingNormalPlaylist - passedTimeFromSong);

            currentPlayer.getCurrentStatus().setTimeLeft(songsCollection
                    .getTotalCollectionDuration());


            currentPlayer.setShuffled(false);
            user.getShuffledSongsCollections().clear();
            node.set("message", currentMapper.convertValue("Shuffle function"
                    + " deactivated successfully.", JsonNode.class));
        }
    }

    /**
     *
     * @param songsCollection the collection of songs
     * @param user the user
     * @param passedTime the passed time from playing
     * @param node the output node
     * @param currentMapper the object mapper
     *
     * The method finds what songs is playing and how much time it has passed from it and then
     * creates a list of shuffled indexes is created using the seed given in the
     * command. From there, a new shuffled collection is created and saved in the users shuffled
     * collections. This happens because there is a need to know the order of the songs, to be
     * able to determine which song is playing in any moment.
     * The shuffled playlist is then truncated to where the playing song starts and the time
     * passed from the original playlist is saved in the users' data. The player status is updated.
     */
    public void shuffleForSongCollection(final SongsCollection songsCollection,
                                         final DatabaseUser user,
                                         final int passedTime, final ObjectNode node,
                                         final ObjectMapper currentMapper) {

        int finalPassedTime = passedTime;
        DatabaseMusicPlayer currentPlayer = user.getDatabaseMusicPlayer();
        if (!currentPlayer.getCurrentStatus().isPlaying()) {
            finalPassedTime = currentPlayer.getCurrentStatus().getPauseTimestamp()
                    - currentPlayer.getCurrentStatus().getStartTime();
        }

        SongsCollectionTuple playlistSongInfo = songsCollection.findSongPlaying(finalPassedTime);
        int passedTimeFromSong = playlistSongInfo.getPassedTimeSong();

        user.getShuffledSongsCollections().remove(currentPlayer.getLoaded());
        Random random = new Random(seed);

        DatabasePlaylist shuffledPlaylist = new DatabasePlaylist(songsCollection.getName(),
                new ArrayList<>(), username, getTimestamp());
        ArrayList<Integer> ids = new ArrayList<>();
        for (int i = 0; i < songsCollection.getSongs().size(); i++) {
            ids.add(i);
        }
        Collections.shuffle(ids, random);

        for (int i = 0; i < ids.size(); i++) {
            shuffledPlaylist.getSongs().add(songsCollection.getSongs().get(ids.get(i)));
        }

        user.getShuffledSongsCollections().put(songsCollection.getName(), shuffledPlaylist);

        DatabasePlaylist shuffledCollection = user.getShuffledSongsCollections()
                .get(songsCollection.getName());
        Iterator<DatabaseSong> iterator = shuffledCollection.getSongs().iterator();
        while (iterator.hasNext()) {
            DatabaseSong song = iterator.next();
            if (!song.getSong().getName().equals(playlistSongInfo.getSong().getSong()
                    .getName())) {
                iterator.remove();
            } else {
                break;
            }
        }

        currentPlayer.setShuffled(true);
        currentPlayer.getCurrentStatus().setTimeLeft(shuffledPlaylist
                .getTotalCollectionDuration());
        user.getSongsCollectionPassedTimeBeforeShuffle()
                .put(songsCollection.getName(), finalPassedTime - passedTimeFromSong);
        node.set("message", currentMapper.convertValue("Shuffle function activated"
                + " successfully.", JsonNode.class));

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(final int seed) {
        this.seed = seed;
    }
}
