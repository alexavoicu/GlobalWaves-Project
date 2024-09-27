package database.userData.pages;

import database.DatabaseSong;
import database.collectionsOfSongs.DatabasePlaylist;
import database.userData.DatabaseUser;

import java.util.ArrayList;

public final class LikedContentPage extends Page {
    private ArrayList<String> likedSongs;
    private ArrayList<String> followedPlaylists;

    public LikedContentPage() {
        this.likedSongs = new ArrayList<>();
        this.followedPlaylists = new ArrayList<>();
    }

    /**
     *
     * @param user the user
     *
     * The method iterates through the liked contents of the user and adds the names to the
     * respective lists.
     */
    public void getLikedContent(final DatabaseUser user) {
        likedSongs = new ArrayList<>();
        for (DatabaseSong song : user.getLikedSongs()) {
            likedSongs.add(song.getSong().getName() + " - " + song.getSong().getArtist());
        }

        followedPlaylists = new ArrayList<>();
        for (DatabasePlaylist playlist : user.getFollowedPlaylists()) {
            followedPlaylists.add(playlist.getName() + " - " + playlist.getOwner());
        }
    }

    public ArrayList<String> getLikedSongs() {
        return likedSongs;
    }

    public void setLikedSongs(final ArrayList<String> likedSongs) {
        this.likedSongs = likedSongs;
    }

    public ArrayList<String> getFollowedPlaylists() {
        return followedPlaylists;
    }

    public void setFollowedPlaylists(final ArrayList<String> followedPlaylists) {
        this.followedPlaylists = followedPlaylists;
    }
}
