package database.userData.pages;

import database.collectionsOfSongs.DatabaseAlbum;
import database.userData.artistPageElements.Event;
import database.userData.artistPageElements.Merch;

import java.util.ArrayList;
import java.util.stream.Collectors;

public final class ArtistPage extends Page {
    private ArrayList<DatabaseAlbum> albums;
    private ArrayList<Event> events;
    private ArrayList<Merch> merch;

    public ArtistPage() {
        this.albums = new ArrayList<>();
        this.events = new ArrayList<>();
        this.merch = new ArrayList<>();
    }

    /**
     *
     * @return a list of strings containing the albums information
     */
    public ArrayList<String> getAlbumOutput() {
        return albums.stream()
                .map(DatabaseAlbum::getName)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     *
     * @return a list of strings containing the events information
     */
    public ArrayList<String> getEventsOutput() {
        ArrayList<String> eventsOutput = new ArrayList<>();
        for (Event event : events) {
            eventsOutput.add(event.toString());
        }
        return eventsOutput;
    }

    /**
     *
     * @return a list of strings containing the merch information
     */
    public ArrayList<String> getMerchOutput() {
        ArrayList<String> merchOutput = new ArrayList<>();
        for (Merch merch1: merch) {
            merchOutput.add(merch1.toString());
        }
        return merchOutput;
    }

    public ArrayList<DatabaseAlbum> getAlbums() {
        return albums;
    }

    public void setAlbums(final ArrayList<DatabaseAlbum> albums) {
        this.albums = albums;
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public void setEvents(final ArrayList<Event> events) {
        this.events = events;
    }

    public ArrayList<Merch> getMerch() {
        return merch;
    }

    public void setMerch(final ArrayList<Merch> merch) {
        this.merch = merch;
    }
}
