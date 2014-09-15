package fr.xebia.conference.companion.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Schedule {

    private Map<String, List<Talk>> talksPerDay = new LinkedHashMap<>();
    private Map<String, Integer> colorForTrack = new HashMap<>();
    private List<String> availableTopics = new ArrayList<>();
    private List<String> availableTypes = new ArrayList<>();
    private DateFormat dateFormatter = new SimpleDateFormat("EEEE");

    public Schedule(List<Talk> talks) {
        this(talks, false);
    }

    public Schedule(List<Talk> talks, boolean filterBreaks) {
        buildSchedule(talks, filterBreaks);
    }

    private void buildSchedule(List<Talk> talks, boolean filterBreaks) {
        talksPerDay.clear();
        Set<String> availableTopicsSet = new HashSet<>();
        Set<String> availableTypesSet = new HashSet<>();
        for (Talk talk : talks) {
            if (!talk.isBreak() || !filterBreaks) {
                addTalkToSchedule(talk);
                if (!talk.isBreak()) {
                    availableTypesSet.add(talk.getType());
                    String track = talk.getTrack();
                    colorForTrack.put(track, talk.getColor());
                    availableTopicsSet.add(track);
                }
            }
        }
        availableTypes = new ArrayList<>(availableTypesSet);
        availableTopics = new ArrayList<>(availableTopicsSet);

        Collections.sort(availableTypes);
        Collections.sort(availableTopics);
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

    public List<String> getFormattedDays() {
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

    public int getColorForTrack(String track) {
        return colorForTrack.get(track);
    }

    public List<String> getAvailableTopics() {
        return availableTopics;
    }

    public List<String> getAvailableTypes() {
        return availableTypes;
    }

    public List<Talk> getFilteredTalks(String dayFilter, String topicFilter, String typeFilter) {
        List<Talk> filteredTalks = new ArrayList<>();
        if (!"".equals(dayFilter)) {
            filteredTalks.addAll(forDay(dayFilter));
        } else {
            for (String day : getFormattedDays()) {
                filteredTalks.addAll(forDay(day));
            }
        }

        Iterator<Talk> filteredTalksIt = filteredTalks.iterator();
        while (filteredTalksIt.hasNext()) {
            Talk talk = filteredTalksIt.next();
            if ((!"".equals(topicFilter) && !talk.getTrack().equals(topicFilter))
                    || (!"".equals(typeFilter) && !talk.getType().equals(typeFilter))) {
                filteredTalksIt.remove();
            }
        }
        return filteredTalks;
    }

}
