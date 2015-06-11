package fr.xebia.devoxx.pl.api;

import fr.xebia.devoxx.pl.model.Rating;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;

public interface VoteApi {
    @POST("/vote")
    Response sendRating(@Body Rating rating);
}
