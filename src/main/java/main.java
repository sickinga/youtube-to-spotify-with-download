import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.plus.Plus;
import com.sapher.youtubedl.YoutubeDLException;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import org.farng.mp3.TagException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.security.GeneralSecurityException;
import java.text.Normalizer;
import java.util.*;


public class main {
    private static final String CLIENT_SECRETS = "client_secret.json";
    private static final Collection<String> SCOPES =
            Arrays.asList("https://www.googleapis.com/auth/youtube.force-ssl");
    private static String DEVELOPER_KEY;
    private static final String APPLICATION_NAME = "YoutubeSpotify";
    private static final File DATA_STORE_DIR = new File("youtubeToken.log");
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static FileDataStoreFactory dataStoreFactory;
    private static Plus plus;

    public static Credential authorize(final NetHttpTransport httpTransport) throws IOException {
        // Load client secrets.
        InputStream in = main.class.getResourceAsStream(CLIENT_SECRETS);
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // set up authorization code flow
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,SCOPES)
                        .setDataStoreFactory(dataStoreFactory)
                        .build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    public static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static YouTube getServiceAuthorized() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
        Credential credential = authorize(httpTransport);
        plus = new Plus.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(
                APPLICATION_NAME).build();
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private static String convertVideoTitle (String videoTitle) {
        videoTitle = videoTitle.toLowerCase();
        String[] roundBracketBody = videoTitle.split("\\(");
        String[] squareBracketBody;
        String tempVideoTitle = "";
        Boolean first = true;

        for (String body: roundBracketBody) {
            if(first) { tempVideoTitle = body; }
            if(!first && (body.startsWith("\\s*feat") || body.startsWith("\\s*ft") || body.contains("mix") || body.contains("bootleg") || body.contains("\\s*prod"))){
                tempVideoTitle += body;
            }
            first = false;
        }

        squareBracketBody = tempVideoTitle.split("\\[");

        for (String body: squareBracketBody) {
            if(first) { tempVideoTitle = body; first = false; }
            if(!first && (body.startsWith("\\s*feat") || body.contains("mix") || body.startsWith("\\s*ft"))){
                tempVideoTitle += body;
            }
        }

        videoTitle = tempVideoTitle;


        if(videoTitle.contains("(") && !videoTitle.contains("(feat") && !videoTitle.contains("(ft")) {
            videoTitle = videoTitle.substring(0,videoTitle.indexOf("("));
        } else if (videoTitle.contains("[")){
            videoTitle = videoTitle.substring(0,videoTitle.indexOf("["));
        }

        videoTitle = Normalizer.normalize(videoTitle, Normalizer.Form.NFD);
        videoTitle = videoTitle.replaceAll("[^A-Za-z0-9_öäü\\s]", "");
        videoTitle = videoTitle.replaceAll("\\bfeat\\b", " ");
        videoTitle = videoTitle.replaceAll("\\bft\\b"," ");
        videoTitle = videoTitle.replaceAll("\\bfeaturing\\b"," ");
        while(videoTitle.contains("  ")) {
            videoTitle = videoTitle.trim().replaceAll("\\s{2,}"," ");
        }
        return videoTitle;
    }

    /**
     * Call function to create API service object. Define and
     * execute API request. Print API response.
     *
     * @throws GeneralSecurityException, IOException, GoogleJsonResponseException
     */

