package fr.xebia.conference.companion.api;

import fr.xebia.conference.companion.model.Conference;
import fr.xebia.conference.companion.model.Rating;
import fr.xebia.conference.companion.model.Speaker;
import fr.xebia.conference.companion.model.Talk;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
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

    @POST("/conferences/{conferenceId}/rating")
    public Response sendRating(@Path("conferenceId") int conferenceId, @Body Rating rating);
}
