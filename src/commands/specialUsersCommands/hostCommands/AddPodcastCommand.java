package commands.specialUsersCommands.hostCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.specialUsersCommands.SpecialUserCommand;
import database.Database;
import database.userData.DatabaseUser;
import database.userData.HostUser;
import fileio.input.EpisodeInput;
import fileio.input.PodcastInput;

import java.util.ArrayList;
import java.util.HashMap;

public final class AddPodcastCommand extends SpecialUserCommand {
    private String username;
    private String name;
    private ArrayList<EpisodeInput> episodes;

    /**
     *
     * @param users list of users
     * @param hosts list of hosts
     * @param podcastInputs list of podcasts
     *
     * The method verifies if the user exists and if it is a host, then it finds if there already
     * is a podcast with the same name on the host's page. If there isn't, the method
     * doesEpisodeAppearTwice is called. If the episode doesn't appear twice then a new podcast is
     * added in the podcasts list and on the host's page. Otherwise, an error message is shown.
     */

    public void execute(final HashMap<String, DatabaseUser> users,
                        final HashMap<String, HostUser> hosts,
                        final ArrayList<PodcastInput> podcastInputs) {
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("addPodcast", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (!userExists(users, username, node, currentMapper)
                || !isUserHost(hosts, username, node, currentMapper)) {
            Database.outputs.add(node);
            return;
        }

        HostUser currentHost = hosts.get(username);
        PodcastInput podcast = currentHost.getPage().getPodcasts()
                .stream()
                .filter(podcastInput -> podcastInput.getName().equals(name))
                .findFirst()
                .orElse(null);

        if (podcast != null) {
            node.set("message", currentMapper.convertValue(username
                    + " has another podcast with the same name.", JsonNode.class));
        } else {
            if (!doesEpisodeAppearTwice(node, currentMapper)) {
                PodcastInput newPodcast = new PodcastInput(name, username, episodes);
                node.set("message", currentMapper.convertValue(username
                        + " has added new podcast successfully.", JsonNode.class));
                currentHost.getPage().getPodcasts().add(newPodcast);
                podcastInputs.add(newPodcast);
            }
        }
        Database.outputs.add(node);
    }

    /**
     *
     * @param node the output node
     * @param currentMapper the object mapper
     * @return if episode appears twice
     *
     * The method iterates through the episodes list and adds the episode name to a hashmap,
     * if the episode name was already added, then the episode appears twice and the method
     * return true, otherwise it returns false.
     */
    private boolean doesEpisodeAppearTwice(final ObjectNode node,
                                           final ObjectMapper currentMapper) {
        HashMap<String, Integer> nrOfTimesAnEpisodeAppears = new HashMap<>();
        for (EpisodeInput episode : episodes) {
            if (nrOfTimesAnEpisodeAppears.containsKey(episode.getName())) {
                node.set("message", currentMapper.convertValue(username
                        + "has the same episode in this podcast.", JsonNode.class));
                return true;
            }
            nrOfTimesAnEpisodeAppears.put(episode.getName(), 1);
        }
        return false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public ArrayList<EpisodeInput> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(final ArrayList<EpisodeInput> episodes) {
        this.episodes = episodes;
    }
}
