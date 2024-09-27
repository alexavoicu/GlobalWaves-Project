package database.userData;

import database.DatabaseSong;

public final class RepeatSongsCollectionTuple {
    private DatabaseSong repeatedSong;
    private int timestampWhenSongStartedPlaying;

    public RepeatSongsCollectionTuple(final DatabaseSong repeatedSong,
                                      final int timestampWhenSongStartedPlaying) {
        this.repeatedSong = repeatedSong;
        this.timestampWhenSongStartedPlaying = timestampWhenSongStartedPlaying;
    }

    public DatabaseSong getRepeatedSong() {
        return repeatedSong;
    }

    public void setRepeatedSong(final DatabaseSong repeatedSong) {
        this.repeatedSong = repeatedSong;
    }

    public int getTimestampWhenSongStartedPlaying() {
        return timestampWhenSongStartedPlaying;
    }

    public void setTimestampWhenSongStartedPlaying(final int timestampWhenSongStartedPlaying) {
        this.timestampWhenSongStartedPlaying = timestampWhenSongStartedPlaying;
    }
}
