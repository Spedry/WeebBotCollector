package sk.spedry.weebbotcollector.ircbot.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class DownloadMessage {
    @Getter
    @Setter
    private String botName;
    @Getter
    @Setter
    private String message;
}
