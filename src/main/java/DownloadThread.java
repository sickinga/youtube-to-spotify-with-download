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

public class DownloadThread implements Runnable{
    private String link, directory, fileTitle, artist, title;

    public DownloadThread(String Link, String Directory, String FileTitle, String Artist, String Title) {
        this.link = Link; this.directory = Directory; this.fileTitle = FileTitle; this.artist = Artist; this.title = Title;
    }

    @Override
    public void run() {
        // Build request
        YoutubeDLRequest request = new YoutubeDLRequest(link, directory);
        request.setOption("extract-audio");        // --ignore-errors
        request.setOption("audio-format", "mp3");    // --output "%(id)s"
        request.setOption("retries", 10);        // --retries 10
        request.setOption("output", "%(title)s.%(ext)s");

        // Make request
        try {
            YoutubeDL.execute(request);

            File song = new File(directory + "\\" + fileTitle + ".mp3");

            MP3File mp3file = new MP3File(song);
            TagOptionSingleton.getInstance().setDefaultSaveMode(TagConstant.MP3_FILE_SAVE_OVERWRITE);

            ID3v1 id3v1Tag = new ID3v1();
            id3v1Tag.setArtist(artist);
            id3v1Tag.setTitle(title);
            mp3file.setID3v1Tag(id3v1Tag);
                mp3file.save(directory + "\\" + fileTitle + ".mp3");
            new File(directory + "\\" + fileTitle + ".original.mp3").delete();

        } catch (IOException | TagException e ) {
            e.printStackTrace();
        } catch (YoutubeDLException e) {
            e.printStackTrace();
        }
    }
}
