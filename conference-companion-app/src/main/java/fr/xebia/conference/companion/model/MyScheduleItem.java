package fr.xebia.conference.companion.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class MyScheduleItem implements Parcelable {
    // types:
    public static final int FREE = 0;  // a free chunk of time
    public static final int SESSION = 1; // a session
    public static final int BREAK = 2; // a break (lunch, breaks, after-hours party)

    public long endTime = -1;
    public long startTime = -1;
    public int type;
    public String title;
    public String subtitle;
    public int backgroundColor;
    public String backgroundImageUrl;
    public boolean hasGivenFeedback;
    public int favoritesCount;
    public ArrayList<Talk> availableTalks = new ArrayList<>();
    public Talk selectedTalk;

    public MyScheduleItem(long startTime, List<Talk> talks) {
        availableTalks.addAll(talks);
        this.startTime = startTime;
        for (Talk talk : availableTalks) {
            if (talk.isFavorite()) {
                // Will help detect conflict
                favoritesCount++;
                if (selectedTalk == null) {
                    // Keep the first talk as the selected one
                    selectedTalk = talk;
                    buildAttributesFromSelectedTalk();
                }
            }
        }

        if (availableTalks.size() == 1) {
            Talk talk = talks.get(0);
            if (talk.isBreak()) {
                type = BREAK;
                title = talk.getTitle();
            }
            endTime = talk.getToTime().getTime();
        } else if (favoritesCount == 0) {
            type = FREE;
            endTime = startTime;
        } else {
            type = SESSION;
        }
    }

    private void buildAttributesFromSelectedTalk() {
        endTime = selectedTalk.getToTime().getTime();
        backgroundColor = selectedTalk.getColor();
        title = selectedTalk.getTitle();
    }

    protected MyScheduleItem(Parcel in) {
        endTime = in.readLong();
        startTime = in.readLong();
        type = in.readInt();
        title = in.readString();
        subtitle = in.readString();
        backgroundColor = in.readInt();
        backgroundImageUrl = in.readString();
        hasGivenFeedback = in.readByte() != 0x00;
        favoritesCount = in.readInt();
        if (in.readByte() == 0x01) {
            availableTalks = new ArrayList<Talk>();
            in.readList(availableTalks, Talk.class.getClassLoader());
        } else {
            availableTalks = null;
        }
        selectedTalk = (Talk) in.readValue(Talk.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(endTime);
        dest.writeLong(startTime);
        dest.writeInt(type);
        dest.writeString(title);
        dest.writeString(subtitle);
        dest.writeInt(backgroundColor);
        dest.writeString(backgroundImageUrl);
        dest.writeByte((byte) (hasGivenFeedback ? 0x01 : 0x00));
        dest.writeInt(favoritesCount);
        if (availableTalks == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(availableTalks);
        }
        dest.writeValue(selectedTalk);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MyScheduleItem> CREATOR = new Parcelable.Creator<MyScheduleItem>() {
        @Override
        public MyScheduleItem createFromParcel(Parcel in) {
            return new MyScheduleItem(in);
        }

        @Override
        public MyScheduleItem[] newArray(int size) {
            return new MyScheduleItem[size];
        }
    };

    public boolean hasTalkSelected() {
        return favoritesCount > 0;
    }
}
