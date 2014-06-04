package fr.xebia.conference.companion.bus;

public class ConferenceFetchedEvent {

    public final boolean success;

    public ConferenceFetchedEvent(boolean success) {
        this.success = success;
    }
}
