package database.userData;

public final class EpisodePlayingInfo {
    private boolean isRepeat;
    private int timeLeft;
    private String name;
    private int timePassed;
    private boolean isFirst;
    private int prevDuration;

    public EpisodePlayingInfo(final int timeLeft, final String name, final int timePassed,
                              final boolean isFirst, final int prevDuration) {
        this.timeLeft = timeLeft;
        this.name = name;
        this.timePassed = timePassed;
        this.isFirst = isFirst;
        this.prevDuration = prevDuration;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setRepeat(final boolean repeat) {
        isRepeat = repeat;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(final int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getTimePassed() {
        return timePassed;
    }

    public void setTimePassed(final int timepassed) {
        this.timePassed = timepassed;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(final boolean first) {
        isFirst = first;
    }

    public int getPrevDuration() {
        return prevDuration;
    }

    public void setPrevDuration(final int prevDuration) {
        this.prevDuration = prevDuration;
    }
}
