package database.collectionsOfSongs;

import database.DatabaseSong;
import database.userData.AlbumOutputStructure;

import java.util.ArrayList;

public final class DatabaseAlbum extends SongsCollection {
    private String description;
    private int nrOfLikes;

    public DatabaseAlbum(final String name, final ArrayList<DatabaseSong> songs,
                         final String owner, final String description) {
        super(name, songs, owner);
        this.description = description;
        this.nrOfLikes = 0;
    }

    /**
     *
     * @return an album output structure which contains the name and a list of song names
     */

    public AlbumOutputStructure storeAlbumInfo() {
        ArrayList<String> songNames = new ArrayList<>();
        for (DatabaseSong songInput : getSongs()) {
            songNames.add(songInput.getSong().getName());
        }
        return new AlbumOutputStructure(getName(), songNames);
    }

    /**
     * The method calculates the total likes for this album.
     */

    public void calculateTotalLikesForAlbum() {
        for (DatabaseSong song : this.getSongs()) {
            nrOfLikes += song.getNumberOfLikes();
        }
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public int getNrOfLikes() {
        return nrOfLikes;
    }

    public void setNrOfLikes(final int nrOfLikes) {
        this.nrOfLikes = nrOfLikes;
    }
}
