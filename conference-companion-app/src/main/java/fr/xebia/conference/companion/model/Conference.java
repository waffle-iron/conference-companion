package fr.xebia.conference.companion.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Table("Conferences")
public class Conference extends Model {

    @JsonProperty @Column("_id") @Key private int id;
    @JsonProperty @Column("name") private String name;
    @JsonProperty @Column("description") private String description;
    @JsonProperty @Column("location") private String location;
    @JsonProperty @Column("backgroundUrl") private String backgroundUrl;
    @JsonProperty @Column("logoUrl") private String logoUrl;
    @JsonProperty @JsonFormat(pattern = "yyyy-MM-dd") @Column("fromDate") private Date from;
    @JsonProperty @JsonFormat(pattern = "yyyy-MM-dd") @Column("toDate") private Date to;
    @JsonProperty @Column("enabled") private boolean enabled;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public Date getFrom() {
        return from;
    }

    public Date getTo() {
        return to;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
