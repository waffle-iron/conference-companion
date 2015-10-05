package fr.xebia.xebicon.api;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItemListResponse;

import java.io.IOException;

public class YoutubeApi implements VideoApi {

    @Override
    public PlaylistItemListResponse getVideos() throws IOException {
        YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {
            }
        }).setApplicationName("youtube-cmdline-search-sample").build();

        YouTube.PlaylistItems.List playlistItemsList = youtube.playlistItems().list("snippet");
        playlistItemsList.setKey("AIzaSyASamoWzAeRgsfkL2LsEdBxoBdGIpZAlng");
        playlistItemsList.setPlaylistId("UUDSbqKhAoWZARlWAPbf88rg");
        playlistItemsList.setMaxResults(50L);
        return playlistItemsList.execute();
    }
}
