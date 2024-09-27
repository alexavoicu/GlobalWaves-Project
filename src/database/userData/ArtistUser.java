package database.userData;

import database.collectionsOfSongs.DatabasePlaylist;
import database.userData.pages.ArtistPage;
import fileio.input.UserInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class ArtistUser extends DatabaseUser {
    private ArtistPage page;
    private int totalNrOfLikes;

    public ArtistUser(final ArtistPage page, final UserInput userInput) {
        super(userInput);
        this.setNormalUser(false);
        this.page = page;
        this.totalNrOfLikes = 0;
    }

    /**
     *
     * @param users list of users
     * @param playlists list of playlists
     * @return if the artist can be deleted
     *
     * This method iterates through the hashmap of users and verifies if the loaded source of a
     * user is owned by the artist, or if the playlist played by the user contains any songs owned
     * by the artist, or if any user is on the artist's page.
     */
    public boolean canArtistBeDeleted(final HashMap<String, DatabaseUser> users,
                                      final ArrayList<DatabasePlaylist> playlists) {
        for (Map.Entry<String, DatabaseUser> entry : users.entrySet()) {
            DatabaseUser currentUser = entry.getValue();
            String audioPlayingOwner = currentUser.getDatabaseMusicPlayer().getAudioPlayingOwner();
            if (audioPlayingOwner != null && audioPlayingOwner
                    .equals(super.getUserInput().getUsername())) {
                return false;
            }
            if (currentUser.getDatabaseMusicPlayer().getLoadedType() != null
                    && currentUser.getDatabaseMusicPlayer().getLoadedType().equals("playlist")) {
                DatabasePlaylist playlist = playlists.stream()
                        .filter(p -> p.getName()
                                .equals(currentUser.getDatabaseMusicPlayer().getLoaded()))
                        .findFirst()
                        .orElse(null);
                if (playlist != null
                        && playlist.containsSongsFromArtist(super.getUserInput().getUsername())) {
                    return false;
                }

            }
            if (currentUser.getCurrentPage().equals("artist page")
                    && currentUser.getOwnerOfCurrentPage().equals(getUserInput().getUsername())) {
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
        return 1;
    }

    public ArtistPage getPage() {
        return page;
    }

    public void setPage(final ArtistPage page) {
        this.page = page;
    }

    public int getTotalNrOfLikes() {
        return totalNrOfLikes;
    }

    public void setTotalNrOfLikes(final int totalNrOfLikes) {
        this.totalNrOfLikes = totalNrOfLikes;
    }
}
