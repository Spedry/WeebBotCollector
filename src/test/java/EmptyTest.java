import sk.spedry.weebbotcollector.ircbot.IRCBotCommands;

public class EmptyTest {

    public static void main(String[] args) {
        IRCBotCommands botCommands = new IRCBotCommands(null);

        String[] spliced = botCommands.splitDownloadMessage("/MSG CR-HOLLAND|NEW XDCC SEND 18843");
        for (String string : spliced) {
            System.out.println(string);
        }
    }
}
