package commands.searchBarCommands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.Database;
import database.userData.DatabaseSearchbar;
import database.userData.DatabaseUser;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class SelectCommand extends Command {
    private String username;
    private int itemNumber;

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public int getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(final int itemNumber) {
        this.itemNumber = itemNumber;
    }


    /**
     *
     * @param user the user that gave the command
     *
     * The method checks for results stored in the searchbar and then, if the selected id is valid
     * it updates the selected iterm in the searchbar.
     */
    public void execute(final DatabaseUser user) {
        DatabaseSearchbar searchBar = user.getDatabaseSearchbar();

        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("select", JsonNode.class));
        node.set("user", currentMapper.convertValue(user.getUserInput().getUsername(),
                JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (searchBar.getSearchResults() == null) {
            searchBar.setSelectedItem(null);
            node.set("message", currentMapper.convertValue("Please conduct a search "
                    + "before making a selection.", JsonNode.class));
        } else if (itemNumber > searchBar.getSearchResults().size()) {
                node.set("message", currentMapper.convertValue("The selected ID is too high.",
                        JsonNode.class));
        } else {
            searchBar.setSelectedItem(searchBar.getSearchResults().get(itemNumber - 1));
            if (searchBar.getSearchSelectedType().equals("artist")
                    || searchBar.getSearchSelectedType().equals("host")) {
                node.set("message", currentMapper.convertValue("Successfully selected " + searchBar
                        .getSearchResults().get(itemNumber - 1) + "'s page.", JsonNode.class));

                user.setOwnerOfCurrentPage(searchBar
                        .getSearchResults().get(itemNumber - 1));
                if (searchBar.getSearchSelectedType().equals("artist")) {
                    user.setCurrentPage("artist page");
                } else {
                    user.setCurrentPage("host page");
                }

            } else {
                node.set("message", currentMapper.convertValue("Successfully selected " + searchBar
                        .getSearchResults().get(itemNumber - 1) + ".", JsonNode.class));
            }

        }
        searchBar.setSearchResults(null);

        Database.outputs.add(node);

    }
}
