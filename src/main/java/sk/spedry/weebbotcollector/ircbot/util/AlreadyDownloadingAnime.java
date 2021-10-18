package sk.spedry.weebbotcollector.ircbot.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class AlreadyDownloadingAnime {
    @Getter
    @Setter
    private String bot;
    @Getter
    @Setter
    private String message;
}
