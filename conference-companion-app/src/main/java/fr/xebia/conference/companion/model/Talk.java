package fr.xebia.conference.companion.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


@Table("Talks")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Talk extends Model {

    // TODO Use default locale when app will support i18n
    private static DateFormat sTimeFormatter = new SimpleDateFormat("HH:mm", Locale.FRANCE);
    private static DateFormat sDayFormatter = new SimpleDateFormat("EEEE", Locale.FRANCE);

    @JsonProperty @Column("_id") @Key private String id;
    @JsonProperty @Column("conferenceId") private int conferenceId;
    @JsonProperty @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Paris") @Column("fromTime") private Date fromTime;
    @JsonProperty @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Paris") @Column("toTime") private Date toTime;
    @JsonProperty private List<Speaker> speakers;
    @JsonProperty @Column("room") private String room;
    @JsonProperty @Column("type") private String type;
    @JsonProperty @Column("language") private String language;
    @JsonProperty @Column("experience") private String experience;
    @JsonProperty @Column("track") private String track;
    @JsonProperty @Column("title") private String title;
    @JsonProperty @Column("summary") private String summary;
    @JsonProperty @Column("devoxxian_note") private int note;
    @JsonProperty @Column("favorite") private boolean favorite;

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

    public String getTrack() {
        return track;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getPeriod() {
        if (mPeriod == null) {
            mPeriod = String.format("%s - %s", getDay(), sTimeFormatter.format(toTime));
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
}
