package commands;

public class Command {
    private String command;
    private int timestamp;

    /**
     * Executes the command. Subclasses should provide specific functionality for the command.
     */
    public void execute() {
        return;
    }

    /**
     *
     * @return
     * Gets the name of the command.
     */
    public String getCommand() {
        return command;
    }

    /**
     *
     * @param command
     * Sets the name of the command.
     */
    public void setCommand(final String command) {
        this.command = command;
    }

    /**
     *
     * @return
     * Gets the timestamp associated with the command.
     */
    public int getTimestamp() {
        return timestamp;
    }

    /**
     *
     * @param timestamp
     * Sets the timestamp associated with the command.
     */

    public void setTimestamp(final int timestamp) {
        this.timestamp = timestamp;
    }
}
