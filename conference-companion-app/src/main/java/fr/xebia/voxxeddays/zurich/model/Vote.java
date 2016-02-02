package fr.xebia.voxxeddays.zurich.model;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

@Table("Votes")
public class Vote extends Model {

    @Column("note") private int note;
    @Column("_id") @Key private String talkId;
    @Column("conferenceId") @Key private int conferenceId;

    public Vote(){

    }

    public Vote(int note, String talkId, int conferenceId) {
        this.note = note;
        this.talkId = talkId;
        this.conferenceId = conferenceId;
    }

    public int getNote() {
        return note;
    }

    public String getTalkId() {
        return talkId;
    }

    public int getConferenceId() {
        return conferenceId;
    }
}
