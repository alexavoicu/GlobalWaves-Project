package commands.adminCommands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.Database;
import database.userData.HostUser;
import database.userData.PodcastsOutputStructure;
import fileio.input.PodcastInput;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ShowPodcastsCommand extends Command {
    private String username;

    /**
     *
     * @param hostUser the host
     *
     * The method iterates through every podcast owned by the host and adds the podcast
     * information to the results list and then puts it in the output node.
     */
    public void execute(final HostUser hostUser) {
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("showPodcasts", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        ArrayList<PodcastsOutputStructure> result = new ArrayList<>();
        for (PodcastInput podcastInput : hostUser.getPage().getPodcasts()) {
            result.add(podcastInput.storePodcastInfo());
        }
        node.set("result", currentMapper.convertValue(result, JsonNode.class));
        Database.outputs.add(node);

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }
}
