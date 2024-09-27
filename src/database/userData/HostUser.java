package database.userData;

import database.userData.pages.HostPage;
import fileio.input.UserInput;

import java.util.HashMap;
import java.util.Map;

public final class HostUser extends DatabaseUser {
    private HostPage page;

    public HostUser(final UserInput userInput, final HostPage page) {
        super(userInput);
        this.setNormalUser(false);
        this.page = page;
    }

    /**
     *
     * @param users the list of users
     * @return if the host can be deleted
     *
     * The method iterates through the list of users and verifies if any user is on the host's
     * page or if it is playing a podcast from the host.
     */
    public boolean canHostBeDeleted(final HashMap<String, DatabaseUser> users) {
        for (Map.Entry<String, DatabaseUser> entry : users.entrySet()) {
            DatabaseUser currentUser = entry.getValue();
            if (currentUser.getCurrentPage().equals("host page")
                    && currentUser.getOwnerOfCurrentPage().equals(getUserInput().getUsername())) {
                return false;
            }

            if (currentUser.getDatabaseMusicPlayer().getAudioPlayingOwner() != null
                    && currentUser.getDatabaseMusicPlayer().getAudioPlayingOwner()
                    .equals(getUserInput().getUsername())) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @return the priority of the artist for the printing of all the users
     */
    public int userTypePriority() {
        return 2;
    }

    public HostPage getPage() {
        return page;
    }

    public void setPage(final HostPage page) {
        this.page = page;
    }
}
