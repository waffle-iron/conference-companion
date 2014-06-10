package fr.xebia.conference.companion.ui.vote;

import android.animation.*;
import android.app.ActionBar;
import android.app.Fragment;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.bus.TagRegisteredEvent;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.core.misc.RestoreActionBarFragment;
import fr.xebia.conference.companion.core.utils.NfcFormatter;
import fr.xebia.conference.companion.model.Conference;
import icepick.Icepick;
import icepick.Icicle;
import se.emilsjolander.sprinkles.OneQuery;
import se.emilsjolander.sprinkles.Query;

import static fr.xebia.conference.companion.core.KouignAmanApplication.BUS;


public class ScanNfcFragment extends Fragment implements RestoreActionBarFragment {

    public static final String TAG = "ScanNfcFragment";

    @InjectView(R.id.scan_nfc) ImageView mScanNfc;
    @InjectView(R.id.scan_nfc_hint) TextView mScanNfcHint;


    private final PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofKeyframe(View.SCALE_X,
            Keyframe.ofFloat(0f, 1f),
            Keyframe.ofFloat(.1f, 1.05f),
            Keyframe.ofFloat(.2f, 1.1f),
            Keyframe.ofFloat(.3f, 1.15f),
            Keyframe.ofFloat(.4f, 1.2f),
            Keyframe.ofFloat(.5f, 1.25f),
            Keyframe.ofFloat(.6f, 1.2f),
            Keyframe.ofFloat(.7f, 1.15f),
            Keyframe.ofFloat(.8f, 1.1f),
            Keyframe.ofFloat(.9f, 1.05f),
            Keyframe.ofFloat(1f, 1f)
    );
    private final PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofKeyframe(View.SCALE_Y,
            Keyframe.ofFloat(0f, 1f),
            Keyframe.ofFloat(.1f, 1.05f),
            Keyframe.ofFloat(.2f, 1.1f),
            Keyframe.ofFloat(.3f, 1.15f),
            Keyframe.ofFloat(.4f, 1.2f),
            Keyframe.ofFloat(.5f, 1.25f),
            Keyframe.ofFloat(.6f, 1.2f),
            Keyframe.ofFloat(.7f, 1.15f),
            Keyframe.ofFloat(.8f, 1.1f),
            Keyframe.ofFloat(.9f, 1.05f),
            Keyframe.ofFloat(1f, 1f)
    );

    private ObjectAnimator mBounceObjectAnimator;

    private Handler mHandler = new Handler();
    private Runnable mRestartAnimRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBounceObjectAnimator != null) {
                mBounceObjectAnimator.start();
            }
        }
    };

    @Icicle String mCurrentNfcId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.scan_nfc_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        restoreActionBar();

        if (mCurrentNfcId == null) {
            mBounceObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(mScanNfc, pvhScaleX, pvhScaleY);
            mBounceObjectAnimator.setDuration(600);
            mBounceObjectAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mHandler.postDelayed(mRestartAnimRunnable, 1000);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            mBounceObjectAnimator.start();
        } else {
            mScanNfcHint.setVisibility(View.GONE);
            mScanNfc.setVisibility(View.GONE);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onDestroyView() {
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    public void initUserForTag(Tag tag) {
        mCurrentNfcId = NfcFormatter.formatNfcId(tag);

        Preferences.saveDevoxxianTag(getActivity(), mCurrentNfcId);
        Query.one(Conference.class, "SELECT * FROM Conferences WHERE _id=?", Preferences.getSelectedConference(getActivity()))
                .getAsync(getLoaderManager(), new OneQuery.ResultHandler<Conference>() {
                    @Override
                    public boolean handleResult(Conference conference) {
                        if (conference != null) {
                            conference.setNfcTag(mCurrentNfcId);
                            conference.saveAsync();
                        }
                        return false;
                    }
                });

        mBounceObjectAnimator.cancel();
        mHandler.removeCallbacks(mRestartAnimRunnable);

        AnimatorSet scanAnimation = new AnimatorSet();
        scanAnimation.play(ObjectAnimator.ofFloat(mScanNfc, "translationY", 0, -(mScanNfc.getTop() + mScanNfc.getHeight())))
                .with(ObjectAnimator.ofFloat(mScanNfcHint, "translationX", 0, -(mScanNfcHint.getLeft() + mScanNfcHint.getWidth())));
        scanAnimation.setDuration(600);
        scanAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                BUS.post(new TagRegisteredEvent());
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        scanAnimation.start();
    }

    @Override
    public void restoreActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.my_votes);
    }
}
