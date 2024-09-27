package database.userData;

public final class DatabaseMusicPlayer {
    private String loadedType;
    private String loaded;
    private String audioPlayingOwner;
    private PlayerTuple currentStatus;
    private int repeatStatus;
    private boolean isShuffled;

    public DatabaseMusicPlayer() {
        this.repeatStatus = 0;
        this.isShuffled = false;
    }

    /**
     *
     * @param timestamp time of the command given
     *
     * The method verifies if the loaded audio has finished playing and eliminates it from the
     * player.
     */
    public void clearMusicPlayer(final int timestamp) {
        if (this.currentStatus != null
                && this.currentStatus.getTimeLeft()
                - (timestamp - this.currentStatus.getStartTime()) <= 0) {
            this.loaded = null;
            this.currentStatus.setTimeLeft(0);
            this.repeatStatus = 0;
            this.isShuffled = false;
            this.currentStatus.setPlaying(false);
            this.audioPlayingOwner = null;
        }

        if (loadedType != null && loadedType.equals("song") && repeatStatus == 1
                && timestamp - currentStatus.getStartTime() > currentStatus.getTimeLeft() / 2) {
            repeatStatus = 0;
        }
    }

    public String getLoadedType() {
        return loadedType;
    }

    public void setLoadedType(final String loadedType) {
        this.loadedType = loadedType;
    }

    public String getLoaded() {
        return loaded;
    }

    public void setLoaded(final String loaded) {
        this.loaded = loaded;
    }

    public PlayerTuple getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(final PlayerTuple currentStatus) {
        this.currentStatus = currentStatus;
    }

    public int getRepeatStatus() {
        return repeatStatus;
    }

    public void setRepeatStatus(final int repeatStatus) {
        this.repeatStatus = repeatStatus;
    }

    public boolean isShuffled() {
        return isShuffled;
    }

    public void setShuffled(final boolean shuffled) {
        isShuffled = shuffled;
    }

    public String getAudioPlayingOwner() {
        return audioPlayingOwner;
    }

    public void setAudioPlayingOwner(final String audioPlayingOwner) {
        this.audioPlayingOwner = audioPlayingOwner;
    }
}
