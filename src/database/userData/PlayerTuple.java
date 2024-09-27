package database.userData;

public final class PlayerTuple {
    private boolean isPlaying;
    private int startTime;
    private int timeLeft;
    private int pauseTimestamp;

    public PlayerTuple(final boolean isPlaying, final int startTime, final int timeLeft) {
        this.isPlaying = isPlaying;
        this.startTime = startTime;
        this.timeLeft = timeLeft;
        this.pauseTimestamp = 0;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(final boolean playing) {
        isPlaying = playing;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(final int startTime) {
        this.startTime = startTime;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(final int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public int getPauseTimestamp() {
        return pauseTimestamp;
    }

    public void setPauseTimestamp(final int pauseTimestamp) {
        this.pauseTimestamp = pauseTimestamp;
    }

}
