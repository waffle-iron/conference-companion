package fr.xebia.xebicon.api;

import fr.xebia.xebicon.model.Rating;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;

public interface VoteApi {
    @POST("/vote")
    Response sendRating(@Body Rating rating);
}
