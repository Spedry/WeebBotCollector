import sk.spedry.weebbotcollector.ircbot.IRCBotCommands;
import sk.spedry.weebbotcollector.ircbot.util.SplittedMessage;

import java.util.ArrayList;

public class EmptyTest {

    public static void main(String[] args) {
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("pes");
        arrayList.add("macka");
        arrayList.add("zajac");
        arrayList.add("kon");

        for (String animal : arrayList)
            System.out.println("Animal: " + animal);

        System.out.println(arrayList.remove(0));

        for (String animal : arrayList)
            System.out.println("Animal: " + animal);
    }
}
