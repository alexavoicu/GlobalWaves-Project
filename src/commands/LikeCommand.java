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
import database.userData.RepeatSongsCollectionTuple;
import database.userData.SongsCollectionTuple;

import java.util.ArrayList;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class LikeCommand extends Command {
    private String username;

    /**
     *
     * @param users list of users
     * @param songs list of songs
     * @param playlists list of playlists
     *
     * If the loaded audio is a podcast or there is nothing loaded, then an error message is shown.
     * Otherwise, it calls the precessSongLike method.
     */


    public void execute(final HashMap<String, DatabaseUser> users,
                        final ArrayList<DatabaseSong> songs,
                        final ArrayList<DatabasePlaylist> playlists,
                        final ArrayList<DatabaseAlbum> albums) {

        DatabaseUser currentUser = users.get(username);
        DatabaseMusicPlayer currentMusicPlayer = currentUser.getDatabaseMusicPlayer();
        String currentLoaded = currentMusicPlayer.getLoaded();

        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("like", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (currentLoaded == null) {
            node.set("message", currentMapper.convertValue("Please load a source before"
                            + " liking or unliking.",
                    JsonNode.class));
        } else if (currentMusicPlayer.getLoadedType().equals("podcast")) {
            node.set("message", currentMapper.convertValue("Loaded source"
                    + " is not a song.", JsonNode.class));
        } else {
            processSongLike(currentUser, songs, playlists, albums, node, currentMapper);
        }
        Database.outputs.add(node);

    }

    /**
     *
     * @param currentUser user
     * @param songs list of songs
     * @param playlists list of playlists
     * @param albums list of albums
     * @param node the output node
     * @param mapper the object mapper
     *
     * The method verifies what kind of audio is loaded and calls their respective functions.
     */
    private void processSongLike(final DatabaseUser currentUser,
                                 final ArrayList<DatabaseSong> songs,
                                 final ArrayList<DatabasePlaylist> playlists,
                                 final ArrayList<DatabaseAlbum> albums,
                                 final ObjectNode node,
                                 final ObjectMapper mapper) {
        switch (currentUser.getDatabaseMusicPlayer().getLoadedType()) {
            case "playlist" -> {
                DatabasePlaylist playlist = new ArrayList<>(playlists.stream()
                        .filter((p) -> p.getName().equals(currentUser
                                .getDatabaseMusicPlayer().getLoaded())).toList()).get(0);
                likeForSongCollection(currentUser, playlist, node, mapper);
            }
            case "album" -> {
                DatabaseAlbum album = new ArrayList<>(albums.stream()
                        .filter((p) -> p.getName().equals(currentUser
                                .getDatabaseMusicPlayer().getLoaded())).toList()).get(0);
                likeForSongCollection(currentUser, album, node, mapper);
            }
            case "song" -> likeForSong(currentUser, songs, node, mapper);
            default -> {
                return;
            }
        }
    }

    /**
     *
     * @param currentUser the user
     * @param songs list of songs
     * @param node the output node
     * @param currentMapper the object mapper
     *
     * The method verifies if the song is already liked by the user and removes it from the liked
     * list, otherwise the song is found and added to the list.
     */

    private void likeForSong(final DatabaseUser currentUser,
                                       final ArrayList<DatabaseSong> songs,
                                       final ObjectNode node,
                                       final ObjectMapper currentMapper) {

        String currentLoaded = currentUser.getDatabaseMusicPlayer().getLoaded();
        ArrayList<DatabaseSong> foundSong = new ArrayList<DatabaseSong>(currentUser
                .getLikedSongs()
                .stream().filter((s) -> s.getSong()
                        .getName().equals(currentLoaded)).toList());
        if (foundSong.isEmpty()) {
            ArrayList<DatabaseSong> songToLike = new ArrayList<DatabaseSong>(songs.stream()
                    .filter((s) -> s.getSong().getName().
                            equals(currentLoaded)).toList());
            currentUser.getLikedSongs().add(songToLike.get(0));
            songToLike.get(0).setNumberOfLikes(songToLike.get(0)
                    .getNumberOfLikes() + 1);
            node.set("message", currentMapper.convertValue("Like registered "
                    + "successfully.", JsonNode.class));
        } else {
            currentUser.getLikedSongs().remove(foundSong.get(0));
            foundSong.get(0).setNumberOfLikes(foundSong.get(0).getNumberOfLikes() - 1);
            node.set("message", currentMapper.convertValue("Unlike "
                    + "registered successfully.", JsonNode.class));
        }
    }

    /**
     *
     * @param currentUser user
     * @param songsCollection collection of songs
     * @param node output node
     * @param currentMapper object mapper
     *
     * Firstly, the passed time is calculated and based on that the song playing is found.
     * After that, it likes/unlikes it.
     */

    public void likeForSongCollection(final DatabaseUser currentUser,
                                      final SongsCollection songsCollection,
                                      final ObjectNode node,
                                      final ObjectMapper currentMapper) {
        int passedTime = getTimestamp() - currentUser.getDatabaseMusicPlayer().
                getCurrentStatus().getStartTime();
        SongsCollection currentSongsCollection = songsCollection;

        if (currentUser.getDatabaseMusicPlayer().isShuffled()) {
            currentSongsCollection = currentUser.getShuffledSongsCollections()
                    .get(currentUser.getDatabaseMusicPlayer().getLoaded());
            if (currentUser.getSongsCollectionPassedTimeBeforeShuffle()
                    .containsKey(currentUser.getDatabaseMusicPlayer().getLoaded())) {
                passedTime = passedTime - currentUser
                        .getSongsCollectionPassedTimeBeforeShuffle()
                        .get(currentUser.getDatabaseMusicPlayer().getLoaded());
            }
            if (currentSongsCollection == null) {
                return;
            }
        }

        SongsCollectionTuple collectionSongInfo = currentSongsCollection
                .findSongPlaying(passedTime);
        if (collectionSongInfo != null) {
            DatabaseSong songPlaying = collectionSongInfo.getSong();
            String finalSongPlaying;
            if (currentUser.getDatabaseMusicPlayer().getRepeatStatus() == 2 && currentUser
                    .getSongsCollectionRepeatInfo().containsKey(songsCollection.getName())) {
                RepeatSongsCollectionTuple songsCollectionRepeatInfo = currentUser
                        .getSongsCollectionRepeatInfo().get(songsCollection.getName());
                songPlaying = songsCollectionRepeatInfo.getRepeatedSong();
                finalSongPlaying = songPlaying.getSong().getName();
            } else {
                finalSongPlaying = songPlaying.getSong().getName();
            }

            ArrayList<DatabaseSong> foundSong = new ArrayList<DatabaseSong>(currentUser
                    .getLikedSongs().stream().filter((s) -> s.getSong()
                            .getName().equals(finalSongPlaying)).toList());

            if (foundSong.isEmpty()) {
                currentUser.getLikedSongs().add(songPlaying);
                songPlaying.setNumberOfLikes(songPlaying.getNumberOfLikes() + 1);
                node.set("message", currentMapper.convertValue("Like registered "
                        + "successfully.", JsonNode.class));
            } else {
                currentUser.getLikedSongs().remove(foundSong.get(0));
                foundSong.get(0).setNumberOfLikes(foundSong.get(0).getNumberOfLikes() - 1);
                node.set("message", currentMapper.convertValue("Unlike "
                        + "registered successfully.", JsonNode.class));
            }
        }
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }
}
