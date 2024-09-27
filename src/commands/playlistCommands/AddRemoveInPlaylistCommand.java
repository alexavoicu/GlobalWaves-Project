package commands.playlistCommands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.Database;
import database.DatabaseSong;
import database.collectionsOfSongs.DatabaseAlbum;
import database.collectionsOfSongs.DatabasePlaylist;
import database.userData.DatabaseMusicPlayer;
import database.userData.DatabaseUser;
import database.userData.SongsCollectionTuple;

import java.util.ArrayList;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class AddRemoveInPlaylistCommand extends Command {
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
     * @param songs list of songs
     * @param users list of users
     *
     * If there is nothing loaded in the player or if the loaded source is not a song/album or if
     * the specified playlist doesn't exist, then an error message is shown.
     * Otherwise, it finds the playlist and based on the existence of the song in the playlist
     * it adds or removes it.
     */

    public void execute(final ArrayList<DatabaseSong> songs,
                        final HashMap<String, DatabaseUser> users,
                        final ArrayList<DatabaseAlbum> albums) {
        DatabaseUser currentUser = users.get(username);
        DatabaseMusicPlayer currentMusicPlayer = currentUser.getDatabaseMusicPlayer();
        String currentLoaded = currentMusicPlayer.getLoaded();

        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("addRemoveInPlaylist", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (currentLoaded == null) {
            node.set("message", currentMapper.convertValue("Please load a source before adding"
                    + " to or removing from the playlist.", JsonNode.class));
            Database.outputs.add(node);
            return;
        }

        if (!currentMusicPlayer.getLoadedType().equals("song")
                && !currentMusicPlayer.getLoadedType().equals("album")) {
            node.set("message", currentMapper.convertValue("The loaded source is not a song.",
                    JsonNode.class));
            Database.outputs.add(node);
            return;
        }

        if (playlistId > currentUser.getCreatedPlaylists().size()) {
            node.set("message", currentMapper.convertValue("The specified playlist does not "
                    + "exist.", JsonNode.class));
            Database.outputs.add(node);
            return;
        }

        DatabasePlaylist playlist = currentUser.getCreatedPlaylists().get(playlistId - 1);
        ArrayList<DatabaseSong> foundSong = new ArrayList<>();
        String songPlaying;

        if (currentMusicPlayer.getLoadedType().equals("song")) {
            foundSong = new ArrayList<>(playlist.getSongs().stream()
                    .filter((s) -> s.getSong().getName().equals(currentLoaded)).toList());
            songPlaying = currentLoaded;
        } else {
            DatabaseAlbum album = new ArrayList<>(albums.stream()
                    .filter((p) -> p.getName().equals(currentLoaded)).toList()).get(0);
            int passedTime = getTimestamp() - currentUser.getDatabaseMusicPlayer().
                    getCurrentStatus().getStartTime();
            SongsCollectionTuple collectionSongInfo = album.findSongPlaying(passedTime);
            DatabaseSong songPlayingInAlbum = collectionSongInfo.getSong();
            String finalSongPlaying = songPlayingInAlbum.getSong().getName();
            foundSong = new ArrayList<>(playlist.getSongs().stream()
                    .filter((s) -> s.getSong().getName().equals(finalSongPlaying)).toList());
            songPlaying = finalSongPlaying;
        }

        if (foundSong.isEmpty()) {
            ArrayList<DatabaseSong> songToAdd = new ArrayList<>(songs.stream()
                    .filter((s) -> s.getSong().getName().equals(songPlaying)).toList());
            playlist.getSongs().add(songToAdd.get(0));
            node.set("message", currentMapper.convertValue("Successfully added to playlist.",
                    JsonNode.class));
        } else {
            playlist.getSongs().remove(foundSong.get(0));
            node.set("message", currentMapper.convertValue("Successfully removed from "
                    + "playlist.", JsonNode.class));
        }

        Database.outputs.add(node);
    }
}
