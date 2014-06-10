package fr.xebia.conference.companion.bus;

import fr.xebia.conference.companion.model.Conference;

public class SynchroFinishedEvent {

    public final boolean success;
    public final Conference conference;

    public SynchroFinishedEvent(boolean success, Conference conference) {
        this.success = success;
        this.conference = conference;
    }
}
