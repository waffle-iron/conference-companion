package fr.xebia.conference.companion.core.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import fr.xebia.conference.companion.R;

public class Compatibility {

    public static boolean isBleAvailable(Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                || context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static String capitalize(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    public static String getLocalizedTitle(Context context, String title) {
        return context.getResources().getBoolean(R.bool.french) ? title : translateTitle(title);
    }

    private static String translateTitle(String title) {
        if (title.startsWith("Accueil")) {
            return "Registration, Welcome and Breakfast";
        } else if (title.startsWith("Pause déjeuner")) {
            return "Lunch";
        } else if (title.startsWith("Pause café")) {
            return "Coffee Break";
        } else if (title.startsWith("Pause courte")) {
            return "Short Break";
        } else if (title.startsWith("Soirée")) {
            return "Night at Noxx";
        }
        return title;
    }
}
