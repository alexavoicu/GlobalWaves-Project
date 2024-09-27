package commands.searchBarCommands.searchCommands.searchBarFilters;

import java.util.ArrayList;

public final class SongFilter {
    private String name;
    private ArrayList<String> tags;
    private String album;
    private String lyrics;
    private String genre;
    private String releaseYear;
    private String artist;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }


    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(final ArrayList<String> tags) {
        this.tags = tags;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(final String lyrics) {
        this.lyrics = lyrics;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(final String genre) {
        this.genre = genre;
    }

    public String getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(final String releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(final String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(final String album) {
        this.album = album;
    }
}
