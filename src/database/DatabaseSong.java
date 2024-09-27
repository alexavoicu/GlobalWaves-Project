package database;

import fileio.input.SongInput;

public final class DatabaseSong {
    private SongInput song;
    private Integer numberOfLikes;

    public DatabaseSong(final SongInput song) {
        this.song = song;
        this.numberOfLikes = 0;
    }

    public SongInput getSong() {
        return song;
    }

    public void setSong(final SongInput song) {
        this.song = song;
    }

    public Integer getNumberOfLikes() {
        return numberOfLikes;
    }

    public void setNumberOfLikes(final Integer numberOfLikes) {
        this.numberOfLikes = numberOfLikes;
    }
}
