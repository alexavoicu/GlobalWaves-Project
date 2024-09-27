package commands.searchBarCommands.searchCommands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.searchBarCommands.searchCommands.searchBarFilters.PodcastFilters;
import database.Database;
import database.userData.DatabaseUser;
import fileio.input.PodcastInput;
import java.util.ArrayList;
import static database.userData.PodcastTuple.updatePodcastInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class PodcastSearchCommand extends SearchCommand {
    private final int maxSizeSearch = 5;
    private String username;
    private PodcastFilters filters;

    /**
     *
     * @param podcasts list of podcasts
     * @param databaseUser user giving the command
     * The method iterates through the list of podcasts and using the filters it finds the top 5
     * matches. Then the users searchbar is updated with the results.
     */

    public void execute(final ArrayList<PodcastInput> podcasts, final DatabaseUser databaseUser) {
        if (databaseUser.getDatabaseMusicPlayer().getLoaded() != null
                && databaseUser.getDatabaseMusicPlayer().getLoadedType().equals("podcast")) {
            updatePodcastInfo(getTimestamp(), databaseUser.
                    getPlayedPodcasts().get(databaseUser
                            .getDatabaseMusicPlayer().getLoaded()));
        }
        ArrayList<String> filteredList = new ArrayList<String>(podcasts
                .stream()
                .filter((podcast) -> {
                    boolean resultValue = true;

                    if (filters.getName() != null) {
                        resultValue = resultValue && podcast.getName()
                                .startsWith(filters.getName());
                    }

                    if (filters.getOwner() != null) {
                        resultValue = resultValue && podcast.getOwner()
                                .equals(filters.getOwner());
                    }

                    return resultValue;
                })
                .limit(maxSizeSearch)
                .map(PodcastInput::getName).toList());

        databaseUser.getDatabaseSearchbar().setSelectedItem(null);
        databaseUser.getDatabaseSearchbar().setSearchResults(filteredList);
        databaseUser.getDatabaseSearchbar().setType("podcast");

        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("search", JsonNode.class));
        node.set("user", currentMapper.convertValue(databaseUser.getUserInput()
                .getUsername(), JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));
        node.set("message", currentMapper.convertValue("Search returned " + filteredList.size()
                + " results", JsonNode.class));
        node.set("results", currentMapper.convertValue(filteredList, JsonNode.class));

        Database.outputs.add(node);
        databaseUser.getDatabaseMusicPlayer().setLoaded(null);
        databaseUser.getDatabaseMusicPlayer().setLoadedType(null);
        databaseUser.getDatabaseSearchbar().setSearchSelectedType("podcast");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public PodcastFilters getFilters() {
        return filters;
    }

    public void setFilters(final PodcastFilters filters) {
        this.filters = filters;
    }
}
