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

@JsonIgnoreProperties(ignoreUnknown = true)
public final class RepeatCommand extends Command {
    private String username;
    private final int highEnoughRepeatTimes = 500;


    /**
     *
     * @param user the user
     * @param songs the list of songs
     * @param albums the list of albums
     * @param playlists the list of playlists
     *
     * The method verifies if there is something loaded in the player, and based on the type of
     * the audio, it calls the respective method.
     */
    public void execute(final DatabaseUser user, final ArrayList<DatabaseSong> songs,
                        final ArrayList<DatabaseAlbum> albums,
                        final ArrayList<DatabasePlaylist> playlists) {
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("repeat", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (user.getDatabaseMusicPlayer().getLoaded() == null) {
            node.set("message", currentMapper.convertValue("Please load a source before"
                    + " setting the repeat status.", JsonNode.class));
            Database.outputs.add(node);
            return;
        }

        int repeatStatus = user.getDatabaseMusicPlayer().getRepeatStatus();
        if (user.getDatabaseMusicPlayer().getLoadedType().equals("song")) {
            setRepeatForSong(user.getDatabaseMusicPlayer(), repeatStatus,
                    currentMapper, node, songs);
        }

        if (user.getDatabaseMusicPlayer().getLoadedType().equals("playlist")) {
            DatabasePlaylist playlist = playlists.stream()
                    .filter(p -> p.getName().equals(user.getDatabaseMusicPlayer().getLoaded()))
                    .findFirst()
                    .orElse(null);
            if (playlist != null) {
                setRepeatForSongsCollection(user,
                        repeatStatus, currentMapper, node, playlist);
            }
        }

        if (user.getDatabaseMusicPlayer().getLoadedType().equals("album")) {
            DatabaseAlbum album = new ArrayList<>(albums.stream()
                    .filter((p) -> p.getName().equals(user
                            .getDatabaseMusicPlayer().getLoaded())).toList()).get(0);
            setRepeatForSongsCollection(user,
                    repeatStatus, currentMapper, node, album);
        }

        Database.outputs.add(node);
    }

    /**
     *
     * @param player the users' music player
     * @param status the current repeat status
     * @param currentMapper the object mapper
     * @param node the output node
     * @param songs the list of songs
     *
     * The method treats each case of the status: if there is no repeat, then the time left for the
     * song is increased and the repeat status is set.
     * If the status is repeat once, then the time left is increased and the repeat status is
     * changed.
     * If the status is repeat infinite, then it finds how many times the song was played
     * and the start time is updated so that it simulates the song being only played
     * once, right now, the time left is updated and the repeat status is set.
     */

    public void setRepeatForSong(final DatabaseMusicPlayer player, final int status,
                                 final ObjectMapper currentMapper, final ObjectNode node,
                                 final ArrayList<DatabaseSong> songs) {

        DatabaseSong song = new ArrayList<>(songs.stream()
                .filter((s) -> s.getSong().getName().equals(player.getLoaded())).toList()).get(0);

        if (status == 0) {
            int timeLeft = player.getCurrentStatus().getTimeLeft();
            player.getCurrentStatus().setTimeLeft(timeLeft + song.getSong().getDuration());
            player.setRepeatStatus(1);
            node.set("message", currentMapper.convertValue("Repeat mode changed"
                    + " to repeat once.", JsonNode.class));
        }

        if (status == 1) {
            int timeLeft = player.getCurrentStatus().getTimeLeft();
            player.getCurrentStatus().setTimeLeft(highEnoughRepeatTimes * timeLeft);
            player.setRepeatStatus(2);
            node.set("message", currentMapper.convertValue("Repeat mode changed"
                    + " to repeat infinite.", JsonNode.class));
        }

        if (status == 2) {
            int startTime = player.getCurrentStatus().getStartTime();
            int timePassed = getTimestamp() - startTime;
            int howManyTimesSongWasPlayed = timePassed / song.getSong().getDuration();
            player.getCurrentStatus().setStartTime(startTime
                    + howManyTimesSongWasPlayed * song.getSong().getDuration());
            player.getCurrentStatus().setTimeLeft(song.getSong().getDuration());
            node.set("message", currentMapper.convertValue("Repeat mode changed"
                    + " to no repeat.", JsonNode.class));
        }
    }

    /**
     *
     * @param user the user
     * @param status the repeat status
     * @param currentMapper the object mapper
     * @param node the output node
     * @param songsCollection the collection of songs
     *
     * The method treats every repeat status:
     * If the repeat status is no repeat, then the time left for the song is increased and the
     * repeat status is set.
     * If the repeat status is repeat all, then it finds how many times the collection was played,
     * finds the song currently playing and updated the start time so that the collection was
     * only played once. The time left is increased and the repeat status is set.
     * If the status is repeat all, then it finds how many times the collection was played
     * and the start time is updated so that it simulates the collection being played only
     * once, right now, the time left is updated and the repeat status is set.
     */

    public void setRepeatForSongsCollection(final DatabaseUser user, final int status,
                                            final ObjectMapper currentMapper,
                                            final ObjectNode node,
                                            final SongsCollection songsCollection) {
        DatabaseMusicPlayer player = user.getDatabaseMusicPlayer();

        if (status == 0) {
            player.getCurrentStatus().setTimeLeft(highEnoughRepeatTimes * songsCollection
                    .getTotalCollectionDuration());
            player.setRepeatStatus(1);
            node.set("message", currentMapper.convertValue("Repeat mode changed"
                    + " to repeat all.", JsonNode.class));
        }
        if (status == 1) {
            int passedTime = getTimestamp() - player.getCurrentStatus().getStartTime();
            int nrOfTimesSongsCollectionPlayed = passedTime / songsCollection
                    .getTotalCollectionDuration();
            int passedTimeFromSongCollectionInThisPlaying = passedTime
                    - nrOfTimesSongsCollectionPlayed * songsCollection
                    .getTotalCollectionDuration();
            SongsCollectionTuple songCollectionInfo = songsCollection
                    .findSongPlaying(passedTimeFromSongCollectionInThisPlaying);
            DatabaseSong songPlaying = songCollectionInfo.getSong();
            int timestampWhenSongStartedPlaying = getTimestamp() - songCollectionInfo
                    .getPassedTimeSong();
            user.getSongsCollectionRepeatInfo().put(songsCollection.getName(),
                    new RepeatSongsCollectionTuple(songPlaying, timestampWhenSongStartedPlaying));

            int timeLeft = player.getCurrentStatus().getTimeLeft();
            int startTime = getTimestamp() - passedTimeFromSongCollectionInThisPlaying;
            player.getCurrentStatus().setStartTime(startTime);
            player.getCurrentStatus().setTimeLeft(timeLeft + highEnoughRepeatTimes * songPlaying.
                    getSong().getDuration());
            player.setRepeatStatus(2);
            node.set("message", currentMapper.convertValue("Repeat mode changed"
                    + " to repeat current song.", JsonNode.class));
        }
        if (status == 2) {
            RepeatSongsCollectionTuple songsCollectionRepeatInfo = user
                    .getSongsCollectionRepeatInfo().get(songsCollection.getName());
            int passedTimeFromSongStarting = getTimestamp() - songsCollectionRepeatInfo
                    .getTimestampWhenSongStartedPlaying();
            int durationOfSong = songsCollectionRepeatInfo
                    .getRepeatedSong().getSong().getDuration();
            int nrOfTimesSongPlayed = passedTimeFromSongStarting / durationOfSong;
            int startTimeOffset = nrOfTimesSongPlayed * durationOfSong;

            player.getCurrentStatus().setStartTime(player.getCurrentStatus()
                    .getStartTime() + startTimeOffset);
            player.setRepeatStatus(0);
            player.getCurrentStatus().setTimeLeft(songsCollection.getTotalCollectionDuration());
            user.getSongsCollectionRepeatInfo().remove(songsCollection.getName());

            node.set("message", currentMapper.convertValue("Repeat mode changed"
                    + " to no repeat.", JsonNode.class));
        }

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }




}
