import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.special.SnapshotResult;
import com.wrapper.spotify.requests.data.playlists.AddTracksToPlaylistRequest;

import java.io.IOException;

public class AddToSpotify {
    String accessToken, playlistId;

    public AddToSpotify (String AccessToken, String PlaylistId) { this.accessToken = AccessToken; this.playlistId = PlaylistId; }

    AddTracksToPlaylistRequest addTracksToPlaylistRequest;

    public void addTracksToPlaylist(String[] uris) {
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setAccessToken(accessToken)
                .build();

        addTracksToPlaylistRequest = spotifyApi.addTracksToPlaylist(playlistId, uris).build();

        try {
            final SnapshotResult snapshotResult = addTracksToPlaylistRequest.execute();
        } catch (IOException | SpotifyWebApiException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
