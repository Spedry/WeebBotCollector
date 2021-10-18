import lombok.NonNull;

import java.io.File;

public class CreateFolder {
    private final String userDir = System.getProperty("user.dir");
    private final String propertiesFile = userDir + "/testFolder/";
    private final String jsonListFile = propertiesFile + "jsonListFile/";

    private void createFolder(@NonNull String path) {
        File directory = new File(path);
        if (!directory.exists()){
            directory.mkdirs();
        }
    }

    public static void main(String[] args) {
        CreateFolder createFolder = new CreateFolder();
        createFolder.createFolder(createFolder.jsonListFile);
    }

}
