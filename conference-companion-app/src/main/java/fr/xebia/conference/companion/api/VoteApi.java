package fr.xebia.conference.companion.api;

import fr.xebia.conference.companion.model.Rating;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;

public interface VoteApi {
    @POST("/vote")
    Response sendRating(@Body Rating rating);
}
