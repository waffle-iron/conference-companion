package fr.xebia.conference.companion.bus;

public class SynchroFinishedEvent {

    public final boolean success;
    public final int conferenceId;

    public SynchroFinishedEvent(boolean success, int conferenceId) {
        this.success = success;
        this.conferenceId = conferenceId;
    }
}
