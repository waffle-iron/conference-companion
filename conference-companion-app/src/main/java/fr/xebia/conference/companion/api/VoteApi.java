package fr.xebia.conference.companion.api;

import fr.xebia.conference.companion.model.Vote;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Path;

public interface VoteApi {

    @GET("/vote/{nfcId}")
    @Headers("Accept: application/json")
    public void getVotes(@Path("nfcId") String nfcId, Callback<Vote[]> votes);

}
