package fr.xebia.devoxx.pl.bus;

public class ConferenceFetchedEvent {

    public final boolean success;

    public ConferenceFetchedEvent(boolean success) {
        this.success = success;
    }
}
