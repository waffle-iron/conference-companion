package fr.xebia.conference.companion.model;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

import java.util.Date;

@Table("Votes")
public class TalkVote extends Model {

    @Column("note") private int note;
    @Column("_id") @Key private String talkId;
    @Column("track") private String track;
    @Column("title") private String title;
    @Column("fromTime") private Date fromTime;
    @Column("toTime") private Date toTime;

    public int getNote() {
        return note;
    }

    public String getTalkId() {
        return talkId;
    }

    public String getTrack() {
        return track;
    }

    public String getTitle() {
        return title;
    }

    public Date getFromTime() {
        return fromTime;
    }

    public Date getToTime() {
        return toTime;
    }
}
