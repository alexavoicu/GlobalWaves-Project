package commands.searchBarCommands.searchCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.searchBarCommands.searchCommands.searchBarFilters.AlbumSearchFilter;
import database.Database;
import database.collectionsOfSongs.DatabaseAlbum;
import database.userData.DatabaseUser;

import java.util.ArrayList;

import static database.userData.PodcastTuple.updatePodcastInfo;

public final class AlbumSearchCommand extends SearchCommand {
    private String username;
    private AlbumSearchFilter filters;
    private final int maxSizeSearch = 5;

    /**
     *
     * @param albums the list of albums
     * @param databaseUser the user
     *
     * The method iterates through the list of albums and using the filters it finds the top 5
     * matches. Then the users searchbar is updated with the results.
     */

    public void execute(final ArrayList<DatabaseAlbum> albums, final DatabaseUser databaseUser) {
        if (databaseUser.getDatabaseMusicPlayer().getLoaded() != null
                && databaseUser.getDatabaseMusicPlayer().getLoadedType().equals("podcast")) {
            updatePodcastInfo(getTimestamp(), databaseUser.
                    getPlayedPodcasts().get(databaseUser
                            .getDatabaseMusicPlayer().getLoaded()));
        }
        ArrayList<String> filteredList = new ArrayList<String>(albums
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

                    if (filters.getDescription() != null) {
                        resultValue = resultValue && podcast.getDescription()
                                .startsWith(filters.getDescription());
                    }

                    return resultValue;
                })
                .limit(maxSizeSearch)
                .map(DatabaseAlbum::getName).toList());

        databaseUser.getDatabaseSearchbar().setSelectedItem(null);
        databaseUser.getDatabaseSearchbar().setSearchResults(filteredList);
        databaseUser.getDatabaseSearchbar().setType("album");

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
        databaseUser.getDatabaseSearchbar().setSearchSelectedType("album");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public AlbumSearchFilter getFilters() {
        return filters;
    }

    public void setFilters(final AlbumSearchFilter filters) {
        this.filters = filters;
    }
}
