package commands.pageSystemCommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import database.Database;
import database.userData.ArtistUser;
import database.userData.DatabaseUser;
import database.userData.HostUser;
import database.userData.pages.HomePage;
import database.userData.pages.LikedContentPage;

import java.util.HashMap;

public final class PrintCurrentPageCommand extends Command {
    private String username;

    /**
     *
     * @param user the user
     * @param artists the list of artists
     * @param hosts the list of hosts
     *
     * The method verifies if the user is online and if he is, then it treats every case of the
     * page and creates the output accordingly.
     */

    public void execute(final DatabaseUser user, final HashMap<String, ArtistUser> artists,
                        final HashMap<String, HostUser> hosts) {
        ObjectMapper currentMapper = new ObjectMapper();
        ObjectNode node = currentMapper.createObjectNode();
        node.set("command", currentMapper.convertValue("printCurrentPage", JsonNode.class));
        node.set("user", currentMapper.convertValue(username, JsonNode.class));
        node.set("timestamp", currentMapper.convertValue(getTimestamp(), JsonNode.class));

        if (!user.isOnline()) {
            node.set("message", currentMapper.convertValue(username
                    + " is offline.", JsonNode.class));
            Database.outputs.add(node);
            return;
        }

        switch (user.getCurrentPage()) {
            case "artist page" -> {
                ArtistUser currentArtist = artists.get(user.getOwnerOfCurrentPage());
                node.set("message", currentMapper.convertValue("Albums:\n\t"
                        + currentArtist.getPage().getAlbumOutput() + "\n\nMerch:\n\t"
                        + currentArtist.getPage().getMerchOutput() + "\n\nEvents:\n\t"
                        + currentArtist.getPage().getEventsOutput(), JsonNode.class));
            }
            case "host page" -> {
                HostUser currentHost = hosts.get(user.getOwnerOfCurrentPage());
                node.set("message", currentMapper.convertValue("Podcasts:\n\t"
                        + currentHost.getPage().getPodcastsOutput() + "\n\nAnnouncements:\n\t"
                        + currentHost.getPage().getAnnouncementsOutput(), JsonNode.class));
            }
            case "Home" -> {
                HomePage homePage = user.getHomePage();
                homePage.findRecommendations(user);
                node.set("message", currentMapper.convertValue("Liked songs:\n\t"
                        + homePage.getTop5LikedSongs() + "\n\nFollowed playlists:\n\t"
                        + homePage.getTop5PlaylistsFollowed(), JsonNode.class));
            }
            case "LikedContent" -> {
                LikedContentPage likedContentPage = user.getLikedContentPage();
                likedContentPage.getLikedContent(user);
                node.set("message", currentMapper.convertValue("Liked songs:\n\t"
                        + likedContentPage.getLikedSongs() + "\n\nFollowed playlists:\n\t"
                        + likedContentPage.getFollowedPlaylists(), JsonNode.class));
            }
            default -> {
                return;
            }
        }

        Database.outputs.add(node);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }
}
