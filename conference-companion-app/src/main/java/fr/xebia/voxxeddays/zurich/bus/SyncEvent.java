package fr.xebia.voxxeddays.zurich.bus;

public class SyncEvent {

    private SyncEvent() {

    }

    private static final SyncEvent INSTANCE = new SyncEvent();

    public static SyncEvent getInstance() {
        return INSTANCE;
    }
}
