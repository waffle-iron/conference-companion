package fr.xebia.devoxx.be.bus;

public class ConferenceSelectedEvent {

    public final int conferenceId;

    public ConferenceSelectedEvent(int conferenceId) {
        this.conferenceId = conferenceId;
    }
}