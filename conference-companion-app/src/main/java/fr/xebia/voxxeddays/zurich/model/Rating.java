package fr.xebia.voxxeddays.zurich.model;

public class Rating {

    public final String user;
    public final int rating;
    public final String talkId;

    public Rating(String user, int rating, String talkId) {
        this.user = user;
        this.rating = rating;
        this.talkId = talkId;
    }
}
