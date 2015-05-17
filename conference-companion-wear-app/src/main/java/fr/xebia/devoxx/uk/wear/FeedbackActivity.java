package fr.xebia.devoxx.uk.wear;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.view.DelayedConfirmationView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

import fr.xebia.devoxx.uk.R;
import timber.log.Timber;

public class FeedbackActivity extends Activity {

    private TextView mTitle;
    private RatingBar mRatingBar;
    private ViewGroup mDelayedConfirmationContainer;
    private DelayedConfirmationView mDelayedConfirmationView;
    private GoogleApiClient mGoogleApiClient;
    private float mCurrentRating;
    private boolean mTimerSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        setContentView(R.layout.feedback_activity);
        setupViews();
        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                mCurrentRating = rating;
                mDelayedConfirmationView.setTotalTimeMs(7 * 1000);
                mDelayedConfirmationContainer.setVisibility(View.VISIBLE);
                mDelayedConfirmationView.start();
                mTimerSelected = false;
            }
        });
        mTitle.setText(getIntent().getStringExtra(HomeListenerService.KEY_TALK_TITLE));
        if (mRatingBar.getProgressDrawable() != null) {
            try {
                mRatingBar.getProgressDrawable().setColorFilter(getIntent().getIntExtra(HomeListenerService.KEY_TALK_COLOR, 0), PorterDuff.Mode.SRC_IN);
            } catch (Exception e) {
                // TODO Check what happen
                Timber.e(e, "Error mutating rating bar");
            }
        }
        mDelayedConfirmationView.setListener(new DelayedConfirmationView.DelayedConfirmationListener() {
            @Override
            public void onTimerFinished(View view) {
                if (mTimerSelected) {
                    mDelayedConfirmationContainer.setVisibility(View.GONE);
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mGoogleApiClient.blockingConnect(10, TimeUnit.SECONDS);
                        if (mGoogleApiClient.isConnected()) {
                            PutDataRequest dataRequest = buildDataRequest();
                            if (mGoogleApiClient.isConnected()) {
                                Wearable.DataApi.putDataItem(mGoogleApiClient, dataRequest).await();
                            }
                        }
                    }
                }).start();
                String talkId = getIntent().getStringExtra(HomeListenerService.KEY_TALK_ID);
                NotificationManagerCompat.from(FeedbackActivity.this).cancelAll();
                startActivity(new Intent(FeedbackActivity.this, FinishActivity.class));
                finish();
            }

            @Override
            public void onTimerSelected(View view) {
                mTimerSelected = true;
                mDelayedConfirmationContainer.setVisibility(View.GONE);
            }
        });
    }

    private PutDataRequest buildDataRequest() {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(HomeListenerService.PATH_RATING);
        DataMap dataMap = putDataMapRequest.getDataMap();
        dataMap.putString("talkId", getIntent().getStringExtra(HomeListenerService.KEY_TALK_ID));
        dataMap.putInt("rating", (int) mCurrentRating);
        return putDataMapRequest.asPutDataRequest();
    }

    private void setupViews() {
        mTitle = (TextView) findViewById(R.id.title);
        mRatingBar = (RatingBar) findViewById(R.id.ratingbar);
        mDelayedConfirmationContainer = (ViewGroup) findViewById(R.id.delayed_confirmation_container);
        mDelayedConfirmationView = (DelayedConfirmationView) findViewById(R.id.delayed_confirmation);
    }
}
