package fr.xebia.conference.companion.model;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

@Table("Talks")
public class Track extends Model {

    @Column("track") @Key String title;
    @Column("count") int count;

    public String getTitle() {
        return title;
    }

    public int getCount() {
        return count;
    }
}
