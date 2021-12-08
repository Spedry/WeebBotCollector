package sk.spedry.weebbotcollector.ircbot;

public class IRCBotWorkPlace {

    public String bytesToReadable(long bytes) {
        double size_kb = bytes / 1024;
        double size_mb = size_kb / 1024;
        double size_gb = size_mb / 1024 ;

        if (size_gb > 0){
            return String.format("%.3f", size_gb) + " GB";
        } else if(size_mb > 0) {
            return String.format("%.3f", size_gb) + " MB";
        } else {
            return String.format("%.3f", size_gb) + " KB";
        }
    }
}
