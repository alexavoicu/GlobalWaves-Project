package database.userData.pages;

import database.DatabaseSong;
import database.collectionsOfSongs.DatabasePlaylist;
import database.userData.DatabaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class HomePage extends Page {
    private ArrayList<String> top5LikedSongs;
    private ArrayList<String> top5PlaylistsFollowed;
    private final int maxSizeRecommendations = 5;

    public HomePage() {
        this.top5LikedSongs = new ArrayList<>();
        this.top5PlaylistsFollowed = new ArrayList<>();
    }

    /**
     *
     * @param user the user
     *
     * The method finds the top 5 liked songs and playlists and then sets the lists of names.
     */
    public void findRecommendations(final DatabaseUser user) {
        List<DatabaseSong> likedSongs = user.getLikedSongs()
                .subList(0, Math.min(user.getLikedSongs().size(), maxSizeRecommendations));
        List<DatabasePlaylist> playlistsFollowed = user.getFollowedPlaylists()
                .subList(0, Math.min(user.getFollowedPlaylists().size(), maxSizeRecommendations));

        this.top5LikedSongs = likedSongs.stream()
                .map(song -> song.getSong().getName())
                .collect(Collectors.toCollection(ArrayList::new));
        this.top5PlaylistsFollowed = playlistsFollowed.stream()
                .map(DatabasePlaylist::getName)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<String> getTop5LikedSongs() {
        return top5LikedSongs;
    }

    public void setTop5LikedSongs(final ArrayList<String> top5LikedSongs) {
        this.top5LikedSongs = top5LikedSongs;
    }

    public ArrayList<String> getTop5PlaylistsFollowed() {
        return top5PlaylistsFollowed;
    }

    public void setTop5PlaylistsFollowed(final ArrayList<String> top5PlaylistsFollowed) {
        this.top5PlaylistsFollowed = top5PlaylistsFollowed;
    }
}
