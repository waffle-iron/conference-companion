package fr.xebia.conference.companion.model;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

@Table("Speaker_Talk")
public class SpeakerTalk extends Model {

    @Column("speakerId") @Key private String speakerId;
    @Column("talkId") @Key private String talkId;
    @Column("conferenceId") @Key private int conferenceId;

    public SpeakerTalk() {
    }

    public SpeakerTalk(String speakerId, String talkId, int conferenceId) {
        this.speakerId = speakerId;
        this.talkId = talkId;
        this.conferenceId = conferenceId;
    }
}
