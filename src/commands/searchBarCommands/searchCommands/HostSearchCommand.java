package commands.searchBarCommands.searchCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.searchBarCommands.searchCommands.searchBarFilters.HostSearchFilter;
import database.Database;
import database.userData.DatabaseUser;
import database.userData.HostUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import static database.userData.PodcastTuple.updatePodcastInfo;

public final class HostSearchCommand extends SearchCommand {
    private String username;
    private HostSearchFilter filters;
    private final int maxSizeSearch = 5;

    /**
     *
     * @param hosts list of hosts
     * @param databaseUser the user
     * The method iterates through the list of hosts and using the filters it finds the top 5
     * matches. Then the users searchbar is updated with the results.
     */

    public void execute(final HashMap<String, HostUser> hosts, final DatabaseUser databaseUser) {
        if (databaseUser.getDatabaseMusicPlayer().getLoaded() != null
                && databaseUser.getDatabaseMusicPlayer().getLoadedType().equals("podcast")) {
            updatePodcastInfo(getTimestamp(), databaseUser.
                    getPlayedPodcasts().get(databaseUser
                            .getDatabaseMusicPlayer().getLoaded()));
        }

        ArrayList<String> filteredList = hosts.keySet().stream()
                .filter(hostUser -> hostUser.startsWith(filters.getName()))
                .limit(maxSizeSearch)
                .collect(Collectors.toCollection(ArrayList::new));
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("search", JsonNode.class));
        node.set("user", currentMapper.convertValue(databaseUser.getUserInput()
                .getUsername(), JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));
        node.set("message", currentMapper.convertValue("Search returned "
                + filteredList.size() + " results", JsonNode.class));
        node.set("results", currentMapper.convertValue(filteredList, JsonNode.class));

        if (databaseUser.isOnline()) {
            databaseUser.getDatabaseSearchbar().setSelectedItem(null);
            databaseUser.getDatabaseSearchbar().setSearchResults(filteredList);
            databaseUser.getDatabaseSearchbar().setType("host");
            databaseUser.getDatabaseSearchbar().setSearchSelectedType("host");
        } else {
            node.set("message", currentMapper.convertValue(username
                    + " is offline.", JsonNode.class));
            node.set("results", currentMapper.convertValue(new ArrayList<>(), JsonNode.class));
        }

        Database.outputs.add(node);
        databaseUser.getDatabaseMusicPlayer().setLoaded(null);
        databaseUser.getDatabaseMusicPlayer().setLoadedType(null);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public HostSearchFilter getFilters() {
        return filters;
    }

    public void setFilters(final HostSearchFilter filters) {
        this.filters = filters;
    }
}
