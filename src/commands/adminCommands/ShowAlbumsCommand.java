package commands.adminCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.Database;
import database.collectionsOfSongs.DatabaseAlbum;
import database.userData.AlbumOutputStructure;
import database.userData.ArtistUser;

import java.util.ArrayList;


public final class ShowAlbumsCommand extends Command {
    private String username;

    /**
     *
     * @param artistUser the artist
     *
     * The method iterates through every album owned by the artist and adds the album information
     * to the results list and then puts it in the output node.
     */

    public void execute(final ArtistUser artistUser) {
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("showAlbums", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        ArrayList<AlbumOutputStructure> results = new ArrayList<>();
        for (DatabaseAlbum album : artistUser.getPage().getAlbums()) {
            results.add(album.storeAlbumInfo());
        }
        node.set("result", currentMapper.convertValue(results, JsonNode.class));
        Database.outputs.add(node);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }
}
