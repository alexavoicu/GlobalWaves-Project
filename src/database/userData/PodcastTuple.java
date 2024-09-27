package database.userData;

import fileio.input.EpisodeInput;
import fileio.input.PodcastInput;

import java.util.ArrayList;

public final class PodcastTuple {
    private boolean isPaused;
    private int playTimestamp;
    private int timeLeft;
    private ArrayList<Integer> totalTimePerEpisode;
    private int passedTime;

    public PodcastTuple(final boolean isPaused, final int playTimestamp,
                        final PodcastInput podcast) {
        this.isPaused = isPaused;
        this.playTimestamp = playTimestamp;
        this.totalTimePerEpisode = this.createTotalTimePerEpisode(podcast);
        this.timeLeft = totalTimePerEpisode.get(totalTimePerEpisode.size() - 1);
        this.passedTime = 0;
    }

    /**
     *
     * @param podcast playing podcast
     * @return total time playing for each podcast
     */
    public ArrayList<Integer> createTotalTimePerEpisode(final PodcastInput podcast) {
        ArrayList<Integer> timesPerEpisode = new ArrayList<>();
        Integer duration = 0;
        for (EpisodeInput episode : podcast.getEpisodes()) {
            duration += episode.getDuration();
            timesPerEpisode.add(duration);
        }
        return timesPerEpisode;
    }

    /**
     *
     * @param currentTimestamp the command timestamp
     * @param podcast the playing podcast
     * @return time left, episode, time passed, is first, previous episode duration
     */
    public EpisodePlayingInfo getPlayingEpisodeInfo(final int currentTimestamp,
                                                     final PodcastInput podcast) {
        boolean isFirst = false;
        int timeLeftFromEpisode = 0;
        int totalPassedTime = this.passedTime;
        int timePassedFromEpisode = 0;
        int prevDuration = 0;
        int i = 0;
        if (!isPaused) {
            totalPassedTime += currentTimestamp - playTimestamp;
        }
        for (i = 0; i < totalTimePerEpisode.size(); i++) {
            if (totalPassedTime >= 0 && totalPassedTime < totalTimePerEpisode.get(0)) {
                i = 0;
                isFirst = true;
                timePassedFromEpisode = totalPassedTime;
                break;
            }
            if (totalPassedTime >= totalTimePerEpisode.get(totalTimePerEpisode.size() - 2)
                    && totalPassedTime < totalTimePerEpisode.get(totalTimePerEpisode.size() - 1)) {
                i = totalTimePerEpisode.size() - 1;
                timePassedFromEpisode = totalTimePerEpisode.get(totalTimePerEpisode.size() - 1)
                        - totalPassedTime;
                prevDuration = totalTimePerEpisode.get(totalTimePerEpisode.size() - 2)
                        - totalTimePerEpisode.get(totalTimePerEpisode.size() - 2 - 1);
                break;
            }
            if (totalPassedTime >= totalTimePerEpisode.get(i)
                    && totalPassedTime < totalTimePerEpisode.get(i + 1)) {
                timePassedFromEpisode = totalPassedTime - totalTimePerEpisode.get(i);
                if (i == 0) {
                    prevDuration = totalTimePerEpisode.get(i);
                } else {
                    prevDuration = totalTimePerEpisode.get(i) - totalTimePerEpisode.get(i - 1);
                }

                i = i + 1;
                break;
            }
        }
        timeLeftFromEpisode = totalTimePerEpisode.get(i) - totalPassedTime;
        String episodeName = podcast.getEpisodes().get(i).getName();

        return new EpisodePlayingInfo(timeLeftFromEpisode, episodeName,
                timePassedFromEpisode, isFirst, prevDuration);
    }

    /**
     *
     * @param currentTimestamp timestamp of the command
     * @param playingPodcast the podcast playing
     *
     * The method updates the podcast info
     */
    public static void updatePodcastInfo(final int currentTimestamp,
                                         final PodcastTuple playingPodcast) {
        if (playingPodcast.isPaused()) {
            playingPodcast.setPaused(false);
            playingPodcast.setPlayTimestamp(currentTimestamp);
        } else {
            playingPodcast.setPaused(true);
            playingPodcast.setTimeLeft(playingPodcast.getTimeLeft() - currentTimestamp
                    + playingPodcast.getPlayTimestamp());
            playingPodcast.setPassedTime(currentTimestamp - playingPodcast.getPlayTimestamp()
                    + playingPodcast.getPassedTime());

        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(final boolean paused) {
        isPaused = paused;
    }

    public int getPlayTimestamp() {
        return playTimestamp;
    }

    public void setPlayTimestamp(final int playTimestamp) {
        this.playTimestamp = playTimestamp;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(final int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public ArrayList<Integer> getTotalTimePerEpisode() {
        return totalTimePerEpisode;
    }

    public void setTotalTimePerEpisode(final ArrayList<Integer> totalTimePerEpisode) {
        this.totalTimePerEpisode = totalTimePerEpisode;
    }

    public int getPassedTime() {
        return passedTime;
    }

    public void setPassedTime(final int passedTime) {
        this.passedTime = passedTime;
    }
}
