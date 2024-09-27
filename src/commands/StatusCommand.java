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
import database.userData.RepeatSongsCollectionTuple;
import database.userData.SongsCollectionTuple;
import fileio.input.PodcastInput;
import java.util.ArrayList;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class StatusCommand extends Command {
    private String username;

    /**
     *
     * @param users list of users
     * @param podcastInputs list of podcasts
     * @param playlists list of playlists
     *
     * The method first verifies if there is anything loaded in the player, if there isn't,
     * the output will show a remain time of 0. If the loaded audio is a song, then the remained
     * time is calculated and the output is set. If the loaded audio is either a playlist or a
     * podcast their respective methods are called and the stats are set.
     */

    public void execute(final HashMap<String, DatabaseUser> users,
                        final ArrayList<PodcastInput> podcastInputs,
                        final ArrayList<DatabasePlaylist> playlists,
                        final ArrayList<DatabaseAlbum> albums,
                        final ArrayList<DatabaseSong> songs) {
        DatabaseUser currentUser = users.get(username);
        DatabaseMusicPlayer currentPlayer = currentUser.getDatabaseMusicPlayer();
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("status", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));
        ObjectNode stats = currentMapper.createObjectNode();

        if (currentUser.getDatabaseMusicPlayer().getLoaded() == null) {
            setDefaultStats(stats, currentMapper);
            node.set("stats", currentMapper.convertValue(stats, JsonNode.class));
            Database.outputs.add(node);
            return;
        }

        if (currentPlayer.getLoadedType().equals("song")) {
            int remainedTime = currentPlayer.getCurrentStatus().isPlaying() ? currentPlayer
                    .getCurrentStatus().getTimeLeft()
                    - (getTimestamp() - currentPlayer.getCurrentStatus()
                    .getStartTime()) : currentPlayer.getCurrentStatus().getTimeLeft();
            if (!currentUser.isOnline()) {
                remainedTime = currentPlayer
                        .getCurrentStatus().getTimeLeft() - (currentUser
                        .getSwitchToOfflineTimestamp() - currentPlayer.getCurrentStatus()
                        .getStartTime());
            }
            if (currentPlayer.getRepeatStatus() != 0) {
                 remainedTime = remainedTimeForRepeatedSong(songs, currentPlayer);
            }

            stats.set("remainedTime", currentMapper.convertValue(remainedTime,
                    JsonNode.class));
            stats.set("name", currentMapper.convertValue(currentPlayer.getLoaded(),
                    JsonNode.class));
        } else if (currentPlayer.getLoadedType().equals("playlist")) {
            DatabasePlaylist playlist = new ArrayList<>(playlists.stream()
                    .filter((p) -> p.getName().equals(currentPlayer.getLoaded()))
                    .toList()).get(0);
            stats = statusForSongCollection(currentMapper, playlist,
                    currentUser, currentPlayer.getLoaded());
        } else if (currentPlayer.getLoadedType().equals("album")) {
            DatabaseAlbum album = new ArrayList<>(albums.stream()
                    .filter((a) -> a.getName().equals(currentPlayer.getLoaded()))
                    .toList()).get(0);
            stats = statusForSongCollection(currentMapper, album,
                    currentUser, currentPlayer.getLoaded());
        } else if (currentPlayer.getLoadedType().equals("podcast")) {
            stats = statusForPodcast(currentMapper, currentUser, podcastInputs);
        }

        stats.set("repeat", currentMapper
                .convertValue(getRepeatValue(currentPlayer), JsonNode.class));
        stats.set("shuffle", currentMapper.convertValue(currentPlayer.isShuffled(),
                JsonNode.class));
        stats.set("paused", currentMapper.convertValue(!currentPlayer.getCurrentStatus()
                .isPlaying(), JsonNode.class));
        node.set("stats", currentMapper.convertValue(stats, JsonNode.class));

        Database.outputs.add(node);
    }

    /**
     *
     * @param songs list of songs
     * @param player the music player
     * @return remained time for a repeated song
     *
     * The method finds the song and calculates how much time is left, based on how many
     * times the song was played since start time.
     */
    public int remainedTimeForRepeatedSong(final ArrayList<DatabaseSong> songs,
                                           final DatabaseMusicPlayer player) {
        DatabaseSong song = new ArrayList<>(songs.stream()
                .filter((s) -> s.getSong().getName().equals(player.getLoaded())).toList()).get(0);

        int startTime = player.getCurrentStatus().getStartTime();
        int timePassed = getTimestamp() - startTime;
        int howManyTimesSongWasPlayed = timePassed / song.getSong().getDuration();
        return song.getSong().getDuration() * (howManyTimesSongWasPlayed + 1) - timePassed;
    }

    /**
     *
     * @param currentMapper the Object Mapper
     * @param audioName the name of the audio collection playing
     * @param user the user that gave the command
     * @param songsCollection the collection playing
     * @return the output node
     *
     * The method finds the playlist and calculates the time passed so that these arguments
     * can be passed to the method that returns the song that is currently playing and the time
     * left from that song. If the playlist is shuffled, then the passed time is modified to
     * reflect the passed time from before shuffling, and the playlist becomes its shuffled
     * equivalent from the original playlist. If the repeat status is repeat current song, then it
     * gets the song that is repeated from the user's repeat collection info and the passed time
     * since that song started playing. It calculates the number of times the song has played
     * and thus finds how much time is left from this playing.
     * The status is set based on the result of the method.
     */

    public ObjectNode statusForSongCollection(final ObjectMapper currentMapper,
                                              final SongsCollection songsCollection,
                                              final DatabaseUser user,
                                              final String audioName) {
        SongsCollection finalSongCollection = songsCollection;
        ObjectNode stats = currentMapper.createObjectNode();
        int passedTime = getTimestamp() - user.getDatabaseMusicPlayer()
                .getCurrentStatus().getStartTime();

        if (!user.isOnline()) {
            passedTime = user.getSwitchToOfflineTimestamp() - user.getDatabaseMusicPlayer()
                    .getCurrentStatus().getStartTime();
        }

        if (!user.getDatabaseMusicPlayer().getCurrentStatus().isPlaying()) {
            passedTime = user.getDatabaseMusicPlayer().getCurrentStatus().getPauseTimestamp()
                    - user.getDatabaseMusicPlayer().getCurrentStatus().getStartTime();
        }

        if (user.getDatabaseMusicPlayer().isShuffled()) {
            finalSongCollection = user.getShuffledSongsCollections().get(audioName);
            if (user.getSongsCollectionPassedTimeBeforeShuffle().containsKey(audioName)) {
                passedTime = passedTime
                        - user.getSongsCollectionPassedTimeBeforeShuffle().get(audioName);
            } else {
                return stats;
            }

        }

        if (user.getDatabaseMusicPlayer().getRepeatStatus() == 2) {
            RepeatSongsCollectionTuple songsCollectionRepeatInfo = user
                    .getSongsCollectionRepeatInfo().get(audioName);
            int passedTimeFromSongStarting = getTimestamp() - songsCollectionRepeatInfo
                    .getTimestampWhenSongStartedPlaying();
            String nameOfTheSong = songsCollectionRepeatInfo.getRepeatedSong().getSong().getName();
            int durationOfSong = songsCollectionRepeatInfo.getRepeatedSong()
                    .getSong().getDuration();
            int nrOfTimesSongPlayed = passedTimeFromSongStarting / durationOfSong;
            int timeLeft = durationOfSong - passedTimeFromSongStarting
                    + nrOfTimesSongPlayed * durationOfSong;

            stats.set("remainedTime", currentMapper.convertValue(timeLeft, JsonNode.class));
            stats.set("name", currentMapper.convertValue(nameOfTheSong, JsonNode.class));
            return stats;
        }

//        if (user.getDatabaseMusicPlayer().getRepeatStatus() == 1) {
//            int nrOfTimesCollectionPlayed = passedTime / songsCollection
//                    .getTotalCollectionDuration();
//            passedTime = passedTime - nrOfTimesCollectionPlayed * songsCollection
//                    .getTotalCollectionDuration();
//        }

        SongsCollectionTuple playlistSongInfo = finalSongCollection.findSongPlaying(passedTime);
        if (playlistSongInfo != null) {
            DatabaseSong songPlaying = playlistSongInfo.getSong();
            int remainedTime = playlistSongInfo.getTimeLeftSong();
            String finalSongPlaying = songPlaying.getSong().getName();
            stats.set("remainedTime", currentMapper.convertValue(remainedTime, JsonNode.class));
            stats.set("name", currentMapper.convertValue(finalSongPlaying, JsonNode.class));
        }
        return stats;
    }

    /**
     *
     * @param currentMapper the Object Mapper
     * @param user the user that gave the command
     * @param podcastInputs the list of podcasts
     * @return
     *
     * The method finds the podcast and calculates the time passed so that these arguments
     * can be passed to the method that returns the episode that is currently playing and the time
     * left from that episode. The status is set based on the result of the method.
     */

    public ObjectNode statusForPodcast(final ObjectMapper currentMapper,
                                        final DatabaseUser user,
                                        final ArrayList<PodcastInput> podcastInputs) {
        ObjectNode stats = currentMapper.createObjectNode();
        DatabaseMusicPlayer player = user.getDatabaseMusicPlayer();
        PodcastInput playingPodcast = new ArrayList<>(podcastInputs.stream()
                .filter((podcast) -> podcast.getName().equals(player.getLoaded()))
                .toList()).get(0);
        EpisodePlayingInfo episodePlaying = user.getPlayedPodcasts()
                .get(player.getLoaded()).getPlayingEpisodeInfo(getTimestamp(), playingPodcast);

        stats.set("remainedTime", currentMapper.convertValue(episodePlaying.getTimeLeft(),
                JsonNode.class));
        stats.set("name", currentMapper.convertValue(episodePlaying.getName(), JsonNode.class));
        return stats;
    }

    /**
     *
     * @param currentPlayer the music player
     * @return the repeat value
     *
     * The repeat status is checked, based on the loaded audio and returned.
     */
    private String getRepeatValue(final DatabaseMusicPlayer currentPlayer) {
        String repeatValue;
        if (currentPlayer.getLoadedType().equals("podcast")
                || currentPlayer.getRepeatStatus() == 0) {
            repeatValue = "No Repeat";
        } else if (currentPlayer.getRepeatStatus() == 1) {
            repeatValue = currentPlayer.getLoadedType().equals("playlist")
                    ? "Repeat All" : "Repeat Once";
        } else {
            repeatValue = currentPlayer.getLoadedType().equals("playlist")
                    ? "Repeat Current Song" : "Repeat Infinite";
        }
        return repeatValue;
    }

    /**
     *
     * @param stats the stats node
     * @param currentMapper the object mapper
     *
     * The method sets the default stats for an empty player.
     */
    private void setDefaultStats(final ObjectNode stats, final ObjectMapper currentMapper) {
        stats.set("remainedTime", currentMapper.convertValue(0, JsonNode.class));
        stats.set("name", currentMapper.convertValue("", JsonNode.class));
        stats.set("repeat", currentMapper.convertValue("No Repeat", JsonNode.class));
        stats.set("shuffle", currentMapper.convertValue(false, JsonNode.class));
        stats.set("paused", currentMapper.convertValue(true, JsonNode.class));
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

}
