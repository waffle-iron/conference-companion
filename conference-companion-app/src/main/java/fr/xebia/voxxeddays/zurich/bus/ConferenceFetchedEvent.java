package fr.xebia.voxxeddays.zurich.bus;

public class ConferenceFetchedEvent {

    public final boolean success;

    public ConferenceFetchedEvent(boolean success) {
        this.success = success;
    }
}
