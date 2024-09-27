package commands.specialUsersCommands.artistCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.specialUsersCommands.SpecialUserCommand;
import database.Database;
import database.userData.ArtistUser;
import database.userData.DatabaseUser;
import database.userData.artistPageElements.Event;

import java.util.HashMap;

public final class RemoveEventCommand extends SpecialUserCommand {
    private String username;
    private String name;

    /**
     *
     * @param users list of users
     * @param artists list of artists
     *
     * The method verifies if the user exists and if it is an artist, then it finds the event
     * that needs to be deleted. If there is no such event, then an error message is shown,
     * otherwise, it removes the event from the artist's page and the success message is shown.
     */

    public void execute(final HashMap<String, DatabaseUser> users,
                        final HashMap<String, ArtistUser> artists) {
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("removeEvent", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (!userExists(users, username, node, currentMapper)
                || !isUserArtist(artists, username, node, currentMapper)) {
            Database.outputs.add(node);
            return;
        }

        ArtistUser currentArtist = artists.get(username);
        Event event = currentArtist.getPage().getEvents()
                .stream()
                .filter(event1 -> event1.getName().equals(name))
                .findFirst()
                .orElse(null);

        if (event == null) {
            node.set("message", currentMapper.convertValue(username
                    + " doesn't have an event with the given name.", JsonNode.class));
        } else {
            currentArtist.getPage().getEvents().removeIf(event1 -> event1.getName().equals(name));
            node.set("message", currentMapper.convertValue(username
                    + " deleted the event successfully.", JsonNode.class));
        }

        Database.outputs.add(node);
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
}
