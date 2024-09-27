package commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import commands.adminCommands.AddUserCommand;
import commands.adminCommands.DeleteUserCommand;
import commands.adminCommands.ShowAlbumsCommand;
import commands.adminCommands.ShowPodcastsCommand;
import commands.normalUsersCommands.*;
import commands.specialUsersCommands.artistCommands.AddAlbumCommand;
import commands.specialUsersCommands.artistCommands.AddEventCommand;
import commands.specialUsersCommands.artistCommands.AddMerchCommand;
import commands.specialUsersCommands.artistCommands.RemoveAlbumCommand;
import commands.specialUsersCommands.artistCommands.RemoveEventCommand;
import commands.fwBwCommands.BackwardCommand;
import commands.fwBwCommands.ForwardCommand;
import commands.generalStatisticsCommands.GetAllUsersCommand;
import commands.generalStatisticsCommands.GetOnlineUsersCommand;
import commands.generalStatisticsCommands.GetTop5AlbumsCommand;
import commands.generalStatisticsCommands.GetTop5ArtistsCommand;
import commands.generalStatisticsCommands.GetTop5PlaylistsCommand;
import commands.generalStatisticsCommands.GetTop5SongsCommand;
import commands.specialUsersCommands.hostCommands.AddAnnouncementCommand;
import commands.specialUsersCommands.hostCommands.AddPodcastCommand;
import commands.specialUsersCommands.hostCommands.RemoveAnnouncementCommand;
import commands.specialUsersCommands.hostCommands.RemovePodcastCommand;
import commands.pageSystemCommands.ChangePageCommand;
import commands.pageSystemCommands.PrintCurrentPageCommand;
import commands.playlistCommands.AddRemoveInPlaylistCommand;
import commands.playlistCommands.CreatePlaylistCommand;
import commands.playlistCommands.FollowPlaylistCommand;
import commands.playlistCommands.ShowPlaylistsCommand;
import commands.playlistCommands.SwitchVisibilityCommand;
import commands.searchBarCommands.SelectCommand;
import commands.searchBarCommands.searchCommands.AlbumSearchCommand;
import commands.searchBarCommands.searchCommands.ArtistSearchCommand;
import commands.searchBarCommands.searchCommands.HostSearchCommand;
import commands.searchBarCommands.searchCommands.PlaylistSearchCommand;
import commands.searchBarCommands.searchCommands.PodcastSearchCommand;
import commands.searchBarCommands.searchCommands.SongSearchCommand;

public final class CommandFactory {

    private CommandFactory() {
    }

