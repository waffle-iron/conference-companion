package fr.xebia.conference.companion.model;

import android.content.Context;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.xebia.conference.companion.R;
import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


@Table("Talks")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Talk extends Model {

    public static final String REGISTRATION = "registration";
    public static final String BREAK = "break";
    public static final String LUNCH = "lunch";
    public static final String PRESENTATION = "talk";

    private static DateFormat sTimeFormatter = new SimpleDateFormat("HH:mm");
    private static DateFormat sDayFormatter = new SimpleDateFormat("EEEE");

    @JsonProperty @Column("_id") @Key private String id;
    @JsonProperty @Column("conferenceId") private int conferenceId;
    @JsonProperty @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Column("fromTime") private Date fromTime;
    @JsonProperty @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Column("toTime") private Date toTime;
    @JsonProperty private List<Speaker> speakers;
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

    public List<Speaker> getSpeakers() {
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
            mPeriod = String.format("%s - %s", getDay(), sTimeFormatter.format(fromTime));
        }
        return mPeriod;
    }

    public String getDay() {
        if (mDay == null) {
            mDay = sDayFormatter.format(fromTime);
            mDay = mDay.substring(0, 1).toUpperCase() + mDay.substring(1, mDay.length()).toLowerCase();
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

    public void setSpeakers(List<Speaker> speakers) {
        this.speakers = speakers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

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
}
