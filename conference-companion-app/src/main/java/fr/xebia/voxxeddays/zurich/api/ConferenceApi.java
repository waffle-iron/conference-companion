package fr.xebia.voxxeddays.zurich.api;

import java.util.List;

import fr.xebia.voxxeddays.zurich.model.Conference;
import fr.xebia.voxxeddays.zurich.model.Speaker;
import fr.xebia.voxxeddays.zurich.model.Talk;
import retrofit.http.GET;
import retrofit.http.Path;

public interface ConferenceApi {
    @GET("/conferences")
    List<Conference> getAvailableConferences();

    @GET("/conferences/{conferenceId}/schedule")
    List<Talk> getSchedule(@Path("conferenceId") int conferenceId);

    @GET("/conferences/{conferenceId}/speakers")
    List<Speaker> getSpeakers(@Path("conferenceId") int conferenceId);
}