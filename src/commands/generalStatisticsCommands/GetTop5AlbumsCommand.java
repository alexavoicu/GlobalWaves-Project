package commands.generalStatisticsCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.Database;
import database.collectionsOfSongs.DatabaseAlbum;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public final class GetTop5AlbumsCommand extends Command {

    private final int maxSizeOfList = 5;

    /**
     *
     * @param albums list of albums
     *
     * The method iterates through the list of albums and calls the calculateTotalLikesForAlbum
     * method and then creates a list of album names by sorting the list of albums by the number
     * of likes, in descending order, then puts the result in the output node.
     */
    public void execute(final ArrayList<DatabaseAlbum> albums) {
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("getTop5Albums", JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        for (DatabaseAlbum album : albums) {
            album.calculateTotalLikesForAlbum();
        }

        ArrayList<String> top5Albums = albums.stream()
                .sorted(Comparator.comparingInt(DatabaseAlbum::getNrOfLikes).reversed())
                .limit(maxSizeOfList)
                .map(DatabaseAlbum::getName)
                .collect(Collectors.toCollection(ArrayList::new));
        node.set("result", currentMapper.convertValue(top5Albums, JsonNode.class));
        Database.outputs.add(node);
    }

}
