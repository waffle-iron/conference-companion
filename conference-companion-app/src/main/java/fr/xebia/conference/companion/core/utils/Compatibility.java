package fr.xebia.conference.companion.core.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

public class Compatibility {

    public static boolean isBleAvailable(Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                || context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }
}
