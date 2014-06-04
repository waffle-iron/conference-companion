package fr.xebia.conference.companion.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

@Table("Votes")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Vote extends Model {

    @JsonProperty @Column("note") private int note;
    @JsonProperty @Column("_id") @Key private String talkId;
    @JsonProperty @Column("conferenceId") @Key private int conferenceId;

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
