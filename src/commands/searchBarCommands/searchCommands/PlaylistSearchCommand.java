package commands.searchBarCommands.searchCommands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.searchBarCommands.searchCommands.searchBarFilters.PlaylistFilter;
import database.Database;
import database.collectionsOfSongs.DatabasePlaylist;
import database.userData.DatabaseUser;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class PlaylistSearchCommand extends SearchCommand {
    private final int maxSizeSearch = 5;
    private String username;
    private PlaylistFilter filters;

    /**
     *
     * @param playlists list of playlists
     * @param databaseUser user that gave the command
     *
     * The method iterates through the list of playlists and using the filters it finds the top 5
     * matches. Then the users searchbar is updated with the results.
     */

    public void execute(final ArrayList<DatabasePlaylist> playlists,
                        final DatabaseUser databaseUser) {

        ArrayList<String> filteredList = new ArrayList<String>(playlists.stream()
                .filter((playlist) -> {
                    boolean resultValue = true;

                    if (filters.getName() != null) {
                        resultValue = resultValue && playlist.getName()
                                .startsWith(filters.getName());
                    }

                    if (filters.getOwner() != null) {
                        resultValue = resultValue && playlist.getOwner()
                                .equals(filters.getOwner());
                    }

                    if (!playlist.getOwner().equals(username)
                            && playlist.getPrivate()) {
                        resultValue = false;
                    }

                    return resultValue;
                })
                .limit(maxSizeSearch)
                .map((fPlaylist) -> fPlaylist.getName()).toList());

        databaseUser.getDatabaseSearchbar().setSelectedItem(null);
        databaseUser.getDatabaseSearchbar().setSearchResults(filteredList);
        databaseUser.getDatabaseSearchbar().setType("playlist");

        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("search", JsonNode.class));
        node.set("user", currentMapper.convertValue(databaseUser.getUserInput()
                .getUsername(), JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));
        node.set("message", currentMapper.convertValue("Search returned "
                + filteredList.size() + " results", JsonNode.class));
        node.set("results", currentMapper.convertValue(filteredList, JsonNode.class));

        Database.outputs.add(node);

        databaseUser.getDatabaseMusicPlayer().setLoaded(null);
        databaseUser.getDatabaseMusicPlayer().setLoadedType(null);
        databaseUser.getDatabaseMusicPlayer().setShuffled(false);
        databaseUser.getDatabaseMusicPlayer().setRepeatStatus(0);
        databaseUser.getDatabaseSearchbar().setSearchSelectedType("playlist");

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public PlaylistFilter getFilters() {
        return filters;
    }

    public void setFilters(final PlaylistFilter filters) {
        this.filters = filters;
    }

}
