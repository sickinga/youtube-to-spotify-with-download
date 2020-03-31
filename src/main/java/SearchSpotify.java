import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;

import java.io.IOException;

public class SearchSpotify {
    public SpotifyApi spotifyApi;

    public SearchSpotify(String clientId, String clientSecret) {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();
    }


    public Track[] searchItem(String spotifySearch) throws IOException, SpotifyWebApiException {
        SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks(spotifySearch).build();

        Paging<Track> trackPaging = searchTracksRequest.execute();

        return trackPaging.getItems();
    }
}
