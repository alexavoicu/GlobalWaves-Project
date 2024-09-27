package database.userData;

import java.util.ArrayList;

public final class AlbumOutputStructure {
    private String name;
    private ArrayList<String> songs;

    public AlbumOutputStructure(final String name, final ArrayList<String> songs) {
        this.name = name;
        this.songs = songs;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public ArrayList<String> getSongs() {
        return songs;
    }

    public void setSongs(final ArrayList<String> songs) {
        this.songs = songs;
    }
}
