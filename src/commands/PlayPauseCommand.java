package commands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import database.Database;
import database.userData.DatabaseMusicPlayer;
import database.userData.DatabaseUser;
import database.userData.PlayerTuple;
import database.userData.PodcastTuple;

import java.util.HashMap;

import static database.userData.PodcastTuple.updatePodcastInfo;

@JsonIgnoreProperties(ignoreUnknown = true)

public final class PlayPauseCommand extends Command {
    private String username;

    /**
     *
     * @param users list of users
     *
     * If there is nothing loaded, an error message is shown.
     * Otherwise, if the loaded audio is a podcast, the podcast information is updated.
     * After, the music player's current state is updated, as well as the time left.
     */

    public void execute(final HashMap<String, DatabaseUser> users) {
        DatabaseUser currentUser = users.get(username);
        DatabaseMusicPlayer currentMusicPlayer = currentUser.getDatabaseMusicPlayer();
        String currentLoaded = currentMusicPlayer.getLoaded();
        PlayerTuple currentTuple = currentMusicPlayer.getCurrentStatus();

        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("playPause", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (currentLoaded == null) {
            node.set("message", currentMapper
                    .convertValue("Please load a source before attempting to pause or "
                            + "resume playback.", JsonNode.class));
        } else {
            if (currentMusicPlayer.getLoadedType().equals("podcast")) {
                PodcastTuple playingPodcast = currentUser.getPlayedPodcasts().get(currentLoaded);
                updatePodcastInfo(getTimestamp(), playingPodcast);
            }
            if (currentTuple.isPlaying()) {
                currentTuple.setPlaying(false);
                currentTuple.setPauseTimestamp(getTimestamp());
//                currentTuple.setTimeLeft(currentTuple.getTimeLeft() - (getTimestamp()
//                        - currentTuple.getStartTime()));
                currentTuple.setTimeLeft(currentTuple.getTimeLeft() * 2);
                node.set("message", currentMapper.convertValue("Playback paused successfully.",
                        JsonNode.class));
            } else {
                currentTuple.setPlaying(true);
                currentTuple.setStartTime(currentTuple.getStartTime()
                        + getTimestamp() - currentTuple.getPauseTimestamp());
                node.set("message", currentMapper.convertValue("Playback resumed successfully.",
                        JsonNode.class));
            }

        }
        Database.outputs.add(node);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }
}
