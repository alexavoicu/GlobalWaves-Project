package commands.searchBarCommands.searchCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.searchBarCommands.searchCommands.searchBarFilters.ArtistSearchFilter;
import database.Database;
import database.userData.ArtistUser;
import database.userData.DatabaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public final class ArtistSearchCommand extends SearchCommand {
    private String username;
    private ArtistSearchFilter filters;
    private final int maxSizeSearch = 5;

    /**
     *
     * @param artists list of artists
     * @param databaseUser the user
     *
     * The method iterates through the list of artists and using the filters it finds the top 5
     * matches. Then the users searchbar is updated with the results.
     */

    public void execute(final HashMap<String, ArtistUser> artists,
                        final DatabaseUser databaseUser) {
        ArrayList<String> filteredList = artists.keySet().stream()
                .filter(artistUser -> artistUser.startsWith(filters.getName()))
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
            databaseUser.getDatabaseSearchbar().setType("artist");
            databaseUser.getDatabaseSearchbar().setSearchSelectedType("artist");
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

    public ArtistSearchFilter getFilters() {
        return filters;
    }

    public void setFilters(final ArtistSearchFilter filters) {
        this.filters = filters;
    }
}
