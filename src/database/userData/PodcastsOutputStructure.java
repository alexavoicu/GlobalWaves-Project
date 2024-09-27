package database.userData;

import java.util.ArrayList;

public final class PodcastsOutputStructure {
    private String name;
    private ArrayList<String> episodes;

    public PodcastsOutputStructure(final String name, final ArrayList<String> episodes) {
        this.name = name;
        this.episodes = episodes;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public ArrayList<String> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(final ArrayList<String> episodes) {
        this.episodes = episodes;
    }
}
