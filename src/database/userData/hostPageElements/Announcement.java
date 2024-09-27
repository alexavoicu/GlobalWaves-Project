package database.userData.hostPageElements;

public final class Announcement {
    private String name;
    private String description;

    public Announcement(final String name, final String description) {
        this.name = name;
        this.description = description;
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

    /**
     *
     * @return a string that contains the announcement information
     */
    public String toString() {
        return name + ":\n\t" + description + "\n";
    }
}
