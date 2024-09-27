package database.userData.pages;

import database.userData.hostPageElements.Announcement;
import fileio.input.EpisodeInput;
import fileio.input.PodcastInput;

import java.util.ArrayList;

public final class HostPage extends Page {
    private ArrayList<PodcastInput> podcasts;
    private ArrayList<Announcement> announcements;

    public HostPage() {
        this.podcasts = new ArrayList<>();
        this.announcements = new ArrayList<>();
    }

    /**
     *
     * @return a list of strings containing the podcast information
     */
    public ArrayList<String> getPodcastsOutput() {
        ArrayList<String> podcastsOutput = new ArrayList<>();
        for (PodcastInput podcastInput : podcasts) {
            String podcastStringOutput = podcastInput.getName() + ":\n\t";
            ArrayList<String> episodesOutput = new ArrayList<>();
            for (EpisodeInput episodeInput : podcastInput.getEpisodes()) {
                episodesOutput.add(episodeInput.toString());
            }
            podcastsOutput.add(podcastStringOutput + episodesOutput.toString() + "\n");
        }
        return podcastsOutput;
    }

    /**
     *
     * @return a list of strings containing the announcements information
     */
    public ArrayList<String> getAnnouncementsOutput() {
        ArrayList<String> announcementsOutput = new ArrayList<>();
        for (Announcement announcement : announcements) {
            announcementsOutput.add(announcement.toString());
        }
        return announcementsOutput;
    }

    public ArrayList<PodcastInput> getPodcasts() {
        return podcasts;
    }

    public void setPodcasts(final ArrayList<PodcastInput> podcasts) {
        this.podcasts = podcasts;
    }

    public ArrayList<Announcement> getAnnouncements() {
        return announcements;
    }

    public void setAnnouncements(final ArrayList<Announcement> announcements) {
        this.announcements = announcements;
    }
}
