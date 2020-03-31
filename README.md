# youtube-to-spotify-with-download

A cmd tool to search for songs added to a youtube playlist on spotify and if found add it to your own spotify playlist.
If the song isn't found on spotify there is an option to download it to a given directory.

**You need [Youtube-dl](https://github.com/ytdl-org/youtube-dl) downloaded and added to your enviromental variables.**

## Settings

There are five options you can change in the settings.json.

You need to change three of them to run it:
1. The directory to safe the downloaded files to. 
2. The youtubePlaylistID that can be found by copying the playlists link from youtube. The id is only the part **after** ?list=
3. The spotifyPlaylistID can be found by rightclicking on a playlist -> Share -> Copy Spotify URI than pasting it somewhere and deleting spotify:playlist

The other two options you can change are:
- If it should ask you for every song found on spotify if it is the right one (true/false, default: true)
- If it should delete the song from the youtube playlist after it is processed (true/false, default: true)

Here is an example of how the file can look like:
```json
{
  "directoryToSaveTo": "C:\\Music",
  "youtubePlaylistID": "PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj",
  "spotifyPlaylistID": "37i9dQZF1DXcBWIGoYBM5M",
  "checkEveryResultsOnSpotify": true,
  "deleteVideoAfterAdding": true
}
```
