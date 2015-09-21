package fr.xebia.devoxx.be.api;

import java.util.List;

import fr.xebia.devoxx.be.model.Conference;
import fr.xebia.devoxx.be.model.Speaker;
import fr.xebia.devoxx.be.model.Talk;
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
