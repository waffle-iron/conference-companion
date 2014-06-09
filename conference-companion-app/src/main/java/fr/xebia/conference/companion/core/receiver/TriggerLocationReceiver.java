package fr.xebia.conference.companion.core.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import fr.xebia.conference.companion.service.BluetoothLocationIntentService;

public class TriggerLocationReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent bleLocationIntent = new Intent(context, BluetoothLocationIntentService.class);
        WakefulBroadcastReceiver.startWakefulService(context, bleLocationIntent);
    }
}
