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
public final class PrevCommand extends Command {
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     *
     * @param user user that gave the command
     * @param playlists list of playlists
     * @param podcastInputs list of podcasts
     * @param songs list of songs
     *
     * The method verifies if there is something loaded in the player, if not, then an error
     * message is shown. Otherwise, if the loaded audio is either a playlist or a podcast,
     * their respective methods are called. If the loaded audio is a song, the method calculates
     * the remained time of the song and if the passed time from song is greater than 1
     * it updates the start time accordingly to go back to the beginning of the song.
     */

    public void execute(final DatabaseUser user, final ArrayList<DatabasePlaylist> playlists,
                        final ArrayList<PodcastInput> podcastInputs,
                        final ArrayList<DatabaseSong> songs,
                        final ArrayList<DatabaseAlbum> albums) {
        DatabaseMusicPlayer currentPlayer = user.getDatabaseMusicPlayer();
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("prev", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));
        if (currentPlayer.getLoaded() == null) {
            node.set("message", currentMapper.convertValue("Please load a source before "
                    + "returning to the previous track.", JsonNode.class));
            Database.outputs.add(node);
            return;
        }

        switch (currentPlayer.getLoadedType()) {
            case "playlist" -> {
                DatabasePlaylist playlist = new ArrayList<>(playlists.stream()
                        .filter((p) -> p.getName().equals(currentPlayer
                                .getLoaded())).toList()).get(0);
                node = prevSongCollection(playlist, node, currentMapper, user);
            }
            case "album" -> {
                DatabaseAlbum album = new ArrayList<>(albums.stream()
                        .filter((a) -> a.getName().equals(currentPlayer
                                .getLoaded())).toList()).get(0);
                node = prevSongCollection(album, node, currentMapper, user);
            }
            case "podcast" -> {
                PodcastInput podcast = new ArrayList<>(podcastInputs.stream()
                        .filter((p) -> p.getName().equals(currentPlayer
                                .getLoaded())).toList()).get(0);
                node = prevEpisodePodcast(podcast, node, currentMapper, user);
            }
            case "song" -> {
                DatabaseSong songPlaying = new ArrayList<>(songs.stream()
                        .filter((s) -> s.getSong().getName().equals(currentPlayer.getLoaded()))
                        .toList()).get(0);
                int remainedTime = currentPlayer.getCurrentStatus().isPlaying() ? currentPlayer.
                        getCurrentStatus().getTimeLeft()
                        - (getTimestamp() - currentPlayer.getCurrentStatus()
                        .getStartTime()) : currentPlayer.
                        getCurrentStatus().getTimeLeft();
                if (songPlaying.getSong().getDuration() - remainedTime >= 1) {
                    currentPlayer.getCurrentStatus().setTimeLeft(songPlaying
                            .getSong().getDuration());
                    currentPlayer.getCurrentStatus().setStartTime(getTimestamp());
                    currentPlayer.getCurrentStatus().setPlaying(true);
                }
            }
            default -> {
                return;
            }
        }
        Database.outputs.add(node);
    }

    /**
     *
     * @param songsCollection currently played collection
     * @param node  output node
     * @param currentMapper ObjectMapper
     * @param user the user giving the command
     * @return the output node
     *
     * The method calculates the time passed so that it can find the song currently playing.
     * If the song is not the first one in the list and the passed time is greater than 1,
     * the time needed to update the timestamp will be the added with the previous song duration,
     * so that the music player will begin the previous song. Then, the start time is updated
     * accordingly. The new played song is shown.
     */

    public ObjectNode prevSongCollection(final SongsCollection songsCollection,
                                         final ObjectNode node,
                                       final ObjectMapper currentMapper, final DatabaseUser user) {
        int passedTime = getTimestamp() - user.getDatabaseMusicPlayer().
                getCurrentStatus().getStartTime();

        SongsCollection currentSongsCollection = songsCollection;

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

        if (user.getDatabaseMusicPlayer().getRepeatStatus() == 2) {
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

            node.set("message", currentMapper.convertValue("Returned to previous track"
                    + " successfully. The current track is "
                    + nameOfTheSong + ".", JsonNode.class));
            user.getDatabaseMusicPlayer().getCurrentStatus().setPlaying(true);
            return node;
        }



        SongsCollectionTuple playlistSongInfo = currentSongsCollection.findSongPlaying(passedTime);
        if (playlistSongInfo != null) {
            int songPassedTime = playlistSongInfo.getPassedTimeSong();

            if (songPassedTime < 1 && !playlistSongInfo.isFirst()) {
                DatabaseSong prevSong = playlistSongInfo.getPrevSong();
                songPassedTime += prevSong.getSong().getDuration();
            }

            user.getDatabaseMusicPlayer().getCurrentStatus().
                    setStartTime(user.getDatabaseMusicPlayer().
                            getCurrentStatus().getStartTime() + songPassedTime);
            passedTime = passedTime - songPassedTime;
            SongsCollectionTuple newPlaylistSongInfo = currentSongsCollection
                    .findSongPlaying(passedTime);
            if (newPlaylistSongInfo != null) {
                DatabaseSong newSongPlaying = newPlaylistSongInfo.getSong();
                node.set("message", currentMapper.convertValue("Returned to previous track"
                                + " successfully. The current track is " + newSongPlaying.
                                getSong().getName() + ".", JsonNode.class));
                user.getDatabaseMusicPlayer().getCurrentStatus().setPlaying(true);
            }
        }
        return node;
    }

    /**
     *
     * @param podcast the currently playing podcast
     * @param node the output node
     * @param currentMapper the ObjectMapper
     * @param user the user that gave the command
     * @return the output node
     *
     * The method calculates the time passed so that it can find the episode currently playing.
     * If the episode is not the first one in the list and the passed time is greater than 1,
     * the time needed to update the timestamp will be the added with the previous episode
     * duration, so that the music player will begin the previous episode. Then, the start time
     * is updated accordingly in the users played podcasts. The new played episode is shown.
     */

    public ObjectNode prevEpisodePodcast(final PodcastInput podcast, final ObjectNode node,
                                         final ObjectMapper currentMapper,
                                         final DatabaseUser user) {
        EpisodePlayingInfo episodePlaying = user.getPlayedPodcasts()
                .get(user.getDatabaseMusicPlayer().getLoaded())
                .getPlayingEpisodeInfo(getTimestamp(), podcast);

        int passedTimeFromEpisode = episodePlaying.getTimePassed();

        PodcastTuple podcastInfo = user.getPlayedPodcasts()
                .get(user.getDatabaseMusicPlayer().getLoaded());

        if (passedTimeFromEpisode < 1 && !episodePlaying.isFirst()) {
            passedTimeFromEpisode += episodePlaying.getPrevDuration();
        }

        podcastInfo.setPassedTime(podcastInfo.getPassedTime() - passedTimeFromEpisode);
        podcastInfo.setTimeLeft(podcastInfo.getTimeLeft() + passedTimeFromEpisode);
        EpisodePlayingInfo newEpisodePlaying = podcastInfo
                .getPlayingEpisodeInfo(getTimestamp(), podcast);
        String newPlayingEpisodeName = newEpisodePlaying.getName();

        node.set("message", currentMapper.convertValue("Skipped to next track successfully."
                        + " The current track is " + newPlayingEpisodeName + " .",
                JsonNode.class));

        return node;
    }
}
