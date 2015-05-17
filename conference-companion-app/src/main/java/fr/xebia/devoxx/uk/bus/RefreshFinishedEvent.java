package fr.xebia.devoxx.uk.bus;

public class RefreshFinishedEvent {

    public final boolean success;

    public RefreshFinishedEvent(boolean success) {
        this.success = success;
    }
}
