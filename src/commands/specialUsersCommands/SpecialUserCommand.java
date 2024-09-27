package commands.specialUsersCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.userData.ArtistUser;
import database.userData.DatabaseUser;
import database.userData.HostUser;

import java.util.HashMap;

public abstract class SpecialUserCommand extends Command {
    /**
     *
     * @param users list of users
     * @param node the output node
     * @param username the username
     * @param currentMapper the object mapper
     * @return does the user exist
     *
     * The method verifies if the users hashmap contains the username.
     */

    protected boolean userExists(final HashMap<String, DatabaseUser> users,
                                 final String username,
                                 final ObjectNode node, final ObjectMapper currentMapper) {
        if (!users.containsKey(username)) {
            node.set("message", currentMapper.convertValue("The username " + username
                    + " doesn't exist.", JsonNode.class));
            return false;
        }
        return true;
    }

    /**
     *
     * @param artists list of artists
     * @param username the username
     * @param node the output node
     * @param currentMapper the object mapper
     * @return if the user is an artist or not
     *
     * The method checks if the hashmap of artists contains the username and returns true,
     * otherwise it returns false.
     */

    protected boolean isUserArtist(final HashMap<String, ArtistUser> artists,
                                   final String username, final ObjectNode node,
                                   final ObjectMapper currentMapper) {
        if (!artists.containsKey(username)) {
            node.set("message", currentMapper.convertValue(username
                    + " is not an artist.", JsonNode.class));
            return false;
        }
        return true;
    }

    /**
     *
     * @param hosts list of hosts
     * @param username the username
     * @param node the output node
     * @param currentMapper the object mapper
     * @return if the user is a host or not
     *
     * The method checks if the hashmap of hosts contains the username and returns true,
     * otherwise it returns false.
     */

    protected boolean isUserHost(final HashMap<String, HostUser> hosts,
                                   final String username, final ObjectNode node,
                                   final ObjectMapper currentMapper) {
        if (!hosts.containsKey(username)) {
            node.set("message", currentMapper.convertValue(username
                    + " is not a host.", JsonNode.class));
            return false;
        }
        return true;
    }
}
