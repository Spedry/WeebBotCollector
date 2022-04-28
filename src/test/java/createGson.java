import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;

public class createGson {

    public static void main(String[] args) throws IOException {
        String json = new Gson().toJson(java.time.LocalDate.now().getDayOfWeek());
        System.out.println(json);
        FileWriter fileWriter = new FileWriter("C:\\Users\\Spedry\\Desktop\\gson.json");
        new Gson().toJson(json, fileWriter);
        fileWriter.close();
    }
}
