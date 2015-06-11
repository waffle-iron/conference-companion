package fr.xebia.devoxx.pl.bus;

public class SyncEvent {

    private SyncEvent() {

    }

    private static final SyncEvent INSTANCE = new SyncEvent();

    public static SyncEvent getInstance() {
        return INSTANCE;
    }
}
