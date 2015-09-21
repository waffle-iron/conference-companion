package fr.xebia.devoxx.be.wear;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import fr.xebia.devoxx.be.R;

public class HomeActivity extends Activity {

    private TextView mSession;
    private TextView mSpeaker;
    private View mContent;
    private ImageView mRateIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        setupViews();

        Bundle bundle = this.getIntent().getExtras();
        if (null != bundle) {
            final String talkTitle = bundle.getString(HomeListenerService.KEY_TALK_TITLE);
            final String talkId = getIntent().getStringExtra(HomeListenerService.KEY_TALK_ID);
            final int notificationId = getIntent().getIntExtra(HomeListenerService.KEY_NOTIFICATION_ID, 0);
            String talkRoom = bundle.getString(HomeListenerService.KEY_TALK_ROOM);
            String speakers = bundle.getString(HomeListenerService.KEY_TALK_SPEAKERS);

            mSession.setText(talkTitle);
            final int talkColor = bundle.getInt(HomeListenerService.KEY_TALK_COLOR);
            mRateIcon.setColorFilter(talkColor, PorterDuff.Mode.SRC_IN);
            mSpeaker.setText(talkRoom + " | " + speakers);
            mContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NotificationManagerCompat.from(view.getContext()).cancel(talkId, 200);
                    view.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final Intent feedbackIntent = new Intent(HomeActivity.this, FeedbackActivity.class);
                            feedbackIntent.putExtra(HomeListenerService.KEY_TALK_ID, talkId);
                            feedbackIntent.putExtra(HomeListenerService.KEY_NOTIFICATION_ID, notificationId);
                            feedbackIntent.putExtra(HomeListenerService.KEY_TALK_COLOR, talkColor);
                            feedbackIntent.putExtra(HomeListenerService.KEY_TALK_TITLE, talkTitle);
                            feedbackIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(feedbackIntent);

                        }
                    }, 150);
                }
            });
        }
    }

    private void setupViews() {
        mSession = (TextView) findViewById(R.id.session_name);
        mSpeaker = (TextView) findViewById(R.id.speaker_room);
        mContent = findViewById(R.id.content);
        mRateIcon = (ImageView) findViewById(R.id.rate_icon);
    }
}
