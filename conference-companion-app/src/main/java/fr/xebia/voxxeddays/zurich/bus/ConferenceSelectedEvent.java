package fr.xebia.voxxeddays.zurich.bus;

public class ConferenceSelectedEvent {

    public final int conferenceId;

    public ConferenceSelectedEvent(int conferenceId) {
        this.conferenceId = conferenceId;
    }
}
