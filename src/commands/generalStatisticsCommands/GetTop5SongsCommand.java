package commands.generalStatisticsCommands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.Database;
import database.DatabaseSong;
import fileio.input.SongInput;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class GetTop5SongsCommand extends Command {
    private final int maxSizeFilter = 5;

    /**
     *
     * @param databaseSongs list of input songs
     */

    public void execute(final ArrayList<DatabaseSong> databaseSongs) {
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("getTop5Songs", JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        ArrayList<SongInput> top5Songs = (ArrayList<SongInput>) databaseSongs.stream()
                .sorted(Comparator.comparing(DatabaseSong::getNumberOfLikes).reversed())
                .limit(maxSizeFilter)
                .map(DatabaseSong::getSong)
                .collect(Collectors.toList());

        ArrayList<String> top5SongsNames = top5Songs.stream()
                .map(SongInput::getName)
                .collect(Collectors.toCollection(ArrayList::new));

        node.set("result", currentMapper.convertValue(top5SongsNames, JsonNode.class));
        Database.outputs.add(node);

    }
}
