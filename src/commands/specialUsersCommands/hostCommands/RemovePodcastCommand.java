package commands.specialUsersCommands.hostCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.specialUsersCommands.SpecialUserCommand;
import database.Database;
import database.userData.DatabaseUser;
import database.userData.HostUser;
import fileio.input.PodcastInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class RemovePodcastCommand extends SpecialUserCommand {
    private String username;
    private String name;

    /**
     *
     * @param users list of users
     * @param hosts list of hosts
     * @param podcastInputs list of podcasts
     *
     * The method verifies if the user exists and if it is a host, then it finds if there is a
     * podcast with the name on the host's page. If there is, the canPodcastBeDeleted method is
     * called and if it returns true, then the podcast is removed from the page and the list of
     * podcasts, otherwise, an error message is shown.
     */
    public void execute(final HashMap<String, DatabaseUser> users,
                        final HashMap<String, HostUser> hosts,
                        final ArrayList<PodcastInput> podcastInputs) {

        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("removePodcast", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (!userExists(users, username, node, currentMapper)
                || !isUserHost(hosts, username, node, currentMapper)) {
            Database.outputs.add(node);
            return;
        }

        HostUser currentHost = hosts.get(username);
        PodcastInput foundPodcast = currentHost.getPage().getPodcasts()
                .stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);

        if (foundPodcast == null) {
            node.set("message", currentMapper.convertValue(username
                    + " doesn't have a podcast with the given name.", JsonNode.class));
        } else if (canPodcastBeDeleted(users, node, currentMapper)) {
            node.set("message", currentMapper.convertValue(username
                    + " deleted the podcast successfully.", JsonNode.class));

            podcastInputs.removeIf(podcastInput -> podcastInput.getName().equals(name));
            currentHost.getPage().getPodcasts().removeIf(podcast -> podcast.getName().equals(name));
        }
        Database.outputs.add(node);
    }

    /**
     *
     * @param users the users list
     * @param node the output node
     * @param currentMapper the object mapper
     * @return if the podcast can be deleted
     *
     * The method iterates through the hashmap of users and if there is any user playing the
     * podcast, then it returns false, otherwise, it returns true.
     */

    private boolean canPodcastBeDeleted(final HashMap<String, DatabaseUser> users,
                                        final ObjectNode node,
                                        final ObjectMapper currentMapper) {
        for (Map.Entry<String, DatabaseUser> entry : users.entrySet()) {
            DatabaseUser currentUser = entry.getValue();
            String audioPlaying = currentUser.getDatabaseMusicPlayer().getLoaded();
            if (audioPlaying != null && audioPlaying.equals(name)) {
                node.set("message", currentMapper.convertValue(username
                        + " can't delete this podcast.", JsonNode.class));
                return false;
            }
        }
        return true;
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
}
