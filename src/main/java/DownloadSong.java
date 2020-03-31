import com.google.api.services.youtube.model.PlaylistItem;
import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLException;
import com.sapher.youtubedl.YoutubeDLRequest;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagConstant;
import org.farng.mp3.TagException;
import org.farng.mp3.TagOptionSingleton;
import org.farng.mp3.id3.ID3v1;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class DownloadSong {
    public void downloadSong(String link, String directory, PlaylistItem video) throws IOException, TagException, YoutubeDLException {
        Boolean incorrectInput = true;
        String input;
        String artist="";
        String title;
        String fileTitle;

        DownloadThread downloadThread;

        Scanner reader = new Scanner(System.in);



        fileTitle = video.getSnippet().getTitle().replaceAll("[\\/\\\\\\:\\*\\?\\<\\>\\|\\\"]", "_");

        if(video.getSnippet().getTitle().contains(" -")) {
            artist = video.getSnippet().getTitle().substring(0, video.getSnippet().getTitle().indexOf(" -"));
            title = video.getSnippet().getTitle().substring(video.getSnippet().getTitle().indexOf(" -")+3);

            System.out.println("Set artist to: \"" + artist + "\" and title to: \"" + title + "\"? (y/n)");
        } else {
            title = video.getSnippet().getTitle();

            System.out.println("Set title to: \"" + title + "\"? (y/n)");
        }



        while (incorrectInput) {
            input = reader.nextLine();
            if (input.equals("y")) {
                downloadThread = new DownloadThread(link,directory,fileTitle,artist,title);
                new Thread(downloadThread).start();
                incorrectInput = false;
            } else if (input.equals("n")) {
                System.out.println("Enter artist name: ");
                artist = reader.nextLine();
                System.out.println("Enter title: ");
                title = reader.nextLine();
                downloadThread = new DownloadThread(link,directory,fileTitle,artist,title);
                new Thread(downloadThread).start();
                incorrectInput = false;
            } else {
                System.out.println("Incorrect input.");
            }
        }
        System.out.println("The song with the name: " + video.getSnippet().getTitle() + " will be downloaded.\n");
    }
}
