package fr.xebia.conference.companion.api;

import fr.xebia.conference.companion.model.Conference;
import fr.xebia.conference.companion.model.Speaker;
import fr.xebia.conference.companion.model.Talk;
import retrofit.http.GET;
import retrofit.http.Path;

import java.util.List;

public interface ConferenceApi {
    @GET("/conferences")
    public List<Conference> getAvailableConferences();

    @GET("/conferences/{conferenceId}/schedule")
    public List<Talk> getSchedule(@Path("conferenceId") int conferenceId);

    @GET("/conferences/{conferenceId}/presentations")
    public List<Talk> getTalks(@Path("conferenceId") int conferenceId);

    @GET("/conferences/{conferenceId}/speakers")
    public List<Speaker> getSpeakers(@Path("conferenceId") int conferenceId);
}
