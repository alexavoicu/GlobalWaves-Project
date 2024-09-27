package database.userData;

import database.DatabaseSong;

public final class SongsCollectionTuple {
    private int passedTimeSong;
    private DatabaseSong song;
    private int timeLeftSong;
    private boolean isFirst;
    private DatabaseSong prevSong;

    public SongsCollectionTuple(final int passedTimeSong, final DatabaseSong song,
                                final int timeLeftSong,
                                final boolean isFirst, final DatabaseSong prevSong) {
        this.passedTimeSong = passedTimeSong;
        this.song = song;
        this.timeLeftSong = timeLeftSong;
        this.isFirst = isFirst;
        this.prevSong = prevSong;
    }

    public int getPassedTimeSong() {
        return passedTimeSong;
    }

    public void setPassedTimeSong(final int passedTimeSong) {
        this.passedTimeSong = passedTimeSong;
    }

    public DatabaseSong getSong() {
        return song;
    }

    public void setSong(final DatabaseSong song) {
        this.song = song;
    }

    public int getTimeLeftSong() {
        return timeLeftSong;
    }

    public void setTimeLeftSong(final int timeLeftSong) {
        this.timeLeftSong = timeLeftSong;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(final boolean first) {
        isFirst = first;
    }

    public DatabaseSong getPrevSong() {
        return prevSong;
    }

    public void setPrevSong(final DatabaseSong prevSong) {
        this.prevSong = prevSong;
    }
}
