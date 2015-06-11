package fr.xebia.devoxx.pl.bus;

public class RefreshFinishedEvent {

    public final boolean success;

    public RefreshFinishedEvent(boolean success) {
        this.success = success;
    }
}
