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
public final class ForwardCommand extends Command {
    private String username;
    private final int forwardValue = 90;

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     *
     * @param user the user giving the command
     * @param podcastInputs the podcasts list
     *
     * The method verifies if there is anything loaded in the player and the loaded source is a
     * podcast, if the conditions are not met, then an error message is shown. Otherwise, it calls
     * the forwardPodcast method.
     */

    public void execute(final DatabaseUser user, final ArrayList<PodcastInput> podcastInputs) {

        DatabaseMusicPlayer currentPlayer = user.getDatabaseMusicPlayer();
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("forward", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));
        if (currentPlayer.getLoaded() == null) {
            node.set("message", currentMapper.convertValue("Please load a source "
                            + "before attempting to forward.",
                    JsonNode.class));
        } else if (currentPlayer.getLoadedType().equals("podcast")) {
            PodcastInput podcast = new ArrayList<>(podcastInputs.stream()
                    .filter((p) -> p.getName().equals(currentPlayer.getLoaded())).toList()).get(0);
            node = forwardPodcast(podcast, node, currentMapper, user);
        } else {
            node.set("message", currentMapper.convertValue("The loaded source is not a podcast.",
                    JsonNode.class));
        }
        Database.outputs.add(node);
    }

    /**
     *
     * @param podcast the playing podcast
     * @param node output node
     * @param currentMapper the current mapper
     * @param user the user giving the command
     * @return the output node
     *
     * The method gets the time to the next episode, then it adjusts the passed and remaining
     * time based on the remained time to the next episode, if the value is lower than forward
     * value, then the next episode starts, otherwise it skips the forward value time.
     */
    public ObjectNode forwardPodcast(final PodcastInput podcast, final ObjectNode node,
                                         final ObjectMapper currentMapper,
                                     final DatabaseUser user) {
        EpisodePlayingInfo episodePlaying = user.getPlayedPodcasts()
                .get(user.getDatabaseMusicPlayer().getLoaded())
                .getPlayingEpisodeInfo(getTimestamp(), podcast);
        int timeToNextEpisode = episodePlaying.getTimeLeft();
        PodcastTuple podcastInfo = user.getPlayedPodcasts()
                .get(user.getDatabaseMusicPlayer().getLoaded());

        if (timeToNextEpisode < forwardValue) {
            podcastInfo.setPassedTime(podcastInfo.getPassedTime() + timeToNextEpisode);
            podcastInfo.setTimeLeft(podcastInfo.getTimeLeft() - timeToNextEpisode);
        } else {
            podcastInfo.setPassedTime(podcastInfo.getPassedTime() + forwardValue);
            podcastInfo.setTimeLeft(podcastInfo.getTimeLeft() - forwardValue);
        }

        node.set("message", currentMapper.convertValue("Skipped forward successfully.",
                JsonNode.class));

        return node;
    }
}
