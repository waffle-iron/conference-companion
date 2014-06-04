package fr.xebia.conference.companion.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Table("Speakers")
public class Speaker extends Model {

    @JsonProperty @Column("_id") @Key private String id;
    @JsonProperty @Column("conferenceId") private int conferenceId;
    @JsonProperty @Column("lang") private String lang;
    @JsonProperty @Column("blog") private String blog;
    @JsonProperty @Column("tweetHandle") private String tweetHandle;
    @JsonProperty @Column("imageURL") private String imageURL;
    @JsonProperty @Column("company") private String company;
    @JsonProperty @Column("bio") private String bio;
    @JsonProperty @Column("lastName") private String lastName;
    @JsonProperty @Column("firstName") private String firstName;
    @JsonProperty private List<Talk> talks;

    public String getId() {
        return id;
    }

    public int getConferenceId() {
        return conferenceId;
    }

    public String getLang() {
        return lang;
    }

    public String getBlog() {
        return blog;
    }

    public String getTweetHandle() {
        if (tweetHandle != null && !tweetHandle.startsWith("@")) {
            tweetHandle = "@" + tweetHandle;
        }
        return tweetHandle;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getCompany() {
        return company;
    }

    public String getBio() {
        return bio;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public List<Talk> getTalks() {
        return talks;
    }
}
