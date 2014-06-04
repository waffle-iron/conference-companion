package fr.xebia.conference.companion.core.misc;


import fr.xebia.conference.companion.BuildConfig;

import java.util.Locale;

public final class Log {

    public static final boolean LOG_ERROR = BuildConfig.RELEASE_BUILD || BuildConfig.LOG_LEVEL > 0;
    public static final boolean LOG_WARN = BuildConfig.LOG_LEVEL > 1;
    public static final boolean LOG_INFO = BuildConfig.LOG_LEVEL > 2;
    public static final boolean LOG_DEBUG = BuildConfig.LOG_LEVEL > 3;

    private Log() {
        throw new UnsupportedOperationException();
    }

    public static void d(String tag, String msgFormat, Object... args) {
        if (LOG_DEBUG) {
            try {
                android.util.Log.d(tag, String.format(Locale.US, msgFormat, args));
            } catch (Exception e) {
                android.util.Log.d(tag, msgFormat);
                android.util.Log.d(tag, msgFormat, e);
            }
        }
    }

    public static void d(String tag, Throwable t, String msgFormat, Object... args) {
        if (LOG_DEBUG) {
            try {
                String msg = String.format(Locale.US, msgFormat, args);
                android.util.Log.d(tag, msg, t);
            } catch (Exception e) {
                android.util.Log.d(tag, msgFormat, t);
                android.util.Log.d(tag, msgFormat, e);
            }
        }
    }

    public static void w(String tag, String msgFormat, Object... args) {
        if (LOG_WARN) {
            try {
                String msg = String.format(Locale.US, msgFormat, args);
                android.util.Log.w(tag, msg);
            } catch (Exception e) {
                android.util.Log.w(tag, msgFormat);
                android.util.Log.w(tag, msgFormat, e);
            }
        }
    }

    public static void w(String tag, Throwable t) {
        if (LOG_WARN) {
            android.util.Log.w(tag, "", t);
        }
    }

    public static void i(String tag, String msgFormat, Object... args) {
        if (LOG_INFO) {
            try {
                String msg = String.format(Locale.US, msgFormat, args);
                android.util.Log.i(tag, msg);
            } catch (Exception e) {
                android.util.Log.i(tag, msgFormat);
                android.util.Log.i(tag, msgFormat, e);
            }
        }
    }

    public static void e(String tag, Throwable t) {
        if (LOG_ERROR) {
            android.util.Log.e(tag, "", t);
        }
    }

    public static void e(String tag, String msgFormat, Object... args) {
        if (LOG_ERROR) {
            try {
                String msg = String.format(Locale.US, msgFormat, args);
                android.util.Log.e(tag, msg);
            } catch (Exception e) {
                android.util.Log.e(tag, msgFormat);
                android.util.Log.e(tag, msgFormat, e);
            }
        }
    }

    public static void e(String tag, Throwable t, String msgFormat, Object... args) {
        if (LOG_ERROR) {
            try {
                String msg = String.format(Locale.US, msgFormat, args);
                android.util.Log.e(tag, msg, t);
            } catch (Exception e) {
                android.util.Log.e(tag, msgFormat, t);
                android.util.Log.e(tag, msgFormat, e);
            }
        }
    }
}
