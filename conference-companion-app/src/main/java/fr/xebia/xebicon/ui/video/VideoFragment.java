package fr.xebia.xebicon.ui.video;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeIntents;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import fr.xebia.xebicon.R;
import fr.xebia.xebicon.core.XebiConApplication;
import fr.xebia.xebicon.core.adapter.BaseAdapter;
import fr.xebia.xebicon.core.adapter.NewBaseAdapter;
import fr.xebia.xebicon.ui.video.view.VideoItemView;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class VideoFragment extends Fragment implements Observer<PlaylistItemListResponse> {

    @InjectView(R.id.video_list) ListView videoListView;
    @InjectView(R.id.video_empty) LinearLayout emptyView;

    private String nextPageToken;
    private NewBaseAdapter<PlaylistItem, VideoItemView> adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new NewBaseAdapter<>(getActivity(), R.layout.item_view_video);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.inject(this, view);

        videoListView.setAdapter(adapter);
        videoListView.setEmptyView(emptyView);
    }

    @Override
    public void onResume() {
        super.onResume();

        Observable.create(new Observable.OnSubscribe<PlaylistItemListResponse>() {
            @Override
            public void call(Subscriber<? super PlaylistItemListResponse> subscriber) {
                try {
                    PlaylistItemListResponse videos = XebiConApplication.getVideoApi().getVideos();
                    subscriber.onNext(videos);
                } catch (IOException e) {
                    subscriber.onError(e);
                }

                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onNext(PlaylistItemListResponse playlistItemListResponse) {
        nextPageToken = playlistItemListResponse.getNextPageToken();
        adapter.setData(playlistItemListResponse.getItems());
        adapter.notifyDataSetChanged();
    }

    @OnItemClick(R.id.video_list)
    public void onVideoClick(int position){
        PlaylistItem item = adapter.getItem(position);

        if (YouTubeIntents.isYouTubeInstalled(getContext())){
            startActivity(YouTubeIntents.createPlayVideoIntent(getContext(), item.getSnippet().getResourceId().getVideoId()));
        } else {
            Toast.makeText(getContext(), R.string.youtube_not_installed, Toast.LENGTH_SHORT).show();
        }

    }
}
