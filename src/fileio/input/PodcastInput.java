package fileio.input;

import database.userData.PodcastsOutputStructure;

import java.util.ArrayList;

public final class PodcastInput {
    private String name;
    private String owner;
    private ArrayList<EpisodeInput> episodes;

    public PodcastInput() {
    }

    public PodcastInput(final String name, final String owner,
                        final ArrayList<EpisodeInput> episodes) {
        this.name = name;
        this.owner = owner;
        this.episodes = episodes;
    }

    /**
     *
     * @return the list of episodes information
     */
    public PodcastsOutputStructure storePodcastInfo() {
        ArrayList<String> episodeNames = new ArrayList<>();
        for (EpisodeInput episodeInput : episodes) {
            episodeNames.add(episodeInput.getName());
        }
        return new PodcastsOutputStructure(name, episodeNames);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(final String owner) {
        this.owner = owner;
    }

    public ArrayList<EpisodeInput> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(final ArrayList<EpisodeInput> episodes) {
        this.episodes = episodes;
    }
}
