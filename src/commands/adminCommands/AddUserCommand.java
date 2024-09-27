package commands.adminCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.Database;
import database.userData.ArtistUser;
import database.userData.DatabaseUser;
import database.userData.HostUser;
import database.userData.pages.ArtistPage;
import database.userData.pages.HostPage;
import fileio.input.UserInput;

import java.util.HashMap;

public final class AddUserCommand extends Command {
    private String type;
    private String username;
    private int age;
    private String city;

    /**
     *
     * @param users list of users
     * @param artists list of artists
     * @param hosts list of hosts
     *
     * The method checks if the username to add is already taken and then creates the user
     * objects accordingly. In the end, if the user is an artist or a host, the respective
     * lists are the updated, along with the list of users.
     */

    public void execute(final HashMap<String, DatabaseUser> users,
                        final HashMap<String, ArtistUser> artists,
                        final HashMap<String, HostUser> hosts) {
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("addUser", JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));

        if (users.containsKey(username)) {
            node.set("message", currentMapper.convertValue("The username "
                    + username + " is already taken.", JsonNode.class));
        } else {
            DatabaseUser newUser = null;
            switch (type) {
                case "user":
                    newUser = new DatabaseUser(new UserInput(username, age, city));
                    break;
                case "artist":
                    newUser = new ArtistUser(new ArtistPage(), new UserInput(username, age, city));
                    artists.put(username, (ArtistUser) newUser);
                    break;
                case "host":
                    newUser = new HostUser(new UserInput(username, age, city), new HostPage());
                    hosts.put(username, (HostUser) newUser);
                    break;
                default:
            }

            users.put(username, newUser);
            node.set("message", currentMapper.convertValue("The username "
                    + username + " has been added successfully.", JsonNode.class));
        }

        Database.outputs.add(node);
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public int getAge() {
        return age;
    }

    public void setAge(final int age) {
        this.age = age;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }
}
