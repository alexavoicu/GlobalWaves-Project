package commands.searchBarCommands.searchCommands;

import commands.Command;

public class SearchCommand extends Command {
    private String type;

    /**
     *
     * @return the type of audio file we are searching
     */

    public String getType() {
        return type;
    }

    /**
     *
     * @param type the type of audio file we are searching
     */

    public void setType(final String type) {
        this.type = type;
    }
}
