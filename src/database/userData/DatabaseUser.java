package database.userData;

import database.collectionsOfSongs.DatabasePlaylist;
import database.DatabaseSong;
import database.userData.pages.HomePage;
import database.userData.pages.LikedContentPage;
import fileio.input.UserInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DatabaseUser {
    private final UserInput userInput;
    private final ArrayList<DatabaseSong> likedSongs;
    private final ArrayList<DatabasePlaylist> followedPlaylists;
    private final ArrayList<DatabasePlaylist> createdPlaylists;
    private final DatabaseSearchbar databaseSearchbar;
    private final DatabaseMusicPlayer databaseMusicPlayer;

    private HomePage homePage;
    private LikedContentPage likedContentPage;
    private final HashMap<String, PodcastTuple> playedPodcasts;
    private final HashMap<String, DatabasePlaylist> shuffledSongsCollections;
    private final HashMap<String, Integer> songsCollectionPassedTimeBeforeShuffle;
    private HashMap<String, RepeatSongsCollectionTuple> songsCollectionRepeatInfo;


    private boolean isOnline;
    private boolean isNormalUser;
    private int switchToOfflineTimestamp;
    private String currentPage;
    private String ownerOfCurrentPage;


    public DatabaseUser(final UserInput userInput) {
        this.userInput = userInput;
        this.followedPlaylists = new ArrayList<DatabasePlaylist>();
        this.likedSongs = new ArrayList<DatabaseSong>();
        this.databaseSearchbar = new DatabaseSearchbar();
        this.databaseMusicPlayer = new DatabaseMusicPlayer();
        this.createdPlaylists = new ArrayList<DatabasePlaylist>();
        this.playedPodcasts = new HashMap<String, PodcastTuple>();
        this.shuffledSongsCollections = new HashMap<String, DatabasePlaylist>();
        this.songsCollectionPassedTimeBeforeShuffle = new HashMap<String, Integer>();
        this.isOnline = true;
        this.isNormalUser = true;
        this.switchToOfflineTimestamp = 0;
        this.homePage = new HomePage();
        this.likedContentPage = new LikedContentPage();
        this.currentPage = "Home";
        this.ownerOfCurrentPage = userInput.getUsername();
        this.songsCollectionRepeatInfo = new HashMap<>();
    }

    /**
     *
     * @param artistName artist
     *
     * This method removes any liked songs from the artist.
     */
    public void deleteLikedContentFromArtist(final String artistName) {
        likedSongs.removeIf(song -> song.getSong().getArtist().equals(artistName));
    }

    /**
     *
     * @param user the user whose content needs to be removed.
     *
     * This method removes any followed playlists owned by the user.
     */
    public void deleteFollowedPlaylistsFromUser(final String user) {
        followedPlaylists.removeIf(databasePlaylist -> databasePlaylist.getOwner().equals(user));
    }

    /**
     *
     * @return the priority of the normal user for the printing of all the users
     */
    public int userTypePriority() {
        return 0;
    }

    /**
     *
     * @param users list of users
     * @param playlists list of playlists
     * @return if the user can be deleted
     *
     * The method iterates through every user in the hashmap and verifies if any users are
     * currently playing a playlist created by the user that needs to be deleted.
     */
    public boolean canUserBeDeleted(final HashMap<String, DatabaseUser> users,
                                    final ArrayList<DatabasePlaylist> playlists) {
        for (Map.Entry<String, DatabaseUser> entry : users.entrySet()) {
            DatabaseUser currentUser = entry.getValue();
            if (currentUser.getDatabaseMusicPlayer().getLoadedType() != null
                    && currentUser.getDatabaseMusicPlayer().getLoadedType().equals("playlist")) {
                DatabasePlaylist playlist = playlists.stream()
                        .filter(p -> p.getName().equals(currentUser
                                .getDatabaseMusicPlayer().getLoaded()))
                        .findFirst()
                        .orElse(null);
                if (playlist != null
                        && playlist.getOwner().equals(userInput.getUsername())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     *
     * @return
     * Gets the user input.
     */
    public UserInput getUserInput() {
        return userInput;
    }

    /**
     *
     * @return
     * Gets the liked songs.
     */
    public ArrayList<DatabaseSong> getLikedSongs() {
        return likedSongs;
    }

    /**
     *
     * @return
     * Gets the followed playlists.
     */
    public ArrayList<DatabasePlaylist> getFollowedPlaylists() {
        return followedPlaylists;
    }

    /**
     *
     * @return
     * Gets the searchbar.
     */
    public DatabaseSearchbar getDatabaseSearchbar() {
        return databaseSearchbar;
    }

    /**
     *
     * @return
     * Gets the player.
     */
    public DatabaseMusicPlayer getDatabaseMusicPlayer() {
        return databaseMusicPlayer;
    }

    /**
     *
     * @return
     * Gets the list of created playlists.
     */
    public ArrayList<DatabasePlaylist> getCreatedPlaylists() {
        return createdPlaylists;
    }

    /**
     *
     * @return
     * Gets the hashmap of played podcasts and the information about them.
     */
    public HashMap<String, PodcastTuple> getPlayedPodcasts() {
        return playedPodcasts;
    }

    /**
     *
     * @return
     * Gets the hashmap of the shuffled song collections.
     */
    public HashMap<String, DatabasePlaylist> getShuffledSongsCollections() {
        return shuffledSongsCollections;
    }

    /**
     *
     * @return
     * Gets the hashmap of passed time before shuffling.
     */
    public HashMap<String, Integer> getSongsCollectionPassedTimeBeforeShuffle() {
        return songsCollectionPassedTimeBeforeShuffle;
    }

    /**
     *
     * @return
     * Gets the name of the command.
     */
    public boolean isOnline() {
        return isOnline;
    }
    /**
     *
     * @param online
     * Sets the online status of the user.
     */
    public void setOnline(final boolean online) {
        isOnline = online;
    }

    /**
     *
     * @return
     * Gets the offline timestamp.
     */
    public int getSwitchToOfflineTimestamp() {
        return switchToOfflineTimestamp;
    }
    /**
     *
     * @param switchToOfflineTimestamp
     * Sets the timestamp when the user went offline.
     */
    public void setSwitchToOfflineTimestamp(final int switchToOfflineTimestamp) {
        this.switchToOfflineTimestamp = switchToOfflineTimestamp;
    }

    /**
     *
     * @return
     * Gets the home page.
     */
    public HomePage getHomePage() {
        return homePage;
    }
    /**
     *
     * @param homePage
     * Sets the users' home page.
     */
    public void setHomePage(final HomePage homePage) {
        this.homePage = homePage;
    }

    /**
     *
     * @return
     * Gets the liked content page.
     */
    public LikedContentPage getLikedContentPage() {
        return likedContentPage;
    }
    /**
     *
     * @param likedContentPage
     * Sets the users' liked content page.
     */
    public void setLikedContentPage(final LikedContentPage likedContentPage) {
        this.likedContentPage = likedContentPage;
    }

    /**
     *
     * @return
     * Gets the current page.
     */
    public String getCurrentPage() {
        return currentPage;
    }
    /**
     *
     * @param currentPage
     * Sets the name of the page that the user is currently on.
     */
    public void setCurrentPage(final String currentPage) {
        this.currentPage = currentPage;
    }

    /**
     *
     * @return
     * Gets the name of the command.
     */
    public boolean isNormalUser() {
        return isNormalUser;
    }
    /**
     *
     * @param normalUser
     * Sets if the user is a normal one.
     */
    public void setNormalUser(final boolean normalUser) {
        isNormalUser = normalUser;
    }

    /**
     *
     * @return
     * Gets the owner of the current page.
     */
    public String getOwnerOfCurrentPage() {
        return ownerOfCurrentPage;
    }
    /**
     *
     * @param ownerOfCurrentPage
     * Sets the owner of the page that the user is currently on.
     */
    public void setOwnerOfCurrentPage(final String ownerOfCurrentPage) {
        this.ownerOfCurrentPage = ownerOfCurrentPage;
    }

    /**
     *
     * @return
     * Gets the repeated songs collection information.
     */
    public HashMap<String, RepeatSongsCollectionTuple> getSongsCollectionRepeatInfo() {
        return songsCollectionRepeatInfo;
    }
    /**
     *
     * @param songsCollectionRepeatInfo
     * Sets a hashmap of information about the repeated audio.
     */
    public void setSongsCollectionRepeatInfo(final HashMap<String, RepeatSongsCollectionTuple>
                                                     songsCollectionRepeatInfo) {
        this.songsCollectionRepeatInfo = songsCollectionRepeatInfo;
    }
}
