package fr.xebia.devoxx.pl.bus;

import fr.xebia.devoxx.pl.model.Conference;

public class SynchroFinishedEvent {

    public final boolean success;
    public final Conference conference;

    public SynchroFinishedEvent(boolean success, Conference conference) {
        this.success = success;
        this.conference = conference;
    }
}
