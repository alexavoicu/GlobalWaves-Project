package database.userData.pages;

public class Page {
    private String username;

    /**
     *
     * @return
     * Gets the username of the pages' owner.
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @param username the username
     * Sets the username of the pages' owner.
     */
    public void setUsername(final String username) {
        this.username = username;
    }
}
