package commands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import database.Database;
import database.DatabaseSong;
import database.collectionsOfSongs.DatabaseAlbum;
import database.collectionsOfSongs.DatabasePlaylist;
import database.userData.DatabaseUser;
import database.userData.PlayerTuple;
import database.userData.PodcastTuple;
import fileio.input.PodcastInput;
import fileio.input.SongInput;

import java.util.ArrayList;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class LoadCommand extends Command {
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
     * @param songs list of songs
     * @param podcasts list of podcasts
     *
     * If nothing is selected in the searchbar or if the loaded audio is an empty playlist then
     * an error message is shown. Otherwise, based on the type of audio loaded, their respective
     * load methods are called.
     *
     *
     * If the loaded audio is a song or a playlist then it creates a new PlayerTuple object that
     * is used to update the status of the music player.
     * Otherwise, it calls the loadForPodcast method
     */

    public void execute(final HashMap<String, DatabaseUser> users,
                        final ArrayList<DatabasePlaylist> playlists,
                        final ArrayList<DatabaseSong> songs,
                        final ArrayList<PodcastInput> podcasts,
                        final ArrayList<DatabaseAlbum> albums) {
        DatabaseUser currentUser = users.get(username);

        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("load", JsonNode.class));
        node.set("user", currentMapper.convertValue(currentUser.getUserInput()
                .getUsername(), JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (!validateSelectedItemAndType(currentUser, node, currentMapper, playlists)) {
            Database.outputs.add(node);
            return;
        }

        currentUser.getDatabaseMusicPlayer().setLoaded(currentUser
                .getDatabaseSearchbar().getSelectedItem());
        currentUser.getDatabaseMusicPlayer().setLoadedType(currentUser
                .getDatabaseSearchbar().getType());

        switch (currentUser.getDatabaseSearchbar().getType()) {
            case "song" -> loadForSong(currentUser, songs, node, currentMapper);
            case "podcast" -> loadForPodcast(currentUser.getDatabaseSearchbar().getSelectedItem(),
                    currentUser, podcasts, node, currentMapper);
            case "playlist" -> loadForPlaylist(currentUser, playlists, node, currentMapper);
            case "album" -> loadForAlbum(albums, currentUser, node, currentMapper);
            default -> {
                return;
            }
        }

        currentUser.getDatabaseSearchbar().setSelectedItem(null);
        Database.outputs.add(node);
    }

    /**
     *
     * @param currentUser user
     * @param node output node
     * @param currentMapper object mapper
     * @param playlists list of playlists
     * @return if the source can be loaded
     *
     * If nothing is selected in the searchbar or if the loaded audio is an empty playlist then
     * it returns false.
     */

    private boolean validateSelectedItemAndType(final DatabaseUser currentUser,
                                                final ObjectNode node,
                                                final ObjectMapper currentMapper,
                                                final ArrayList<DatabasePlaylist> playlists) {
        if (currentUser.getDatabaseSearchbar().getSelectedItem() == null) {
            node.set("message", currentMapper.convertValue("Please select a source before "
                    + "attempting to load.", JsonNode.class));
            return false;
        }

        if (currentUser.getDatabaseSearchbar().getType().equals("playlist")) {
            DatabasePlaylist selectedPlaylist = playlists.stream()
                    .filter(playlist -> playlist.getName().equals(currentUser
                            .getDatabaseSearchbar().getSelectedItem()))
                    .findFirst().orElse(null);

            if (selectedPlaylist != null && selectedPlaylist.getSongs().isEmpty()) {
                node.set("message", currentMapper.convertValue("You cannot load an empty audio"
                        + " collection!", JsonNode.class));
                return false;
            }
        }

        return true;
    }

    /**
     *
     * @param currentUser user
     * @param songs list of songs
     * @param node the output node
     * @param currentMapper object mapper
     *
     * If the loaded audio is a song, it creates a new PlayerTuple object that
     * is used to update the status of the music player.
     */

    private void loadForSong(final DatabaseUser currentUser, final ArrayList<DatabaseSong> songs,
                             final ObjectNode node, final ObjectMapper currentMapper) {

        SongInput playingSong = songs.stream()
                .filter(song -> song.getSong().getName().equals(currentUser
                        .getDatabaseSearchbar().getSelectedItem()))
                .findFirst().orElse(null).getSong();

        if (playingSong != null) {
            currentUser.getDatabaseMusicPlayer().setAudioPlayingOwner(playingSong.getArtist());
            int currentDuration = playingSong.getDuration();

            currentUser.getDatabaseMusicPlayer().setCurrentStatus(new PlayerTuple(true,
                    getTimestamp(), currentDuration));
            currentUser.getDatabaseMusicPlayer().setAudioPlayingOwner(playingSong.getArtist());
        }

        node.set("message", currentMapper.convertValue("Playback loaded "
                + "successfully.", JsonNode.class));
    }

    /**
     *
     * @param currentUser user
     * @param playlists list of playlists
     * @param node the output node
     * @param currentMapper object mapper
     *
     * If the loaded audio is a playlist, it creates a new PlayerTuple object that
     * is used to update the status of the music player.
     */

    private void loadForPlaylist(final DatabaseUser currentUser,
                                 final ArrayList<DatabasePlaylist> playlists,
                                 final ObjectNode node,
                                 final ObjectMapper currentMapper) {
        DatabasePlaylist playlist = playlists.stream()
                .filter((p) -> p.getName().equals(currentUser
                        .getDatabaseSearchbar().getSelectedItem()))
                .toList().get(0);
        int currentDuration = 0;
        for (DatabaseSong song : playlist.getSongs()) {
            currentDuration += song.getSong().getDuration();
        }

        currentUser.getDatabaseMusicPlayer().setCurrentStatus(new PlayerTuple(true,
                getTimestamp(), currentDuration));
        node.set("message", currentMapper.convertValue("Playback loaded "
                + "successfully.", JsonNode.class));
    }

    /**
     *
     * @param podcastName the podcast name
     * @param currentUser the user that gave the command
     * @param podcasts the list of podcasts
     *
     * This method verifies if the user has played the podcast before, if not it creates a new
     * PodcastTuple object in which the relevant information is stored. If the user has played
     * the podcast before, it updates the timestamp, sets the podcast as playing and creates a
     * new PlayerTuple object that is used to update the status of the music player.
     */
    public void loadForPodcast(final String podcastName, final DatabaseUser currentUser,
                                final ArrayList<PodcastInput> podcasts, final ObjectNode node,
                               final ObjectMapper currentMapper) {
        if (!currentUser.getPlayedPodcasts().containsKey(podcastName)) {
            PodcastInput podcast = podcasts.stream()
                    .filter(p -> p.getName().equals(currentUser
                            .getDatabaseSearchbar().getSelectedItem()))
                    .toList().get(0);
            PodcastTuple newPodcastTuple = new PodcastTuple(false, getTimestamp(), podcast);
            currentUser.getPlayedPodcasts().put(podcastName, newPodcastTuple);
            currentUser.getDatabaseMusicPlayer()
                    .setCurrentStatus(new PlayerTuple(true, getTimestamp(),
                            newPodcastTuple.getTimeLeft()));

            currentUser.getDatabaseMusicPlayer().setAudioPlayingOwner(podcast.getOwner());
        } else {
            PodcastTuple playingPodcastTuple = currentUser.getPlayedPodcasts().get(podcastName);
            playingPodcastTuple.setPlayTimestamp(getTimestamp());
            playingPodcastTuple.setPaused(false);
            currentUser.getDatabaseMusicPlayer()
                    .setCurrentStatus(new PlayerTuple(true, getTimestamp(),
                            playingPodcastTuple.getTimeLeft()));
        }

        node.set("message", currentMapper.convertValue("Playback "
                + "loaded successfully.", JsonNode.class));
    }

    /**
     *
     * @param currentUser user
     * @param albums list of albums
     * @param node the output node
     * @param currentMapper object mapper
     *
     * If the loaded audio is an album, it creates a new PlayerTuple object that
     * is used to update the status of the music player.
     */

    public void loadForAlbum(final ArrayList<DatabaseAlbum> albums, final DatabaseUser currentUser,
                             final ObjectNode node, final ObjectMapper currentMapper) {
        DatabaseAlbum album = albums.stream()
                .filter((p) -> p.getName().equals(currentUser
                        .getDatabaseSearchbar().getSelectedItem()))
                .toList().get(0);
        int currentDuration = 0;
        for (DatabaseSong song : album.getSongs()) {
            currentDuration += song.getSong().getDuration();
        }

        currentUser.getDatabaseMusicPlayer().setCurrentStatus(new PlayerTuple(true,
                getTimestamp(), currentDuration));
        currentUser.getDatabaseMusicPlayer().setAudioPlayingOwner(album.getOwner());
        node.set("message", currentMapper.convertValue("Playback loaded "
                + "successfully.", JsonNode.class));
    }
}
