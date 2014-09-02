package fr.xebia.conference.companion.ui.widget;

import android.graphics.Color;

/**
 * https://github.com/google/iosched/blob/master/android/src/main/java/com/google/samples/apps/iosched/util/UIUtils.java
 */
public class UIUtils {

    public static int setColorAlpha(int color, float alpha) {
        int alpha_int = Math.min(Math.max((int)(alpha * 255.0f), 0), 255);
        return Color.argb(alpha_int, Color.red(color), Color.green(color), Color.blue(color));
    }
}
