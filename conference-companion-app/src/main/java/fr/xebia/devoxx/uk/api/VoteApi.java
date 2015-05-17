package fr.xebia.devoxx.uk.api;

import fr.xebia.devoxx.uk.model.Rating;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;

public interface VoteApi {
    @POST("/vote")
    Response sendRating(@Body Rating rating);
}
