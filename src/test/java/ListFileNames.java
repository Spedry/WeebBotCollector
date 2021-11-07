import java.io.File;

public class ListFileNames {

    private final static String userDir = System.getProperty("user.dir");
    private final static String propertiesFolder = userDir + "/properties/";
    private final static String jsonListFolder = propertiesFolder + "jsonListFolder/";

    public static void main(String[] args) {
        File folder = new File(jsonListFolder);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
    }
}
