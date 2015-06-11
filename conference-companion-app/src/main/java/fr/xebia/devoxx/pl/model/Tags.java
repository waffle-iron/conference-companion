package fr.xebia.devoxx.pl.model;

import java.util.HashMap;
import java.util.Map;

import fr.xebia.devoxx.pl.R;

public class Tags {

    // tag categories
    public static final String CATEGORY_DAY = "DAY";
    public static final String CATEGORY_TOPIC = "TOPIC";
    public static final String CATEGORY_TYPE = "TYPE";

    public static final Map<String, Integer> CATEGORY_DISPLAY_ORDERS = new HashMap<String, Integer>();

    public static final String[] FILTER_CATEGORIES =
            {CATEGORY_DAY, CATEGORY_TOPIC, CATEGORY_TYPE};

    public static final int[] EXPLORE_CATEGORY_ALL_STRING = {
            R.string.all_days, R.string.all_topics, R.string.all_types
    };

    public static final int[] FILTER_CATEGORY_TITLE = {
            R.string.days, R.string.topics, R.string.types
    };

    static {
        Tags.CATEGORY_DISPLAY_ORDERS.put(Tags.CATEGORY_DAY, 0);
        Tags.CATEGORY_DISPLAY_ORDERS.put(Tags.CATEGORY_TOPIC, 1);
        Tags.CATEGORY_DISPLAY_ORDERS.put(Tags.CATEGORY_TYPE, 2);
    }
}
