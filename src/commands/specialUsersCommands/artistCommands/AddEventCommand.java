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


public final class AddEventCommand extends SpecialUserCommand {
    private String username;
    private String name;
    private String description;
    private String date;

    private final int maxDateOfFebruary = 28;
    private final int february = 2;
    private final int maxDateNormalMonth = 31;
    private final int maxMonthNumber = 12;
    private final int inferiorLimitForYear = 1900;
    private final int superiorLimitForYear = 2023;

    /**
     *
     * @param users list of users
     * @param artists list of artists
     *
     * The method start by verifying if the user exists and if it is an artist, then
     * it tries to find if an event with the same name already exists and if it doesn't
     * it validates the date and shows the respective messages.
     */

    public void execute(final HashMap<String, DatabaseUser> users,
                        final HashMap<String, ArtistUser> artists) {
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("addEvent", JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));

        if (!userExists(users, username, node, currentMapper)
                || !isUserArtist(artists, username, node, currentMapper)) {
            Database.outputs.add(node);
            return;
        }

        ArtistUser currentArtist = artists.get(username);
        Event event = currentArtist.getPage().getEvents().stream()
                .filter(event1 -> event1.getName().equals(name))
                .findFirst()
                .orElse(null);
        if (event != null) {
            node.set("message", currentMapper.convertValue(username
                    + " has another event with the same name.", JsonNode.class));
        } else {
            validateAndAddEvent(currentArtist, node, currentMapper);
        }

        Database.outputs.add(node);
    }

    /**
     *
     * @param currentArtist artist
     * @param node the output node
     * @param currentMapper the object mapper
     *
     * The method transforms the date into a LocalDate object so that it can be easy to verify
     * if the date is valid or not, if it is then it adds the event to the artist's page and
     * shows a success message.
     */

    private void validateAndAddEvent(final ArtistUser currentArtist, final ObjectNode node,
                                     final ObjectMapper currentMapper) {

        int firstDashIndex = date.indexOf('-');
        int secondDashIndex = date.lastIndexOf('-');

        int day = Integer.parseInt(date.substring(0, firstDashIndex));
        int month = Integer.parseInt(date.substring(firstDashIndex + 1, secondDashIndex));
        int year = Integer.parseInt(date.substring(secondDashIndex + 1));


        if (day > maxDateOfFebruary && month == february) {
            node.set("message", currentMapper.convertValue("Event for " + username
                    + " does not have a valid date.", JsonNode.class));
        } else if (day > maxDateNormalMonth || month > maxMonthNumber
                || year < inferiorLimitForYear || year > superiorLimitForYear) {
            node.set("message", currentMapper.convertValue("Event for " + username
                    + " does not have a valid date.", JsonNode.class));
        } else {
            currentArtist.getPage().getEvents().add(new Event(name, description, date));
            node.set("message", currentMapper.convertValue(username
                    + " has added new event successfully.", JsonNode.class));
        }
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

    public String getDate() {
        return date;
    }

    public void setDate(final String date) {
        this.date = date;
    }
}
