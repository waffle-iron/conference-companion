package fr.xebia.conference.companion.model;

import fr.xebia.conference.companion.R;

import java.util.HashMap;
import java.util.Map;

public class TrackResource {

    private static final Map<String, Integer> ICON_BY_TRACK = new HashMap<>();
    private static final Map<String, Integer> COLOR_RES_BY_TRACK = new HashMap<>();

    static {
        ICON_BY_TRACK.put("Future<Devoxx>", R.drawable.ic_futur);
        COLOR_RES_BY_TRACK.put("Future<Devoxx>", R.color.track_future);

        ICON_BY_TRACK.put("Java SE, Java EE", R.drawable.ic_java);
        COLOR_RES_BY_TRACK.put("Java SE, Java EE", R.color.track_java);

        ICON_BY_TRACK.put("Agilité, DevOps", R.drawable.ic_agile);
        COLOR_RES_BY_TRACK.put("Agilité, DevOps", R.color.track_agile);

        ICON_BY_TRACK.put("Web, HTML5", R.drawable.ic_web);
        COLOR_RES_BY_TRACK.put("Web, HTML5", R.color.track_web);

        ICON_BY_TRACK.put("Startup & Innovation", R.drawable.ic_startup);
        COLOR_RES_BY_TRACK.put("Startup & Innovation", R.color.track_startup);

        ICON_BY_TRACK.put("Mobile", R.drawable.ic_mobile);
        COLOR_RES_BY_TRACK.put("Mobile", R.color.track_mobile);

        ICON_BY_TRACK.put("Cloud, Big Data, NoSQL", R.drawable.ic_cloud);
        COLOR_RES_BY_TRACK.put("Cloud, Big Data, NoSQL", R.color.track_data);

        ICON_BY_TRACK.put("Langages alternatifs", R.drawable.ic_alternative);
        COLOR_RES_BY_TRACK.put("Langages alternatifs", R.color.track_language);
    }

    public static int getIconForTrack(String track) {
        Integer iconRes = ICON_BY_TRACK.get(track);
        return iconRes == null ? R.drawable.ic_futur : iconRes;
    }

    public static int getColorResForTrack(String track) {
        Integer iconRes = COLOR_RES_BY_TRACK.get(track);
        return iconRes == null ? R.color.track_future : iconRes;
    }
}
