package fr.xebia.voxxeddays.zurich.core.utils;

import android.content.Context;

import fr.xebia.voxxeddays.zurich.R;

public class Languages {
    public static String from(Context context, String code) {
        switch (code) {
            case "it":
                return context.getString(R.string.language_it);

            case "en":
                return context.getString(R.string.language_en);

            default:
                return "";
        }
    }
}
