package commands.searchBarCommands.searchCommands;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.searchBarCommands.searchCommands.searchBarFilters.SongFilter;
import database.Database;
import database.DatabaseSong;
import database.userData.DatabaseUser;
import java.util.ArrayList;
import static database.userData.PodcastTuple.updatePodcastInfo;

public final class SongSearchCommand extends SearchCommand {
    private final int maxSizeSearch = 5;
    private String username;
    private SongFilter filters;

    /**
     *
     * @param songs list of songs
     * @param databaseUser user that gave the command
     *
     * The method iterates through the list of songs and using the filters it finds the top 5
     * matches. Then the users searchbar is updated with the results.
     */

    public void execute(final ArrayList<DatabaseSong> songs, final DatabaseUser databaseUser) {
        if (databaseUser.getDatabaseMusicPlayer().getLoaded() != null
                && databaseUser.getDatabaseMusicPlayer().getLoadedType().equals("podcast")) {
            updatePodcastInfo(getTimestamp(), databaseUser.
                    getPlayedPodcasts().get(databaseUser
                            .getDatabaseMusicPlayer().getLoaded()));
        }

        ArrayList<String> filteredList = new ArrayList<String>(songs
                .stream()
                .filter((song) -> {
            boolean resultValue = true;

            if (filters.getName() != null) {
                resultValue = resultValue && song.getSong().getName()
                        .startsWith(filters.getName());
            }

            if (filters.getAlbum() != null) {
                resultValue = resultValue && song.getSong().getAlbum().equals(filters.getAlbum());
            }

            if (filters.getLyrics() != null) {
                String lowercaseLyrics = filters.getLyrics().toLowerCase();
                String lowercaseSongLyrics = song.getSong().getLyrics().toLowerCase();
                resultValue = resultValue && (song.getSong().getLyrics()
                        .contains(filters.getLyrics())
                || song.getSong().getLyrics().contains(lowercaseLyrics)
                        || lowercaseSongLyrics.contains(lowercaseLyrics));
            }

            if (filters.getGenre() != null) {
                resultValue = resultValue && song.getSong().getGenre()
                        .equalsIgnoreCase(filters.getGenre());
            }

            if (filters.getReleaseYear() != null) {
                char operator = filters.getReleaseYear().charAt(0);
                String value = filters.getReleaseYear().substring(1);

                if (operator == '>') {
                    resultValue = resultValue && song.getSong()
                            .getReleaseYear() > Integer.parseInt(value);
                } else {
                    resultValue = resultValue && song.getSong()
                            .getReleaseYear() < Integer.parseInt(value);
                }
            }

            if (filters.getArtist() != null) {
                resultValue = resultValue && song.getSong()
                        .getArtist().equals(filters.getArtist());
            }

            if (filters.getTags() != null) {
                for (String tag : filters.getTags()) {
                    resultValue = resultValue && song.getSong().getTags().contains(tag);
                }
            }

            return resultValue;
        })
                .limit(maxSizeSearch)
                .map((fSong) -> fSong.getSong().getName()).toList());

        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("search", JsonNode.class));
        node.set("user", currentMapper.convertValue(databaseUser.getUserInput()
                .getUsername(), JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (databaseUser.isOnline()) {
            databaseUser.getDatabaseSearchbar().setSelectedItem(null);
            databaseUser.getDatabaseSearchbar().setSearchResults(filteredList);
            databaseUser.getDatabaseSearchbar().setType("song");
            databaseUser.getDatabaseSearchbar().setSearchSelectedType("song");
            node.set("message", currentMapper.convertValue("Search returned " + filteredList.size()
                    + " results", JsonNode.class));
            node.set("results", currentMapper.convertValue(filteredList, JsonNode.class));
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

    public SongFilter getFilters() {
        return filters;
    }

    public void setFilters(final SongFilter filters) {
        this.filters = filters;
    }
}
