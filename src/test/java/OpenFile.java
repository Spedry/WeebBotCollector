import com.google.gson.*;
import sk.spedry.weebbotcollector.util.WCMServer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;

public class OpenFile {

    private final String userDir = System.getProperty("user.dir");
    private final String jsonListFile = userDir + "/properties/jsonListFile/";

    public static void main(String[] args) {
        try {
            new OpenFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public OpenFile() throws FileNotFoundException {
        String pathTo = jsonListFile + "serverList.json";
        FileReader fileReader = new FileReader(pathTo);
        JsonObject jsonObject = (JsonObject) JsonParser.parseReader(fileReader);
        JsonArray jsonArray;
        jsonArray = (JsonArray) jsonObject.get("serverList");
        for (JsonElement jsonElement : jsonArray) {
            System.out.println(jsonElement.toString());
        }
    }
}
