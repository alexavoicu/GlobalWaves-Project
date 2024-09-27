package commands.generalStatisticsCommands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.Database;
import database.collectionsOfSongs.DatabasePlaylist;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class GetTop5PlaylistsCommand extends Command {
    private final int maxSizeFilter = 5;
    /**
     *
     * @param playlists list of playlists
     */
    public void execute(final ArrayList<DatabasePlaylist> playlists) {
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("getTop5Playlists", JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        ArrayList<String> top5Playlists = (ArrayList<String>) playlists.stream()
                .sorted(Comparator.comparing(DatabasePlaylist::getNrOfFollows).reversed())
                .limit(maxSizeFilter)
                .map(DatabasePlaylist::getName)
                .collect(Collectors.toList());
        node.set("result", currentMapper.convertValue(top5Playlists, JsonNode.class));
        Database.outputs.add(node);

    }
}

