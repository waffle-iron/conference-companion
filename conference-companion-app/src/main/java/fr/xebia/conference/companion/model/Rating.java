package fr.xebia.conference.companion.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class Rating {

    public final String deviceId;
    public final int conferenceId;
    public final int rating;
    public final String presentationId;

    public Rating(String deviceId, int conferenceId, int rating, String presentationId) {
        this.deviceId = deviceId;
        this.conferenceId = conferenceId;
        this.rating = rating;
        this.presentationId = presentationId;
    }
}
