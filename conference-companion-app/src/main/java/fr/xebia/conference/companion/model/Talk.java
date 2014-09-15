package fr.xebia.conference.companion.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.core.utils.TimeUtils;
import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;


@Table("Talks")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Talk extends Model implements Parcelable {

    public static final String REGISTRATION = "registration";
    public static final String BREAK = "break";
    public static final String LUNCH = "lunch";
    public static final String KEYNOTE = "keynote";
    public static final String PRESENTATION = "talk";


    @JsonProperty @Column("_id") @Key private String id;
    @JsonProperty @Column("conferenceId") private int conferenceId;
    @JsonProperty @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT") @Column("fromTime") private Date fromTime;
    @JsonProperty @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT") @Column("toTime") private Date toTime;
    @JsonProperty private LinkedHashSet<Speaker> speakers;
    @JsonProperty @Column("room") private String room;
    @JsonProperty @Column("type") private String type;
    @JsonProperty @Column("language") private String language;
    @JsonProperty @Column("experience") private String experience;
    @JsonProperty @Column("track") private String track;
    @JsonProperty @Column("kind") private String kind;
    @JsonProperty @Column("title") private String title;
    @JsonProperty @Column("summary") private String summary;
    @JsonProperty @Column("favorite") private boolean favorite;
    @JsonProperty("presentation") private Talk details;

    @Column("talkDetailsId") private String talkDetailsId;
    @Column("color") private int color;
    @Column("memo") private String memo = "";
    @Column("prettySpeakers") private String prettySpeakers;

    public Talk() {

    }

    private String mPeriod;
    private String mDay;

    public String getId() {
        return id;
    }

    public int getConferenceId() {
        return conferenceId;
    }

    public Date getFromTime() {
        return fromTime;
    }

    public Date getToTime() {
        return toTime;
    }

    public Collection<Speaker> getSpeakers() {
        return speakers;
    }

    public String getRoom() {
        return room;
    }

    public String getType() {
        return type;
    }

    public String getLanguage() {
        return language;
    }

    public String getExperience() {
        return experience;
    }

    public boolean isBreak() {
        return REGISTRATION.equalsIgnoreCase(kind) || BREAK.equalsIgnoreCase(kind) || LUNCH.equalsIgnoreCase(kind);
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getTitle() {
        return title;
    }

    public String getUncotedTitle() {
        if (title != null) {
            return title.replaceAll("\"", "");
        } else {
            return "";
        }
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getPeriod() {
        if (mPeriod == null) {
            mPeriod = TimeUtils.formatTimeRange(fromTime, toTime);
        }
        return mPeriod;
    }

    public String getDay() {
        if (mDay == null) {
            mDay = TimeUtils.formatDay(fromTime);
        }
        return mDay;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getBody(Context context) {
        StringBuilder buffer = new StringBuilder(getUncotedTitle());
        buffer.append("( ");
        buffer.append(getPeriod());
        buffer.append(" - ");
        buffer.append(room);
        buffer.append(")");
        buffer.append("\n");
        buffer.append("\n");
        buffer.append(context.getResources().getString(R.string.memo).toUpperCase());
        buffer.append("\n");
        buffer.append("\n");
        buffer.append(memo);
        buffer.append("\n");
        buffer.append("\n");
        buffer.append(context.getResources().getString(R.string.summary).toUpperCase());
        buffer.append("\n");
        buffer.append("\n");
        buffer.append(summary);
        if (speakers != null) {
            buffer.append("\n");
            buffer.append("\n");
            buffer.append(context.getResources().getString(R.string.authors).toUpperCase());
            buffer.append("\n");
            for (Speaker speaker : speakers) {
                buffer.append("\n");
                buffer.append(speaker.getFirstName());
                buffer.append(" ");
                buffer.append(speaker.getLastName());
                buffer.append(" ");
                buffer.append(speaker.getBlog());
            }
        }
        return buffer.toString();
    }

    public String getTalkDetailsId() {
        return talkDetailsId;
    }

    public void setTalkDetailsId(String talkDetailsId) {
        this.talkDetailsId = talkDetailsId;
    }

    public void setSpeakers(LinkedHashSet<Speaker> speakers) {
        this.speakers = speakers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || ((Object) this).getClass() != o.getClass()) return false;

        Talk talk = (Talk) o;

        if (conferenceId != talk.conferenceId) return false;
        if (id != null ? !id.equals(talk.id) : talk.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + conferenceId;
        return result;
    }


    public Talk getDetails() {
        return details;
    }

    public void setPrettySpeakers(Collection<Speaker> speakers, HashMap<String, Speaker> speakersMap) {
        StringBuilder prettySpeakers = new StringBuilder();
        int position = 0;
        for (Speaker speaker : speakers) {
            Speaker speakerDetails = speakersMap.get(speaker.getId());
            appendSpeaker(prettySpeakers, speakerDetails, position != 0);
            position++;
        }
        this.prettySpeakers = prettySpeakers.toString();
    }

    private void appendSpeaker(StringBuilder prettySpeakers, Speaker speaker, boolean separator) {
        if (speaker != null && !TextUtils.isEmpty(speaker.getLastName())) {
            if (separator) {
                prettySpeakers.append(", ");
            }
            if (!TextUtils.isEmpty(speaker.getFirstName())) {
                prettySpeakers.append(speaker.getFirstName().substring(0, 1).toUpperCase());
                prettySpeakers.append(".");
                prettySpeakers.append(" ");
            }
            prettySpeakers.append(speaker.getLastName().substring(0, 1).toUpperCase());
            prettySpeakers.append(speaker.getLastName().substring(1, speaker.getLastName().length()));
        }
    }

    public String getPrettySpeakers() {
        return prettySpeakers;
    }

    protected Talk(Parcel in) {
        id = in.readString();
        conferenceId = in.readInt();
        long tmpFromTime = in.readLong();
        fromTime = tmpFromTime != -1 ? new Date(tmpFromTime) : null;
        long tmpToTime = in.readLong();
        toTime = tmpToTime != -1 ? new Date(tmpToTime) : null;
        speakers = (LinkedHashSet) in.readValue(LinkedHashSet.class.getClassLoader());
        room = in.readString();
        type = in.readString();
        language = in.readString();
        experience = in.readString();
        track = in.readString();
        kind = in.readString();
        title = in.readString();
        summary = in.readString();
        favorite = in.readByte() != 0x00;
        details = (Talk) in.readValue(Talk.class.getClassLoader());
        talkDetailsId = in.readString();
        color = in.readInt();
        memo = in.readString();
        prettySpeakers = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(conferenceId);
        dest.writeLong(fromTime != null ? fromTime.getTime() : -1L);
        dest.writeLong(toTime != null ? toTime.getTime() : -1L);
        dest.writeValue(speakers);
        dest.writeString(room);
        dest.writeString(type);
        dest.writeString(language);
        dest.writeString(experience);
        dest.writeString(track);
        dest.writeString(kind);
        dest.writeString(title);
        dest.writeString(summary);
        dest.writeByte((byte) (favorite ? 0x01 : 0x00));
        dest.writeValue(details);
        dest.writeString(talkDetailsId);
        dest.writeInt(color);
        dest.writeString(memo);
        dest.writeString(prettySpeakers);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Talk> CREATOR = new Parcelable.Creator<Talk>() {
        @Override
        public Talk createFromParcel(Parcel in) {
            return new Talk(in);
        }

        @Override
        public Talk[] newArray(int size) {
            return new Talk[size];
        }
    };

    public boolean isKeynote() {
        return kind.equalsIgnoreCase(KEYNOTE);
    }
}
