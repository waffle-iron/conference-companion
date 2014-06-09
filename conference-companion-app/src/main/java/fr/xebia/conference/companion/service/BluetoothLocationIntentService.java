package fr.xebia.conference.companion.service;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import fr.xebia.conference.companion.core.KouignAmanApplication;
import fr.xebia.conference.companion.core.misc.Preferences;
import fr.xebia.conference.companion.core.utils.Compatibility;
import fr.xebia.conference.companion.model.Conference;
import fr.xebia.conference.companion.model.location.IBeacon;
import fr.xebia.conference.companion.ui.settings.SettingsFragment;
import retrofit.RetrofitError;
import se.emilsjolander.sprinkles.Query;
import timber.log.Timber;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class BluetoothLocationIntentService extends IntentService {

    public static final String ACTION_LOCATE = "fr.xebia.conference.companion.service.ACTION_LOCATE";
    public static final String CONFERENCE_URI = "conf://conference/";
    public static final String PENDING_DATA_FILE = "pending.ser";
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mBluetoothWasDisabled;

    private Looper mBleLooper;
    private Handler mBleHandler;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 15_000;
    private AlarmManager mAlarmManager;

    public BluetoothLocationIntentService() {
        super("BluetoothLocationService");
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onCreate() {
        super.onCreate();
        if (Compatibility.isBleAvailable(this)) {
            mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
            mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            HandlerThread thread = new HandlerThread("Ble handler");
            thread.start();

            mBleLooper = thread.getLooper();
            mBleHandler = new Handler(mBleLooper);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Uri intentData = intent.getData();
        try {
            if (!Compatibility.isBleAvailable(this) || !Preferences.hasSelectedConference(this)
                    || !PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsFragment.KEY_PREF_BLE_LOCATION, true)) {
                disableAlarm(intent);
                return;
            }

            int conferenceId = intentData == null ? Preferences.getSelectedConference(this) : Integer.valueOf(intentData
                    .getLastPathSegment());
            Intent bleLocateIntent = new Intent(this, BluetoothLocationIntentService.class);
            bleLocateIntent.setData(Uri.parse(CONFERENCE_URI + conferenceId));
            Conference conference = Query.one(Conference.class, "SELECT * FROM Conferences WHERE _id=?", conferenceId).get();
            if (intentData == null) { // We don't come from receiver
                configureAlarm(bleLocateIntent, conference);
            } else if (!disableAlarmIfConferenceOver(bleLocateIntent, conference) && conference.isStarted()) {
                doBleLocation();
            }
        } finally {
            if (intentData != null) {
                WakefulBroadcastReceiver.completeWakefulIntent(intent);
            }
        }
    }


    private void configureAlarm(Intent bleLocateIntent, Conference conference) {
        boolean alarmUp = (PendingIntent.getService(this, 0, bleLocateIntent, PendingIntent.FLAG_NO_CREATE) != null);
        if (!alarmUp) {
            Timber.d("Alarm is not active");
            mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, conference.getFrom().getTime(), 120 * 1000,
                    PendingIntent.getService(this, 0, bleLocateIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }

    private boolean disableAlarmIfConferenceOver(Intent bleLocateIntent, Conference conference) {
        if (conference.getTo().getTime() < System.currentTimeMillis()) {
            disableAlarm(bleLocateIntent);
            return true;
        } else {
            return false;
        }
    }

    private void disableAlarm(Intent bleLocateIntent) {
        mAlarmManager.cancel(PendingIntent.getService(this, 0, bleLocateIntent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void doBleLocation() {
        activateBluetooth();

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        mBleHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                countDownLatch.countDown();
            }
        }, SCAN_PERIOD);

        final Set<String> bluetoothDevicesWithRssi = retrievePendingData();
        BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                IBeacon iBeacon = IBeacon.fromScanData(scanRecord, rssi, device);
                if (iBeacon != null) {
                    iBeacon.setUserId(mBluetoothAdapter.getAddress());
                    iBeacon.setScanTime(System.currentTimeMillis());
                    bluetoothDevicesWithRssi.add(iBeacon.toString());
                }
            }
        };
        mBluetoothAdapter.startLeScan(leScanCallback);

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Timber.d(e, "Couldn't scan during whole period");
        }
        mBluetoothAdapter.stopLeScan(leScanCallback);

        sendLocations(bluetoothDevicesWithRssi);

        disableBluetooth();
    }

    private void activateBluetooth() {
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothWasDisabled = true;
            // TODO check preferences
            mBluetoothAdapter.enable();
        }
    }

    private void sendLocations(Set<String> bluetoothDevicesWithRssi) {
        int bluetoothDevicesWithRssiCount = bluetoothDevicesWithRssi.size();
        if (bluetoothDevicesWithRssiCount > 0) {
            try {
                KouignAmanApplication.getBleLocationApi()
                        .sendLocations(bluetoothDevicesWithRssi.toArray(new String[bluetoothDevicesWithRssiCount]));
                deleteFile(PENDING_DATA_FILE);
            } catch (RetrofitError e) {
                storePendingData(bluetoothDevicesWithRssi);
            }
        }
    }

    private Set<String> retrievePendingData() {
        File pendingDataFile = new File(getFilesDir() + "/" + PENDING_DATA_FILE);
        ObjectInputStream objectInputStream = null;
        if (pendingDataFile.exists()) {
            try {
                objectInputStream = new ObjectInputStream(openFileInput(PENDING_DATA_FILE));
                return (HashSet<String>) objectInputStream.readObject();
            } catch (ClassNotFoundException | IOException e) {
                Timber.d(e, "Couldn't retrieve pending data");
            } finally {
                if (objectInputStream != null) {
                    try {
                        objectInputStream.close();
                    } catch (IOException e) {
                        Timber.d(e, "Couldn't close input stream");
                    }
                }
            }
        }
        return new HashSet<>();
    }

    private void disableBluetooth() {
        if (mBluetoothWasDisabled) {
            mBluetoothAdapter.disable();
        }
    }

    private void storePendingData(Set<String> bluetoothDevicesWithRssi) {
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(openFileOutput(PENDING_DATA_FILE, MODE_PRIVATE));
            objectOutputStream.writeObject(bluetoothDevicesWithRssi);
            objectOutputStream.flush();
        } catch (IOException e) {
            Timber.d(e, "Couldn't save pending data");
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    Timber.d(e, "Couldn't close output stream");
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        if (mBleLooper != null) {
            mBleLooper.quit();
        }
        super.onDestroy();
    }
}
