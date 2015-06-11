package fr.xebia.devoxx.pl.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

@JsonIgnoreProperties(ignoreUnknown = true)
@Table("Speakers")
public class Speaker extends Model implements Parcelable {

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

    public Speaker() {

    }

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

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public List<Talk> getTalks() {
        return talks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || (((Object) this).getClass()) != o.getClass()) return false;

        Speaker speaker = (Speaker) o;

        if (conferenceId != speaker.conferenceId) return false;
        if (id != null ? !id.equals(speaker.id) : speaker.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + conferenceId;
        return result;
    }

    protected Speaker(Parcel in) {
        id = in.readString();
        conferenceId = in.readInt();
        lang = in.readString();
        blog = in.readString();
        tweetHandle = in.readString();
        imageURL = in.readString();
        company = in.readString();
        bio = in.readString();
        lastName = in.readString();
        firstName = in.readString();
        if (in.readByte() == 0x01) {
            talks = new ArrayList<Talk>();
            in.readList(talks, Talk.class.getClassLoader());
        } else {
            talks = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(conferenceId);
        dest.writeString(lang);
        dest.writeString(blog);
        dest.writeString(tweetHandle);
        dest.writeString(imageURL);
        dest.writeString(company);
        dest.writeString(bio);
        dest.writeString(lastName);
        dest.writeString(firstName);
        if (talks == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(talks);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Speaker> CREATOR = new Parcelable.Creator<Speaker>() {
        @Override
        public Speaker createFromParcel(Parcel in) {
            return new Speaker(in);
        }

        @Override
        public Speaker[] newArray(int size) {
            return new Speaker[size];
        }
    };
}
