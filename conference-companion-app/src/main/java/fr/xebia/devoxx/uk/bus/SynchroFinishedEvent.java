package fr.xebia.devoxx.uk.bus;

import fr.xebia.devoxx.uk.model.Conference;

public class SynchroFinishedEvent {

    public final boolean success;
    public final Conference conference;

    public SynchroFinishedEvent(boolean success, Conference conference) {
        this.success = success;
        this.conference = conference;
    }
}
