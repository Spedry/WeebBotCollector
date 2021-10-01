package sk.spedry.weebbotcollector.gsonobjects;

import lombok.Getter;
import lombok.Setter;

public class GsAnimeEntry {
    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private String animeName;
    @Getter
    @Setter
    private int numberOfEpisodes;
    @Getter
    @Setter
    private String quality;

}
