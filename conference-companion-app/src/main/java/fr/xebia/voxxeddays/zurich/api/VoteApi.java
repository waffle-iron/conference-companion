package fr.xebia.voxxeddays.zurich.api;

import fr.xebia.voxxeddays.zurich.model.Rating;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;

public interface VoteApi {
    @POST("/vote")
    Response sendRating(@Body Rating rating);
}
