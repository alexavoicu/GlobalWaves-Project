package commands.specialUsersCommands.artistCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.specialUsersCommands.SpecialUserCommand;
import database.Database;
import database.userData.ArtistUser;
import database.userData.DatabaseUser;
import database.userData.artistPageElements.Merch;

import java.util.HashMap;


public final class AddMerchCommand extends SpecialUserCommand {
    private String username;
    private String name;
    private String description;
    private int price;

    /**
     *
     * @param users list of users
     * @param artists list of artists
     *
     * The method starts by verifying if the user exists and if it is an artist, after, it gets
     * the artist and finds if there is merch with the same name already and outputs an error
     * message if needed, then it verifies if the price is negative and outputs an error
     * message if needed, otherwise it adds the merch to the artist's page.
     */

    public void execute(final HashMap<String, DatabaseUser> users,
                        final HashMap<String, ArtistUser> artists) {
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("addMerch", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (!userExists(users, username, node, currentMapper)
                || !isUserArtist(artists, username, node, currentMapper)) {
            Database.outputs.add(node);
            return;
        }

        ArtistUser currentArtist = artists.get(username);
        Merch existingMerch = findExistingMerch(currentArtist, name);

        if (existingMerch != null) {
            node.set("message", currentMapper.convertValue(username
                    + " has merchandise with the same name.", JsonNode.class));
        } else if (price < 0) {
            node.set("message", currentMapper.convertValue("Price for merchandise"
                    + " can not be negative.", JsonNode.class));
        } else {
            currentArtist.getPage().getMerch().add(new Merch(name, description, price));
            node.set("message", currentMapper.convertValue(username
                    + " has added new merchandise successfully.", JsonNode.class));
        }

        Database.outputs.add(node);
    }

    /**
     *
     * @param currentArtist the artist adding the merch
     * @param merchName name of the merch to add
     * @return the merch if it exists, or null if it doesn't
     *
     * The method filters through the merch from the artist's page, if there is a merch with the
     * same name then it returns it, otherwise it returns null.
     */

    private Merch findExistingMerch(final ArtistUser currentArtist, final String merchName) {
        return currentArtist.getPage().getMerch()
                .stream()
                .filter(merch1 -> merch1.getName().equals(merchName))
                .findFirst()
                .orElse(null);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(final int price) {
        this.price = price;
    }
}
