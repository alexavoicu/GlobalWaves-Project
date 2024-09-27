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
import database.userData.EpisodePlayingInfo;
import database.userData.PodcastTuple;
import database.userData.RepeatSongsCollectionTuple;
import database.userData.SongsCollectionTuple;
import fileio.input.PodcastInput;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class NextCommand extends Command {
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     *
     * @param user the user
     * @param playlists list of playlists
     * @param podcastInputs list of podcasts
     *
     * The method first verifies if the music player is loaded, if it's not, an error message is
     * shown.
     * If the loaded audio is a playlist or a podcast, then it calls their respective methods.
     */

    public void execute(final DatabaseUser user, final ArrayList<DatabasePlaylist> playlists,
                        final ArrayList<PodcastInput> podcastInputs,
                        final ArrayList<DatabaseAlbum> albums) {
        DatabaseMusicPlayer currentPlayer = user.getDatabaseMusicPlayer();
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("next", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));
        if (currentPlayer.getLoaded() == null) {
            node.set("message", currentMapper.convertValue("Please load a source before skipping"
                            + " to the next track.",
                    JsonNode.class));
        } else if (currentPlayer.getLoadedType().equals("playlist")) {
            DatabasePlaylist playlist = new ArrayList<>(playlists.stream()
                    .filter((p) -> p.getName().equals(currentPlayer.getLoaded())).toList()).get(0);
            node = nextSongsCollection(playlist, node, currentMapper, user);
        } else if (currentPlayer.getLoadedType().equals("album")) {
            DatabaseAlbum album = new ArrayList<>(albums.stream()
                    .filter((a) -> a.getName().equals(currentPlayer.getLoaded())).toList()).get(0);
            node = nextSongsCollection(album, node, currentMapper, user);
        } else if (currentPlayer.getLoadedType().equals("podcast")) {
            PodcastInput podcast = new ArrayList<>(podcastInputs.stream()
                    .filter((p) -> p.getName().equals(currentPlayer.getLoaded())).toList()).get(0);
            node = nextEpisodePodcast(podcast, node, currentMapper, user);
        }
        Database.outputs.add(node);
    }

    /**
     *
     * @param songsCollection the collection currently played
     * @param node the output node
     * @param currentMapper the objectMapper
     * @param user the user giving the command
     * @return the output node
     *
     * This method finds the song that is currently played and the time left from it, then it
     * updates the player start time, so that it skips over the time left. After that the next
     * song playing is shown.
     */
    public ObjectNode nextSongsCollection(final SongsCollection songsCollection,
                                          final ObjectNode node,
                                          final ObjectMapper currentMapper,
                                          final DatabaseUser user) {

        SongsCollection currentSongsCollection = songsCollection;
        int passedTime = getTimestamp() - user.getDatabaseMusicPlayer()
                .getCurrentStatus().getStartTime();

        if (!user.getDatabaseMusicPlayer().getCurrentStatus().isPlaying()) {
            passedTime = user.getDatabaseMusicPlayer().getCurrentStatus().getPauseTimestamp()
                    - user.getDatabaseMusicPlayer().getCurrentStatus().getStartTime();

            int startTime = user.getDatabaseMusicPlayer().getCurrentStatus().getStartTime();
            user.getDatabaseMusicPlayer().getCurrentStatus().setStartTime(startTime
                    + getTimestamp() - user.getDatabaseMusicPlayer().getCurrentStatus()
                    .getPauseTimestamp());
        }

        if (user.getDatabaseMusicPlayer().isShuffled()) {
            currentSongsCollection = user.getShuffledSongsCollections()
                    .get(user.getDatabaseMusicPlayer().getLoaded());
            if (user.getSongsCollectionPassedTimeBeforeShuffle()
                    .containsKey(user.getDatabaseMusicPlayer().getLoaded())) {
                passedTime = passedTime - user
                        .getSongsCollectionPassedTimeBeforeShuffle()
                        .get(user.getDatabaseMusicPlayer().getLoaded());
            }
            if (currentSongsCollection == null) {
                return node;
            }
        }

        if (user.getDatabaseMusicPlayer().getRepeatStatus() == 2 && user
                .getSongsCollectionRepeatInfo().containsKey(songsCollection.getName())) {
            RepeatSongsCollectionTuple songsCollectionRepeatInfo = user
                    .getSongsCollectionRepeatInfo().get(currentSongsCollection.getName());
            String nameOfTheSong = songsCollectionRepeatInfo.getRepeatedSong().getSong().getName();
            int passedTimeFromSongStarting = getTimestamp() - songsCollectionRepeatInfo
                    .getTimestampWhenSongStartedPlaying();
            int durationOfSong = songsCollectionRepeatInfo.getRepeatedSong()
                    .getSong().getDuration();
            int nrOfTimesSongPlayed = passedTimeFromSongStarting / durationOfSong;
            int timeLeft = durationOfSong - passedTimeFromSongStarting
                    + nrOfTimesSongPlayed * durationOfSong;
            int passedTimeFromThisPlaying = durationOfSong - timeLeft;
            user.getDatabaseMusicPlayer().getCurrentStatus().
                    setStartTime(user.getDatabaseMusicPlayer().
                            getCurrentStatus().getStartTime() - passedTimeFromThisPlaying);
            int startRepeat = user.getSongsCollectionRepeatInfo()
                    .get(songsCollection.getName()).getTimestampWhenSongStartedPlaying();
            user.getSongsCollectionRepeatInfo().remove(songsCollection.getName());
            user.getSongsCollectionRepeatInfo().put(songsCollection.getName(),
                    new RepeatSongsCollectionTuple(songsCollectionRepeatInfo.getRepeatedSong(),
                            startRepeat + passedTimeFromThisPlaying));

            node.set("message", currentMapper.convertValue("Skipped to next track"
                    + " successfully. The current track is "
                    + nameOfTheSong + ".", JsonNode.class));
            user.getDatabaseMusicPlayer().getCurrentStatus().setPlaying(true);
            return node;
        }

        SongsCollectionTuple collectionSongInfo = currentSongsCollection
                .findSongPlaying(passedTime);
        if (collectionSongInfo != null) {
            int remainedTime = collectionSongInfo.getTimeLeftSong();
            user.getDatabaseMusicPlayer().getCurrentStatus().
                    setStartTime(user.getDatabaseMusicPlayer().
                            getCurrentStatus().getStartTime() - remainedTime);
            passedTime = passedTime + remainedTime;
            SongsCollectionTuple newPlaylistSongInfo = currentSongsCollection
                    .findSongPlaying(passedTime);
            if (newPlaylistSongInfo != null) {

                DatabaseSong newSongPlaying = newPlaylistSongInfo.getSong();
                node.set("message", currentMapper.convertValue("Skipped to next track "
                                + "successfully. The current track is "
                                + newSongPlaying.getSong().getName() + ".",
                        JsonNode.class));
            } else {
                node.set("message", currentMapper.convertValue("Please load a source"
                                + " before skipping to the next track.",
                        JsonNode.class));
            }
        }
        user.getDatabaseMusicPlayer().getCurrentStatus().setPlaying(true);

        return node;
    }

    /**
     *
     * @param podcast the currently played podcast
     * @param node the output node
     * @param currentMapper the ObjectMapper
     * @param user the user giving the command
     * @return the output node
     *
     * The method first finds the episode that is currently played, the time left from the episode
     * and updates the start time and time left of the podcast in the users played podcasts, so
     * that it skips over the left time from the episode. After that the next episode playing
     * is shown.
     */
    public ObjectNode nextEpisodePodcast(final PodcastInput podcast, final ObjectNode node,
                                         final ObjectMapper currentMapper,
                                         final DatabaseUser user) {
        EpisodePlayingInfo episodePlaying = user.getPlayedPodcasts()
                .get(user.getDatabaseMusicPlayer().getLoaded()).
                getPlayingEpisodeInfo(getTimestamp(), podcast);
        int timeToNextEpisode = episodePlaying.getTimeLeft();
        PodcastTuple podcastInfo = user.getPlayedPodcasts()
                .get(user.getDatabaseMusicPlayer().getLoaded());
        podcastInfo.setPassedTime(podcastInfo.getPassedTime() + timeToNextEpisode);
        podcastInfo.setTimeLeft(podcastInfo.getTimeLeft() - timeToNextEpisode);
        EpisodePlayingInfo newEpisodePlaying = podcastInfo
                .getPlayingEpisodeInfo(getTimestamp(), podcast);
        String newPlayingEpisodeName = newEpisodePlaying.getName();

        node.set("message", currentMapper.convertValue("Skipped to next track successfully."
                        + " The current track is " + newPlayingEpisodeName + ".",
                JsonNode.class));

        return node;
    }

}
