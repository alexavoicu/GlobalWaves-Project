package database.collectionsOfSongs;


import database.DatabaseSong;

import java.util.ArrayList;

public final class DatabasePlaylist extends SongsCollection {
    private Boolean isPrivate;
    private Integer nrOfFollows;
    private Integer creationTime;


    public DatabasePlaylist(final String name, final ArrayList<DatabaseSong> songs,
                            final String owner, final Integer creationTime) {
        super(name, songs, owner);
        this.creationTime = creationTime;
        this.nrOfFollows = 0;
        this.isPrivate = false;
    }

    /**
     *
     * @param album the album
     * @return if the playlist contains any songs from the album
     */

    public boolean containsSongsFromAlbum(final String album) {
        for (DatabaseSong song : getSongs()) {
            if (song.getSong().getAlbum().equals(album)) {
                return true;
            }
        }
        return false;
    }

    public Boolean getPrivate() {
        return isPrivate;
    }

    public void setPrivate(final Boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public Integer getNrOfFollows() {
        return nrOfFollows;
    }

    public void setNrOfFollows(final Integer nrOfFollows) {
        this.nrOfFollows = nrOfFollows;
    }

    public Integer getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(final Integer creationTime) {
        this.creationTime = creationTime;
    }



}
