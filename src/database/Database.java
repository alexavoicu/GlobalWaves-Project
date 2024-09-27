package database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import commands.LikeCommand;
import commands.LoadCommand;
import commands.NextCommand;
import commands.PlayPauseCommand;
import commands.PrevCommand;
import commands.RepeatCommand;
import commands.normalUsersCommands.ShowPreferredSongsCommand;
import commands.ShuffleCommand;
import commands.StatusCommand;
import commands.adminCommands.AddUserCommand;
import commands.adminCommands.DeleteUserCommand;
import commands.adminCommands.ShowAlbumsCommand;
import commands.adminCommands.ShowPodcastsCommand;
import commands.fwBwCommands.BackwardCommand;
import commands.fwBwCommands.ForwardCommand;
import commands.generalStatisticsCommands.GetAllUsersCommand;
import commands.generalStatisticsCommands.GetOnlineUsersCommand;
import commands.generalStatisticsCommands.GetTop5AlbumsCommand;
import commands.generalStatisticsCommands.GetTop5PlaylistsCommand;
import commands.generalStatisticsCommands.GetTop5SongsCommand;
import commands.normalUsersCommands.SwitchConnectionStatusCommand;
import commands.pageSystemCommands.ChangePageCommand;
import commands.pageSystemCommands.PrintCurrentPageCommand;
import commands.searchBarCommands.searchCommands.AlbumSearchCommand;
import commands.searchBarCommands.searchCommands.ArtistSearchCommand;
import commands.searchBarCommands.searchCommands.HostSearchCommand;
import commands.searchBarCommands.searchCommands.PlaylistSearchCommand;
import commands.searchBarCommands.searchCommands.PodcastSearchCommand;
import commands.searchBarCommands.searchCommands.SearchCommand;
import commands.searchBarCommands.searchCommands.SongSearchCommand;
import commands.specialUsersCommands.artistCommands.AddAlbumCommand;
import commands.specialUsersCommands.artistCommands.AddEventCommand;
import commands.specialUsersCommands.artistCommands.AddMerchCommand;
import commands.specialUsersCommands.artistCommands.RemoveAlbumCommand;
import commands.specialUsersCommands.artistCommands.RemoveEventCommand;
import commands.playlistCommands.SwitchVisibilityCommand;
import commands.playlistCommands.ShowPlaylistsCommand;
import commands.playlistCommands.AddRemoveInPlaylistCommand;
import commands.playlistCommands.CreatePlaylistCommand;
import commands.playlistCommands.FollowPlaylistCommand;
import commands.searchBarCommands.SelectCommand;
import commands.specialUsersCommands.hostCommands.AddAnnouncementCommand;
import commands.specialUsersCommands.hostCommands.AddPodcastCommand;
import commands.specialUsersCommands.hostCommands.RemoveAnnouncementCommand;
import commands.specialUsersCommands.hostCommands.RemovePodcastCommand;
import database.collectionsOfSongs.DatabaseAlbum;
import database.collectionsOfSongs.DatabasePlaylist;
import database.userData.ArtistUser;
import database.userData.DatabaseUser;
import database.userData.HostUser;
import fileio.input.PodcastInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public final class Database {
    private static Database instance = null;
    private ArrayList<Command> commands;
    public static ArrayNode outputs;
    private ArrayList<DatabaseSong> databaseSongs;
    private ArrayList<PodcastInput> podcastInputs;
    private ArrayList<DatabasePlaylist> playlists;
    private HashMap<String, DatabaseUser> users;
    private HashMap<String, ArtistUser> artists;
    private HashMap<String, HostUser> hosts;
    private ArrayList<DatabaseAlbum> albums;

    private Database() { }

    /**
     *
     * @return
     *
     * Gets the instance of the object.
     */
    public static Database getDatabase() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    /**
     *
     * @param commandInput stores all the commands given
     * @param output will store the output
     * @param songs stores all the songs from input
     * @param podcasts stores all the podcasts from input
     * @param allUsers stores all the users from input
     *
     * Initializes the database with the input data.
     */
    public void initializeDatabase(final ArrayList<Command> commandInput,
                                   final ArrayNode output,
                                   final ArrayList<DatabaseSong> songs,
                                   final ArrayList<PodcastInput> podcasts,
                                   final HashMap<String, DatabaseUser> allUsers) {
        this.commands = commandInput;
        Database.outputs = output;
        this.databaseSongs = songs;
        this.podcastInputs = podcasts;
        this.users = allUsers;
        this.playlists = new ArrayList<>();
        this.albums = new ArrayList<>();
        this.artists = new LinkedHashMap<>();
        this.hosts = new LinkedHashMap<>();
    }


    /**
     * Iterates through all the commands and calls their respective execute method.
     */

    public void query() {
        for (Command command : this.commands) {
            for (DatabaseUser user : this.users.values()) {
                if (user.isOnline()) {
                    user.getDatabaseMusicPlayer().clearMusicPlayer(command.getTimestamp());
                }
            }

            switch (command.getCommand()) {
                case "search" -> {
                    switch (((SearchCommand) command).getType()) {
                        case "song" -> executeSongSearch(command);
                        case "playlist" -> executePlaylistSearch(command);
                        case "podcast" -> executePodcastSearch(command);
                        case "artist" -> executeArtistSearch(command);
                        case "host" -> executeHostSearch(command);
                        case "album" -> executeAlbumSearch(command);
                        default -> {
                            return;
                        }
                    }
                }
                case "select" -> executeSelect(command);
                case "load" -> executeLoad(command);
                case "createPlaylist" -> executeCreatePlaylist(command);
                case "playPause" -> executePlayPause(command);
                case "status" -> executeStatus(command);
                case "addRemoveInPlaylist" -> executeAddRemoveInPlaylist(command);
                case "like" -> executeLikeCommand(command);
                case "showPlaylists" -> executeShowPlaylistCommand(command);
                case "showPreferredSongs" -> executeShowPreferredSongsCommand(command);
                case "follow" -> executeFollowCommand(command);
                case "switchVisibility" -> executeSwitchVisibilityCommand(command);
                case "getTop5Playlists" -> executeGetTop5Playlists(command);
                case "getTop5Albums" -> executeGetTop5Albums(command);
                case "getTop5Songs" -> executeGetTop5Songs(command);
                case "next" -> executeNextCommand(command);
                case "prev" -> executePrevCommand(command);
                case "forward" -> executeForwardCommand(command);
                case "backward" -> executeBackwardCommand(command);
                case "shuffle" -> executeShuffleCommand(command);
                case "repeat" -> executeRepeatCommand(command);
                case "switchConnectionStatus" -> executeSwitchConnectionStatusCommand(command);
                case "getOnlineUsers" -> executeGetOnlineUsersCommand(command);
                case "getAllUsers" -> executeGetAllUsersCommand(command);
                case "addUser" -> executeAddUserCommand(command);
                case "addAlbum" -> executeAddAlbumCommand(command);
                case "removeAlbum" -> executeRemoveAlbumCommand(command);
                case "removeEvent" -> executeRemoveEventCommand(command);
                case "showAlbums" -> executeShowAlbumsCommand(command);
                case "showPodcasts" -> executeShowPodcastsCommand(command);
                case "printCurrentPage" -> executePrintCurrentPageCommand(command);
                case "changePage" -> executeChangePageCommand(command);
                case "addEvent" -> executeAddEventCommand(command);
                case "addMerch" -> executeAddMerchCommand(command);
                case "addPodcast" -> executeAddPodcastCommand(command);
                case "addAnnouncement" -> executeAddAnnouncementCommand(command);
                case "removeAnnouncement" -> executeRemoveAnnouncementCommand(command);
                case "deleteUser" -> executeDeleteUser(command);
                case "removePodcast" -> executeRemovePodcastCommand(command);
                default -> {
                    return;
                }
            }
        }
        return;
    }

    /**
     *
     * @param command current command
     * @param user user giving the command
     * @return if the user is online or not
     *
     * This method is designed to verify if the user is online and the command can be processed.
     */
    public boolean verifyOnline(final Command command, final DatabaseUser user) {
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue(command.getCommand(), JsonNode.class));
        node.set("user", currentMapper.convertValue(user.getUserInput()
                .getUsername(), JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(command.getTimestamp(), JsonNode.class));
        if (!user.isOnline()) {
            node.set("message", currentMapper.convertValue(user.getUserInput()
                    .getUsername()
                    + " is offline.", JsonNode.class));
            Database.outputs.add(node);
            return false;
        }
        return true;
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Delete User Command.
     */
    public void executeDeleteUser(final Command command) {
        ((DeleteUserCommand) command).execute(users, artists, hosts, albums,
                databaseSongs, playlists, podcastInputs);
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Artist Search Command.
     */
    public void executeArtistSearch(final Command command) {
        ((ArtistSearchCommand) command).execute(artists, users
                .get(((ArtistSearchCommand) command)
                        .getUsername()));
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Repeat Command.
     */
    public void executeRepeatCommand(final Command command) {
        if (verifyOnline(command, users
                .get(((RepeatCommand) command).getUsername()))) {
            ((RepeatCommand) command).execute(users
                    .get(((RepeatCommand) command)
                            .getUsername()), databaseSongs, albums, playlists);
        }
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Host Search Command.
     */
    public void executeHostSearch(final Command command) {
        if (verifyOnline(command, users
                .get(((HostSearchCommand) command).getUsername()))) {
            ((HostSearchCommand) command).execute(hosts, users
                    .get(((HostSearchCommand) command)
                            .getUsername()));
        }
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Album Search Command.
     */
    public void executeAlbumSearch(final Command command) {
        if (verifyOnline(command, users
                .get(((AlbumSearchCommand) command).getUsername()))) {
            ((AlbumSearchCommand) command).execute(albums, users
                    .get(((AlbumSearchCommand) command)
                            .getUsername()));
        }
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Add Event Command.
     */
    public void executeAddEventCommand(final Command command) {
        ((AddEventCommand) command).execute(users, artists);
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Add Merch Command.
     */
    public void executeAddMerchCommand(final Command command) {
        ((AddMerchCommand) command).execute(users, artists);
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Print Current Page Command.
     */
    public void executePrintCurrentPageCommand(final Command command) {
        if (verifyOnline(command, users
                .get(((PrintCurrentPageCommand) command).getUsername()))) {
            ((PrintCurrentPageCommand) command).execute(users
                    .get(((PrintCurrentPageCommand) command)
                            .getUsername()), artists, hosts);
        }
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Change Page Command.
     */
    public void executeChangePageCommand(final Command command) {
        if (verifyOnline(command, users
                .get(((ChangePageCommand) command).getUsername()))) {
            ((ChangePageCommand) command).execute(users
                    .get(((ChangePageCommand) command)
                            .getUsername()));
        }
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Show Albums Command.
     */
    public void executeShowAlbumsCommand(final Command command) {
        ((ShowAlbumsCommand) command).execute(artists
                .get(((ShowAlbumsCommand) command)
                .getUsername()));
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Show Podcasts Command.
     */
    public void executeShowPodcastsCommand(final Command command) {
        ((ShowPodcastsCommand) command).execute(hosts
                .get(((ShowPodcastsCommand) command)
                        .getUsername()));
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Add Album Command.
     */
    public void executeAddAlbumCommand(final Command command) {
        ((AddAlbumCommand) command).execute(users, artists, albums, databaseSongs);
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Remove Album Command.
     */
    public void executeRemoveAlbumCommand(final Command command) {
        ((RemoveAlbumCommand) command).execute(users, artists, albums, playlists);
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Remove Event Command.
     */
    public void executeRemoveEventCommand(final Command command) {
        ((RemoveEventCommand) command).execute(users, artists);
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Add Podcast Command.
     */
    public void executeAddPodcastCommand(final Command command) {
        ((AddPodcastCommand) command).execute(users, hosts, podcastInputs);
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Add Announcement Command.
     */
    public void executeAddAnnouncementCommand(final Command command) {
        ((AddAnnouncementCommand) command).execute(users, hosts);
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Remove Announcement Command.
     */
    public void executeRemoveAnnouncementCommand(final Command command) {
        ((RemoveAnnouncementCommand) command).execute(users, hosts);
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Remove Podcast Command.
     */
    public void executeRemovePodcastCommand(final Command command) {
        ((RemovePodcastCommand) command).execute(users, hosts, podcastInputs);
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Add User Command.
     */
    public void executeAddUserCommand(final Command command) {
        ((AddUserCommand) command).execute(users, artists, hosts);
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Get Online Users Command.
     */
    public void executeGetOnlineUsersCommand(final Command command) {
        ((GetOnlineUsersCommand) command).execute(users);
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Get All Users Command.
     */
    public void executeGetAllUsersCommand(final Command command) {
        ((GetAllUsersCommand) command).execute(users);
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Switch Connection Status Command.
     */
    public void executeSwitchConnectionStatusCommand(final Command command) {
        ((SwitchConnectionStatusCommand) command).execute(users);
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Shuffle Command.
     */

    public void executeShuffleCommand(final Command command) {
        if (verifyOnline(command, users
                .get(((ShuffleCommand) command).getUsername()))) {
            ((ShuffleCommand) command).execute(users.get(((ShuffleCommand) command)
                    .getUsername()), playlists, albums);
        }
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Backward Command.
     */

    public void executeBackwardCommand(final Command command) {
        if (verifyOnline(command, users
                .get(((BackwardCommand) command).getUsername()))) {
            ((BackwardCommand) command).execute(users.get(((BackwardCommand) command)
                    .getUsername()), podcastInputs);
        }
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Forward Command.
     */
    public void executeForwardCommand(final Command command) {
        if (verifyOnline(command, users
                .get(((ForwardCommand) command).getUsername()))) {
            ((ForwardCommand) command).execute(users.get(((ForwardCommand) command)
                    .getUsername()), podcastInputs);
        }
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Prev Command.
     */
    public void executePrevCommand(final Command command) {
        if (verifyOnline(command, users
                .get(((PrevCommand) command).getUsername()))) {
            ((PrevCommand) command).execute(users.get(((PrevCommand) command)
                    .getUsername()), playlists, podcastInputs, databaseSongs, albums);
        }
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Shuffle Command.
     */
    public void executeNextCommand(final Command command) {
        if (verifyOnline(command, users
                .get(((NextCommand) command).getUsername()))) {
            ((NextCommand) command).execute(users.get(((NextCommand) command)
                    .getUsername()), playlists, podcastInputs, albums);
        }
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the GetTop5Songs Command.
     */
    public void executeGetTop5Songs(final Command command) {

        ((GetTop5SongsCommand) command).execute(databaseSongs);
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the GetTop5Playlists Command.
     */
    public void executeGetTop5Playlists(final Command command) {
        ((GetTop5PlaylistsCommand) command).execute(playlists);
    }
    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Get Top 5 Albums Command.
     */

    public void executeGetTop5Albums(final Command command) {
        ((GetTop5AlbumsCommand) command).execute(albums);
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the SwitchVisibility Command.
     */
    public void executeSwitchVisibilityCommand(final Command command) {
        if (verifyOnline(command, users
                .get(((SwitchVisibilityCommand) command).getUsername()))) {
            ((SwitchVisibilityCommand) command).execute(users);
        }
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Follow Command.
     */
    public void executeFollowCommand(final Command command) {
        if (verifyOnline(command, users
                .get(((FollowPlaylistCommand) command).getUsername()))) {
            ((FollowPlaylistCommand) command).execute(users, playlists);
        }
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the ShowPreferredSongs Command.
     */
    public void executeShowPreferredSongsCommand(final Command command) {
        if (verifyOnline(command, users
                .get(((ShowPreferredSongsCommand) command).getUsername()))) {
            ((ShowPreferredSongsCommand) command).execute(users);
        }
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the ShowPlaylist Command.
     */
    public void executeShowPlaylistCommand(final Command command) {
        if (verifyOnline(command, users
                .get(((ShowPlaylistsCommand) command).getUsername()))) {
            ((ShowPlaylistsCommand) command).execute(playlists);
        }
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Like Command.
     */
    public void executeLikeCommand(final Command command) {
        if (verifyOnline(command, users
                .get(((LikeCommand) command).getUsername()))) {
            ((LikeCommand) command).execute(users, databaseSongs, playlists, albums);
        }
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the AddRemoveInPlaylist Command.
     */
    public void executeAddRemoveInPlaylist(final Command command) {
        if (verifyOnline(command, users
                .get(((AddRemoveInPlaylistCommand) command).getUsername()))) {
            ((AddRemoveInPlaylistCommand) command).execute(databaseSongs, users, albums);
        }
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Status Command.
     */
    public void executeStatus(final Command command) {
        ((StatusCommand) command).execute(users, podcastInputs, playlists, albums, databaseSongs);
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the PlayPause Command.
     */

    public void executePlayPause(final Command command) {
        if (verifyOnline(command, users
                .get(((PlayPauseCommand) command).getUsername()))) {
            ((PlayPauseCommand) command).execute(users);
        }
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the CreatePlaylist Command.
     */

    public void executeCreatePlaylist(final Command command) {
        if (verifyOnline(command, users
                .get(((CreatePlaylistCommand) command).getUsername()))) {
            ((CreatePlaylistCommand) command)
                    .execute(users, playlists);
        }
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Load Command.
     */
    public void executeLoad(final Command command) {
        if (verifyOnline(command, users
                .get(((LoadCommand) command).getUsername()))) {
            ((LoadCommand) command)
                    .execute(users, playlists, databaseSongs, podcastInputs, albums);
        }
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the SongSearch Command.
     */
    public void executeSongSearch(final Command command) {
        ((SongSearchCommand) command)
                .execute(databaseSongs, users
                        .get(((SongSearchCommand) command)
                                .getUsername()));
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the PlaylistSearch Command.
     */
    public void executePlaylistSearch(final Command command) {
        ((PlaylistSearchCommand) command)
                .execute(playlists, users
                        .get(((PlaylistSearchCommand) command)
                                .getUsername()));
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the PodcastSearch Command.
     */
    public void executePodcastSearch(final Command command) {
        ((PodcastSearchCommand) command)
                .execute(podcastInputs, users
                        .get(((PodcastSearchCommand) command)
                                .getUsername()));
    }

    /**
     *
     * @param command given command
     *
     * Calls the execute method for the Select Command.
     */
    public void executeSelect(final Command command) {
        if (verifyOnline(command, users
                .get(((SelectCommand) command).getUsername()))) {
            ((SelectCommand) command).execute(users.get(((SelectCommand) command)
                    .getUsername()));
        }
    }


    public ArrayList<Command> getCommands() {
        return commands;
    }

    public void setCommands(final ArrayList<Command> commands) {
        this.commands = commands;
    }

    public ArrayNode getOutputs() {
        return outputs;
    }
    public ArrayList<DatabaseSong> getDatabaseSongs() {
        return databaseSongs;
    }

    public void setDatabaseSongs(final ArrayList<DatabaseSong> databaseSongs) {
        this.databaseSongs = databaseSongs;
    }

    public ArrayList<PodcastInput> getPodcastInputs() {
        return podcastInputs;
    }

    public void setPodcastInputs(final ArrayList<PodcastInput> podcastInputs) {
        this.podcastInputs = podcastInputs;
    }

    public ArrayList<DatabasePlaylist> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(final ArrayList<DatabasePlaylist> playlists) {
        this.playlists = playlists;
    }

    public HashMap<String, DatabaseUser> getUsers() {
        return users;
    }

    public void setUsers(final HashMap<String, DatabaseUser> users) {
        this.users = users;
    }
}
