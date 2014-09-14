package fr.xebia.conference.companion.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Schedule {

    private Map<String, List<Talk>> talksPerDay = new LinkedHashMap<>();
    private DateFormat dateFormatter = new SimpleDateFormat("EEEE");

    public Schedule(List<Talk> talks){
        buildSchedule(talks);
    }

    private void buildSchedule(List<Talk> talks) {
        talksPerDay.clear();
        for (Talk talk : talks) {
            addTalkToSchedule(talk);
        }
    }

    private void addTalkToSchedule(Talk talk) {
        String day = dateFormatter.format(talk.getFromTime()).toLowerCase();
        List<Talk> talksForDay = talksPerDay.get(day);
        if (talksForDay == null) {
            talksForDay = new ArrayList<>();
            talksPerDay.put(day, talksForDay);
        }
        talksForDay.add(talk);
    }

    public Map<String, List<Talk>> getTalksPerDay() {
        return talksPerDay;
    }

    public List<String> getFormattedDays(){
        List<String> daysFormatted = new ArrayList<>();
        for (String day : talksPerDay.keySet()) {
            daysFormatted.add(day.substring(0, 1).toUpperCase() + day.substring(1, day.length()));
        }
        return daysFormatted;
    }

    public boolean isEmpty() {
        return talksPerDay.isEmpty();
    }


    public List<Talk> forDay(String day) {
        return talksPerDay.get(day.toLowerCase());
    }

    public int getConferenceDaysCount() {
        return talksPerDay.keySet().size();
    }
}