    /**
     *
     * @param command input command read
     * @param readMapper objectMapper
     * @return a general Command object that can be particularized for every specific command
     *
     * The method reads all the input commands and based on the type, deserializes the JSON data
     * into an instance of the corresponding command class.
     */
    public static Command generateCommand(final JsonNode command, final ObjectMapper readMapper)
            throws JsonProcessingException {
        String currentCommand = command.get("command").asText();

        return switch (currentCommand) {
            case "search" -> generateSearchCommand(command, readMapper);
            case "select" -> readMapper.treeToValue(command, SelectCommand.class);
            case "load" -> readMapper.treeToValue(command, LoadCommand.class);
            case "playPause" -> readMapper.treeToValue(command, PlayPauseCommand.class);
            case "repeat" -> readMapper.treeToValue(command, RepeatCommand.class);
            case "shuffle" -> readMapper.treeToValue(command, ShuffleCommand.class);
            case "forward" -> readMapper.treeToValue(command, ForwardCommand.class);
            case "backward" -> readMapper.treeToValue(command, BackwardCommand.class);
            case "like" -> readMapper.treeToValue(command, LikeCommand.class);
            case "next" -> readMapper.treeToValue(command, NextCommand.class);
            case "prev" -> readMapper.treeToValue(command, PrevCommand.class);
            case "addRemoveInPlaylist" -> readMapper.treeToValue(command,
                    AddRemoveInPlaylistCommand.class);
            case "status" -> readMapper.treeToValue(command, StatusCommand.class);
            case "createPlaylist" -> readMapper.treeToValue(command, CreatePlaylistCommand.class);
            case "switchVisibility" -> readMapper.treeToValue(command,
                    SwitchVisibilityCommand.class);
            case "follow" -> readMapper.treeToValue(command, FollowPlaylistCommand.class);
            case "showPlaylists" -> readMapper.treeToValue(command, ShowPlaylistsCommand.class);
            case "showPreferredSongs" -> readMapper.treeToValue(command,
                    ShowPreferredSongsCommand.class);
            case "getTop5Songs" -> readMapper.treeToValue(command, GetTop5SongsCommand.class);
            case "getTop5Playlists" -> readMapper.treeToValue(command,
                    GetTop5PlaylistsCommand.class);
            case "addUser" -> readMapper.treeToValue(command, AddUserCommand.class);
            case "deleteUser" -> readMapper.treeToValue(command, DeleteUserCommand.class);
            case "showAlbums" -> readMapper.treeToValue(command, ShowAlbumsCommand.class);
            case "showPodcasts" -> readMapper.treeToValue(command, ShowPodcastsCommand.class);
            case "addAlbum" -> readMapper.treeToValue(command, AddAlbumCommand.class);
            case "removeAlbum" -> readMapper.treeToValue(command, RemoveAlbumCommand.class);
            case "addEvent" -> readMapper.treeToValue(command, AddEventCommand.class);
            case "removeEvent" -> readMapper.treeToValue(command, RemoveEventCommand.class);
            case "addMerch" -> readMapper.treeToValue(command, AddMerchCommand.class);
            case "addPodcast" -> readMapper.treeToValue(command, AddPodcastCommand.class);
            case "removePodcast" -> readMapper.treeToValue(command, RemovePodcastCommand.class);
            case "addAnnouncement" -> readMapper
                    .treeToValue(command, AddAnnouncementCommand.class);
            case "removeAnnouncement" -> readMapper
                    .treeToValue(command, RemoveAnnouncementCommand.class);
            case "switchConnectionStatus" -> readMapper
                    .treeToValue(command, SwitchConnectionStatusCommand.class);
            case "getTop5Albums" -> readMapper.treeToValue(command, GetTop5AlbumsCommand.class);
            case "getTop5Artists" -> readMapper.treeToValue(command, GetTop5ArtistsCommand.class);
            case "getAllUsers" -> readMapper.treeToValue(command, GetAllUsersCommand.class);
            case "getOnlineUsers" -> readMapper.treeToValue(command, GetOnlineUsersCommand.class);
            case "changePage" -> readMapper.treeToValue(command, ChangePageCommand.class);
            case "printCurrentPage" -> readMapper
                    .treeToValue(command, PrintCurrentPageCommand.class);
            default -> null;
        };
    }

    /**
     *
     * @param command input command read
     * @param readMapper objectMapper
     * @return a SearchCommand object that can be particularized for every specific search command
     * @throws JsonProcessingException
     *
     * The method reads all the search input commands and based on the type, deserializes the JSON
     * data into an instance of the corresponding search command class.
     */
    private static Command generateSearchCommand(final JsonNode command,
                                                 final ObjectMapper readMapper)
            throws JsonProcessingException {
        return switch (command.get("type").asText()) {
            case "song" -> readMapper.treeToValue(command, SongSearchCommand.class);
            case "playlist" -> readMapper.treeToValue(command, PlaylistSearchCommand.class);
            case "podcast" -> readMapper.treeToValue(command, PodcastSearchCommand.class);
            case "album" -> readMapper.treeToValue(command, AlbumSearchCommand.class);
            case "artist" -> readMapper.treeToValue(command, ArtistSearchCommand.class);
            case "host" -> readMapper.treeToValue(command, HostSearchCommand.class);
            default -> null;
        };
    }
}
