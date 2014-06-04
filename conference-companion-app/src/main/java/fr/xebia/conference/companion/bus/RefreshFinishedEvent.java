package fr.xebia.conference.companion.bus;

public class RefreshFinishedEvent {

    public final boolean success;

    public RefreshFinishedEvent(boolean success) {
        this.success = success;
    }
}
