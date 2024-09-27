package database.collectionsOfSongs;

import database.DatabaseSong;
import database.userData.SongsCollectionTuple;

import java.util.ArrayList;

public class SongsCollection {
    private String name;
    private ArrayList<DatabaseSong> songs;
    private String owner;

    public SongsCollection(final String name,
                           final ArrayList<DatabaseSong> songs,
                           final String owner) {
        this.name = name;
        this.songs = songs;
        this.owner = owner;
    }

    /**
     *
     * @param artist the artists
     * @return if the song collection contains any songs from the artist
     */
    public boolean containsSongsFromArtist(final String artist) {
        for (DatabaseSong song : songs) {
            if (song.getSong().getArtist().equals(artist)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param passedTime since collection started playing
     * @return an object that has time passed, song, time left and previous song
     *
     * This method returns the necessary information about a collection being played, based
     * on the duration of every song.
     */

    public SongsCollectionTuple findSongPlaying(final int passedTime) {
        int countSongsTime = 0;
        int timeLeft = 0;
        int timePassedForSong = 0;
        DatabaseSong songPlaying = null;
        DatabaseSong prevSong = null;
        for (DatabaseSong song : this.songs) {
            if (countSongsTime <= passedTime && countSongsTime + song.getSong()
                    .getDuration() > passedTime) {
                songPlaying = song;
                timeLeft = countSongsTime + song.getSong().getDuration() - passedTime;
                timePassedForSong = song.getSong().getDuration() - timeLeft;

                break;
            }
            countSongsTime += song.getSong().getDuration();
            prevSong = song;
        }
        if (songPlaying != null) {
            boolean isFirst = false;
            if (songPlaying.equals(songs.get(0))) {
                isFirst = true;
            }

            return new SongsCollectionTuple(timePassedForSong, songPlaying,
                    timeLeft, isFirst, prevSong);
        }
        return null;
    }

    /**
     *
     * @param songName name of the song playing
     * @return total time that is played until the player reaches the song
     */

    public int findTotalTimePlayedUntilSong(final String songName) {
        int totalTime = 0;
        for (DatabaseSong song : this.songs) {
            if (song.getSong().getName().equals(songName)) {
                break;
            }
            totalTime += song.getSong().getDuration();
        }
        return totalTime;
    }

    /**
     *
     * @return total duration of the collection
     */

    public int getTotalCollectionDuration() {
        int totalTime = 0;
        for (DatabaseSong song : this.songs) {
            totalTime += song.getSong().getDuration();
        }
        return totalTime;
    }

    /**
     *
     * @return
     * Gets the name of the collection.
     */

    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * Sets the name of the collection.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * Gets the list of songs of the collection.
     */
    public ArrayList<DatabaseSong> getSongs() {
        return songs;
    }

    /**
     *
     * @param songs
     * Sets the list of songs of the collection.
     */
    public void setSongs(final ArrayList<DatabaseSong> songs) {
        this.songs = songs;
    }
    /**
     *
     * @return
     * Gets the owner of the collection.
     */

    public String getOwner() {
        return owner;
    }

    /**
     *
     * @param owner
     * Sets the owner of the collection.
     */
    public void setOwner(final String owner) {
        this.owner = owner;
    }
}
