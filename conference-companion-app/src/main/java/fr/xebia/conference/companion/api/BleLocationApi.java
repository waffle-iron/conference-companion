package fr.xebia.conference.companion.api;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;

public interface BleLocationApi {

    @POST("/locations")
    Response sendLocations(@Body String[] locations);
}
