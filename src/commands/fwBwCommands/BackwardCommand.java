package commands.fwBwCommands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.Database;
import database.userData.DatabaseMusicPlayer;
import database.userData.DatabaseUser;
import database.userData.EpisodePlayingInfo;
import database.userData.PodcastTuple;
import fileio.input.PodcastInput;
import java.util.ArrayList;


@JsonIgnoreProperties(ignoreUnknown = true)
public final class BackwardCommand extends Command {
    private final String username;
    private final int backwardValue = 90;
    public BackwardCommand() {
        this.username = null;
    }

    public BackwardCommand(final String username) {
        this.username = username;
    }


    public String getUsername() {
        return username;
    }

    /**
     *
     * @param user the user that gives the command
     * @param podcastInputs podcast list
     * The method verifies if there is anything loaded in the player and the loaded source is a
     * podcast, if the conditions are not met, then an error message is shown. Otherwise, it calls
     * the backwardPodcast method.
     */

    public void execute(final DatabaseUser user, final ArrayList<PodcastInput> podcastInputs) {
        DatabaseMusicPlayer currentPlayer = user.getDatabaseMusicPlayer();
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("backward", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));
        if (currentPlayer.getLoaded() == null) {
            node.set("message", currentMapper.convertValue("Please select a source "
                            + "before rewinding.",
                    JsonNode.class));
        } else if (currentPlayer.getLoadedType().equals("podcast")) {
            PodcastInput podcast = new ArrayList<>(podcastInputs.stream()
                    .filter((p) -> p.getName().equals(currentPlayer.getLoaded())).toList()).get(0);
            node = backwardPodcast(podcast, node, currentMapper, user);
        } else {
            node.set("message", currentMapper.convertValue("The loaded source is not a podcast.",
                    JsonNode.class));
        }
        Database.outputs.add(node);
    }

    /**
     *
     * @param podcast the playing podcast
     * @param node the node for the output
     * @param currentMapper the objectMapper used for the output
     * @param user the user giving the command
     * @return the ouput node
     * The method gets the time to the next episode, then it adjusts the passed and remaining
     * time based on the passed time from the playing episode, if the value is lower than backward
     * value, then the previous episode starts, otherwise it rewinds the backward value time.
     */

    public ObjectNode backwardPodcast(final PodcastInput podcast, final ObjectNode node,
                                     final ObjectMapper currentMapper, final DatabaseUser user) {
        EpisodePlayingInfo episodePlaying = user.getPlayedPodcasts()
                .get(user.getDatabaseMusicPlayer().getLoaded())
                .getPlayingEpisodeInfo(getTimestamp(), podcast);
        int passedTimeFromEpisode = episodePlaying.getTimePassed();

        PodcastTuple podcastInfo = user.getPlayedPodcasts()
                .get(user.getDatabaseMusicPlayer().getLoaded());

        if (passedTimeFromEpisode < backwardValue) {
            podcastInfo.setPassedTime(podcastInfo.getPassedTime() - passedTimeFromEpisode);
            podcastInfo.setTimeLeft(podcastInfo.getTimeLeft() + passedTimeFromEpisode);
        } else {
            podcastInfo.setPassedTime(podcastInfo.getPassedTime() - backwardValue);
            podcastInfo.setTimeLeft(podcastInfo.getTimeLeft() + backwardValue);
        }

        node.set("message", currentMapper.convertValue("Rewound successfully.",
                JsonNode.class));

        return node;
    }

}
