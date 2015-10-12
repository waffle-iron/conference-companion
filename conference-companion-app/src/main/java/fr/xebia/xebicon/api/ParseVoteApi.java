package fr.xebia.xebicon.api;

import android.content.Context;
import android.provider.Settings.Secure;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import fr.xebia.xebicon.model.Vote;
import rx.Observable;
import rx.schedulers.Schedulers;

import static android.provider.Settings.Secure.ANDROID_ID;

public class ParseVoteApi implements VoteApi {

    Context context;

    public ParseVoteApi(Context context) {
        this.context = context;
    }

    @Override
    public void sendRating(Vote vote) {
        String userId = Secure.getString(context.getContentResolver(), ANDROID_ID);

        Observable.create(subscriber -> {
            ParseObject parseVote = null;

            try {
                parseVote = new ParseQuery("Vote")
                        .whereMatches("talk", vote.getTalkId())
                        .whereMatches("user", userId)
                        .getFirst();

            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (parseVote == null) {
                parseVote = new ParseObject("Vote");
            }

            parseVote.put("user", userId);
            parseVote.put("talk", vote.getTalkId());
            parseVote.put("rate", vote.getRate());
            parseVote.put("revelent", vote.getRevelent());
            parseVote.put("content", vote.getContent());
            parseVote.put("speakers", vote.getSpeakers());
            parseVote.put("comment", vote.getComment());

            parseVote.saveEventually();
        })
                .subscribeOn(Schedulers.io())
                .subscribe(
                        aVoid -> {},
                        throwable -> {});


    }
}
