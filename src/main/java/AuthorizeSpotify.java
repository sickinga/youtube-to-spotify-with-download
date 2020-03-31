import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AuthorizeSpotify {
    private static final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost:8888/callback");
    private static String code;
    private static String refreshToken;
    private BlockingQueue blockingQueue = new ArrayBlockingQueue(1024);
    Object lock = new Object();
    BasicHttpServer basicHttpServer = new BasicHttpServer(blockingQueue, lock);

    private static SpotifyApi spotifyApi;
    private static AuthorizationCodeUriRequest authorizationCodeUriRequest;
    private static AuthorizationCodeRequest authorizationCodeRequest;

    public AuthorizeSpotify (String clientId, String clientSecret) {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(redirectUri)
                .build();
        authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                .scope("playlist-modify-public playlist-modify-private")
                .show_dialog(true)
                .build();
    }

    public static void authorizationCodeUri() throws IOException {
        final URI uri = authorizationCodeUriRequest.execute();

        Desktop desktop = java.awt.Desktop.getDesktop();
        desktop.browse(uri);
    }

    public String authorizationCode() {
        try {
            if(Files.exists(Paths.get("spotifyToken.log")))
                refreshToken = Files.readAllLines(Paths.get("spotifyToken.log")).get(0);
            if(refreshToken == null) {
                new Thread(basicHttpServer).start();
                authorizationCodeUri();

                synchronized (lock) {
                    lock.wait();
                }

                code = blockingQueue.take().toString();
                code = code.replace("/callback?code=","");

                authorizationCodeRequest = spotifyApi.authorizationCode(code).build();

                final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

                // Set access and refresh token for further "spotifyApi" object usage
                spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
                spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());

                new File("spotifyToken.log");
                Files.write(Paths.get("spotifyToken.log"),spotifyApi.getRefreshToken().getBytes());
            } else {
                spotifyApi.setRefreshToken(refreshToken);
                AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh()
                        .build();

                AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();

                spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
                spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            }
            return spotifyApi.getAccessToken();
        } catch (IOException | SpotifyWebApiException e) {
            System.out.println("Error: " + e.getMessage());
            return "error";
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "error";
        }
    }
}