    public static void main(String[]args) throws IOException, YoutubeDLException, GeneralSecurityException, SpotifyWebApiException, ParseException, TagException {
        Boolean incorrectInput2 = true;
        Boolean incorrectInput3 = true;
        Boolean incorrectInput4 = true;
        Boolean nothingHappened = false;

        DownloadSong downloadSong = new DownloadSong();

        Scanner reader = new Scanner(System.in);
        String input;

        YouTube youtubeService = getService();
        YouTube youTubeServiceAuthorized = null;

        JSONParser parser = new JSONParser();
        JSONObject settings = (JSONObject) parser.parse(new FileReader("settings.json"));

        String youtubePlaylistID = (String) settings.get("youtubePlaylistID");
        String directory = (String) settings.get("directoryToSaveTo");
        String spotifyPlaylist = (String) settings.get("spotifyPlaylistID");
        DEVELOPER_KEY = (String) settings.get("googleAPI");
        String spotifyClientID = (String) settings.get("spotifyClientID");
        String spotifySecret = (String) settings.get("spotifyClientSecret");
        final Boolean checkEveryResultsOnSpotify = (Boolean) settings.get("checkEveryResultsOnSpotify");
        final Boolean deleteVideoAfterAdding = (Boolean) settings.get("deleteVideoAfterAdding");

        SearchSpotify searchSpotify = new SearchSpotify(spotifyClientID,spotifySecret);
        AuthorizeSpotify authorizeSpotify = new AuthorizeSpotify(spotifyClientID, spotifySecret);
        searchSpotify.spotifyApi.setAccessToken(authorizeSpotify.authorizationCode());
        AddToSpotify addToSpotify = new AddToSpotify(searchSpotify.spotifyApi.getAccessToken(), spotifyPlaylist);

        // Define and execute the API request
        YouTube.PlaylistItems.List pLrequest = youtubeService.playlistItems()
                .list("snippet,contentDetails");
        PlaylistItemListResponse pLresponse = pLrequest.setKey(DEVELOPER_KEY)
                .setMaxResults(50L)
                .setPlaylistId(youtubePlaylistID)
                .execute();
        if(deleteVideoAfterAdding) youTubeServiceAuthorized = getServiceAuthorized();

        for (PlaylistItem video: pLresponse.getItems()) {
            String videoTitle = convertVideoTitle(video.getSnippet().getTitle());
            Track[] tracks = searchSpotify.searchItem(videoTitle);

            if(tracks.length == 0) {
                System.out.println(video.getSnippet().getTitle() + " was not found on Spotify.\nShould the song be downloaded? (y/n)");
                while (incorrectInput4) {
                    input = reader.nextLine();
                    if (input.equals("y")) {
                        downloadSong.downloadSong("https://www.youtube.com/watch?v="+video.getContentDetails().getVideoId(), directory, video);
                        incorrectInput4 = false;
                    } else if (input.equals("n")) {
                        System.out.println("Nothing happend.");
                        incorrectInput4 = false;
                        nothingHappened = true;
                    } else {
                        System.out.println("Incorrect Input.");
                    }
                }
                incorrectInput4 = true;
            } else if(tracks.length == 1) {
                String artists = "";
                for (ArtistSimplified artist:tracks[0].getArtists()) {
                    artists += artist.getName();
                }
                if(checkEveryResultsOnSpotify) {
                    System.out.println("Is this the correct song: " + artists + " - " + tracks[0].getName() +"? (y/n)");
                    while (incorrectInput3) {
                        input = reader.nextLine();
                        if (input.equals("y")) {
                            String[] tracksToAdd = new String[]{tracks[0].getUri()};
                            addToSpotify.addTracksToPlaylist(tracksToAdd);
                            System.out.println("The song with the name: " + video.getSnippet().getTitle() + " was found with the name: " + artists + " - " + tracks[0].getName()  + " added to the playlist.\n");
                            incorrectInput3 = false;
                        } else if (input.equals("n")) {
                            System.out.println("Should the song be downloaded? (y/n)");
                            while (incorrectInput4) {
                                input = reader.nextLine();
                                if (input.equals("y")) {
                                    downloadSong.downloadSong("https://www.youtube.com/watch?v="+video.getContentDetails().getVideoId(), directory, video);
                                    incorrectInput4 = false;
                                } else if (input.equals("n")) {
                                    System.out.println("Nothing happend.");
                                    nothingHappened = true;
                                    incorrectInput4 = false;
                                } else {
                                    System.out.println("Incorrect Input.");
                                }
                            }
                            incorrectInput4 = true;
                            incorrectInput3 = false;
                        } else {
                            System.out.println("Incorrect Input.");
                        }
                    }
                    incorrectInput3 = true;
                } else {
                    String[] tracksToAdd = new String[]{tracks[0].getUri()};
                    addToSpotify.addTracksToPlaylist(tracksToAdd);
                    System.out.println("The song with the name: " + video.getSnippet().getTitle() + " was found with the name: " + artists + " - " + tracks[0].getName()  + "added to the playlist.\n");
                }
            } else if (tracks.length > 1){
                String artists = "";
                for (ArtistSimplified artist:tracks[0].getArtists()) {
                    artists += artist.getName();
                }
                int i = 1, chosenTrackInt;
                String chosenTrack;
                System.out.println("The song: " + video.getSnippet().getTitle() + " was found but with multiple results.\nType the number of the wanted song\nor type -1 to choose none and download the song or -2 to choose none and do nothing.\n");
                for (Track track:tracks) {
                    System.out.println(i + ": " + artists + " - " + track.getName() + "\n");
                    i++;
                    if(i>20) break;
                }
                System.out.println("Choose: ");

                while (incorrectInput2) {
                    chosenTrack = reader.nextLine();
                    chosenTrackInt = Integer.valueOf(chosenTrack);
                    if (chosenTrackInt == -1) {
                        downloadSong.downloadSong("https://www.youtube.com/watch?v=" + video.getContentDetails().getVideoId(), directory, video);
                        incorrectInput2 = false;
                    } else if (chosenTrackInt == -2) {
                        System.out.println("None was chosen, Nothing will happen.");
                        nothingHappened = true;
                        incorrectInput2 = false;
                    } else if (chosenTrackInt > 0 && chosenTrackInt < 21) {
                        String[] tracksToAdd = new String[]{tracks[chosenTrackInt-1].getUri()};
                        addToSpotify.addTracksToPlaylist(tracksToAdd);

                        System.out.println("The song with the name: " + video.getSnippet().getTitle() + " was found with the name: " + tracks[chosenTrackInt].getArtists()[0].getName() + " - " + tracks[chosenTrackInt].getName() + " added to the playlist.\n");
                        incorrectInput2 = false;
                    } else {
                        System.out.println("Incorrect input!");
                    }
                }
                incorrectInput2 = true;
            }

            if(deleteVideoAfterAdding && !nothingHappened) {
                YouTube.PlaylistItems.Delete deleteRequest = youTubeServiceAuthorized.playlistItems()
                        .delete(video.getId());
                deleteRequest.execute();
            }
            nothingHappened = false;
        }
    }
}

