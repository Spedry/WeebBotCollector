package sk.spedry.weebbotcollector.work;

import com.google.gson.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.ircbot.IRCBotCommands;
import sk.spedry.weebbotcollector.ircbot.IRCBotListener;
import sk.spedry.weebbotcollector.ircbot.WBCService;
import sk.spedry.weebbotcollector.properties.Configuration;
import sk.spedry.weebbotcollector.util.*;
import sk.spedry.weebbotcollector.util.lists.AnimeList;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

public class WBCWorkPlace extends WBCMessageSender {

    private final Logger logger = LogManager.getLogger(this.getClass());
    // Only to start/stop/reset bot
    private final WBCService service;
    @Getter
    private final Configuration conf;
    @Setter
    private IRCBotCommands botCommands;


    private final String userDir = System.getProperty("user.dir");
    @Getter
    private final String propertiesFolder = userDir + "/properties/";
    @Getter
    private final String jsonListFolder = propertiesFolder + "jsonListFolder/";
    @Getter
    private final String animeListFile = jsonListFolder + "animeList.json";
    private final String copyOfAnimeListFile = jsonListFolder + "animeListCopy.json";

    public WBCWorkPlace() {
        logger.traceEntry();
        this.service = new WBCService();
        this.conf = new Configuration();
        startIRCBot(new WCMessage("startIRCBot"));
        logger.debug("Testing if folders exists");
        createFolder(propertiesFolder);
        createFolder(jsonListFolder);
        logger.traceExit();
    }

    public void createFolder(@NonNull String path) {
        logger.traceEntry(path);
        File directory = new File(path);
        if (!directory.exists()){
            logger.debug("Creating folder: {}", path);
            if(directory.mkdirs())
                logger.debug("Folder created");
        }
        logger.traceExit(true);
    }

    private JsonArray getJsonArray(@NonNull String pathTo) {
        logger.traceEntry(pathTo);
        try {
            File file = new File(pathTo);
            if (!file.createNewFile()) {
                //logger.debug("File {} already exists", file.getName());
                if (file.length() == 0) {
                    logger.debug("File {} was empty", file.getName());
                    return new JsonArray();
                } // if file exists but is empty create empty array
                else {
                    FileReader fileReader = new FileReader(pathTo);
                    JsonObject jsonObject = (JsonObject) JsonParser.parseReader(fileReader);
                    return (JsonArray) jsonObject.get("list");
                } // else read the content and save it into jsonArray
            } // if the file exists check if file isn't empty
            else {
                logger.debug("File {} was created", file.getName());
                return new JsonArray();
            } // else create empty jsonArray
        } catch (IOException e) {
            logger.error("Couldn't find the file in given path: " + pathTo, e);
        }
        return new JsonArray();
    }

    private JsonObject getJsonObject(String pathTo) {
        logger.traceEntry(pathTo);
        try {
            File file = new File(pathTo);
            if (!file.createNewFile()) {
                logger.debug("File {} already exists", file.getName());
                if (file.length() == 0) {
                    logger.debug("File {} was empty", file.getName());
                    return new JsonObject();
                }
                else {
                    FileReader fileReader = new FileReader(file);
                    return (JsonObject) JsonParser.parseReader(fileReader);
                }
            }
            else {
                logger.debug("File {} was created", file.getName());
                return new JsonObject();
            }
        } catch (IOException e) {
            logger.error("Couldn't find the file in given path: " + pathTo, e);
        }
        return logger.traceExit(new JsonObject());
    }

    public AnimeList getAnimeList() {
        logger.traceEntry();
        AnimeList animeList = new AnimeList();
        JsonArray jsonArray = getJsonArray(animeListFile);
        Iterator<JsonElement> iterator = jsonArray.iterator();
        // put already existed anime into list
        int pos = 0;
        while (iterator.hasNext()) {
            WCMAnime wcmAnime = new Gson().fromJson(iterator.next().toString(), WCMAnime.class);
            wcmAnime.setId(pos++);
            animeList.addAnime(wcmAnime);
        }
        return logger.traceExit(animeList);
    }

    public WCMAnime getAnime(int id) {
        logger.traceEntry(String.valueOf(id));
        for (WCMAnime anime : getAnimeList().getAnimeList()) {
            if (anime.getId() == id)
                return logger.traceExit(anime);
        }
        logger.traceExit(false);
        return null;
    }

    public WCMAnime getAnime(String animeName) {
        logger.traceEntry(animeName);
        for (WCMAnime anime : getAnimeList().getAnimeList()) {
            if (anime.getAnimeName().equals(animeName))
                return logger.traceExit(anime);
        }
        logger.traceExit(false);
        return null;
    }

    private void searchAlreadyReleased(WCMessage wcMessage, WCMAnime wcmAnime) {
        logger.traceEntry(wcmAnime.getAnimeName());
        WCMDownAlrRel downAlrRel = new Gson().fromJson(wcMessage.getAdditionalData(), WCMDownAlrRel.class);
        if (downAlrRel.isDownload()) {
            int numberOfEpisodes = downAlrRel.getAlreadyReleasedEp();
            if (numberOfEpisodes == 0) {
                logger.warn("TODO");
                //botCommands.searchAnime(conf.getProperty("searchBot"), wcmAnime.getAnimeName(), [ ,wcmAnime.getTypeOfQuality()]);
            }
            else {
                for (int i = 1; i < numberOfEpisodes + 1; i++) {
                    try {
                        //logger.debug(conf.getProperty("searchBot") + " " + wcmAnime.getAnimeName() + " " +  String.valueOf(i) + " " +  wcmAnime.getTypeOfQuality().getName());
                        String number;
                        if (i < 10)
                            number = "0" + i;
                        else
                            number = String.valueOf(i);
                        logger.debug("Cycle to search for already released anime, searching anime {} episode {}", wcmAnime.getAnimeName(), number);
                        botCommands.searchAnime(conf.getProperty("searchBot"), conf.getProperty("downloadFrom"), wcmAnime.getAnimeName(), number, wcmAnime.getTypeOfQuality().getName());
                        java.util.concurrent.TimeUnit.SECONDS.sleep(Integer.parseInt(conf.getProperty("waitBeforeSearch")));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                synchronized (IRCBotListener.class) {
                    IRCBotListener.class.notify();
                }
            }
        }
        logger.traceExit();
    }

    private void backupAnimeListFile(@NonNull String pathToStoreCopy, @NonNull String pathToOriginalFile) {
        logger.traceEntry("Backup {}", pathToOriginalFile);
        try {
            if (new File(pathToOriginalFile).length() != 0) {
                Path copied = Paths.get(pathToStoreCopy);
                Path originalPath = Paths.get(pathToOriginalFile);
                Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);

                if (testIfFileExists(pathToStoreCopy)) {
                    if (testIfFilesAreEqual(pathToStoreCopy, pathToOriginalFile)) {
                        logger.traceExit("Files are equal");
                    } else {
                        logger.traceExit("Files aren't equal");
                    }
                }
            }
            else {
                logger.traceExit("Files aren't equal");
                throw new Exception();
            }
        } catch (IOException e) {
            logger.error("Couldn't copy animeList file: {}", e.toString());
        } catch (Exception e) {
            logger.error("File wasn't backed up because original file {} was empty", pathToOriginalFile);
        }
    }

    private boolean testIfFileExists(@NonNull String pathTo) {
        logger.traceEntry("Test if file {} exists", pathTo);
        File f = new File(pathTo);
        if(f.exists() && !f.isDirectory()) {
            return logger.traceExit(true);
        }
        return logger.traceExit(false);
    }

    private boolean testIfFilesAreEqual(@NonNull String file1, @NonNull String file2) {
        logger.traceEntry("Compare {} with {}", file1, file2);
        try {
            Path filePath1 = Paths.get(file1);
            Path filePath2 = Paths.get(file2);

            long mismatch = Files.mismatch(filePath1, filePath2);

            if (mismatch == -1) {
                logger.debug("Files ({} and {}) are equal",file1, file2);
                return logger.traceExit(true);
            }
            else {
                throw new Exception();
            }

        } catch (IOException e) {
            logger.error("Couldn't mismatch files: {}", e.toString());
        } catch (Exception e) {
            logger.error("Files ({} and {}) aren't equal", file1, file2);
        }
        return logger.traceExit(false);
    }

    private void saveUpdatedAnime(WCMAnime anime) {
        logger.traceEntry(anime.getAnimeName());
        try {
            // backup file before anything
            if (conf.getBoolProperty("isBcup"))
                backupAnimeListFile(copyOfAnimeListFile, animeListFile);
            AnimeList animeList = getAnimeList();
            // put file reader after the file was read, file reader will delete it's content
            FileWriter fileWriter = new FileWriter(animeListFile);
            // edit existing anime in the list
            animeList.updateAnime(anime.getId(), anime);
            logger.debug("Update anime entry with id: {}", anime.getId());
            // save content of the list into file
            new Gson().toJson(animeList, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.traceExit();
    }

    /**
     * Methods that communicate with client
     * all ends with sendMessage();
     * aren't used in switch
     */

    public void increaseAnimeDownload(@NonNull String animeName) {
        logger.traceEntry(animeName);
        WCMAnime anime = getAnime(animeName);
        assert anime != null;
        anime.increaseNumberOfDownloadedEpisodes();
        saveUpdatedAnime(anime);
        send("setProgress", 0);
        logger.traceExit(anime.getNumberOfDownloadedEpisodes());
    }

    public void updateNumberOfDownloadedEpisodes(String animeName) {
        logger.traceEntry(animeName);
        WCMAnime anime = getAnime(animeName);
        assert anime != null;
        File folder = new File(conf.getProperty("downloadFolder") + "/" + animeName);
        logger.debug("Path is: {}", folder.getPath());
        if (folder.exists()) {
            int tempNumberOfEpInFolder = Objects.requireNonNull(folder.listFiles()).length;
            logger.debug("Setting number of downloaded EP for anime {} to {}", animeName, tempNumberOfEpInFolder);
            anime.setNumberOfDownloadedEpisodes(tempNumberOfEpInFolder);
        }
        else {
            logger.error("Folder {} at path {} didn't exist", folder.getName(), folder.getPath());
            logger.warn("Increasing number of episodes by +1");
            anime.increaseNumberOfDownloadedEpisodes();
        }
        saveUpdatedAnime(anime);
        send("setProgress", 0);
        logger.traceExit(anime.getNumberOfDownloadedEpisodes());
    }

    public void setWasDownloaded(@NonNull String animeName, boolean wasDownloaded) {
        logger.traceEntry("{} : {}", animeName, wasDownloaded);
        WCMAnime anime = getAnime(animeName);
        assert anime != null;
        if (wasDownloaded) {
            if (anime.isWasDownloaded())
                anime.setMissedEpisode(true);
            else
                anime.setWasDownloaded(true);
        }
        else {
            anime.setWasDownloaded(false);
            anime.setMissedEpisode(false);
        }
        saveUpdatedAnime(anime);
        send("animeList", getAnimeList());
        logger.traceExit("WasDownloaded" + anime.isWasDownloaded() + "MissedEpisode" + anime.isMissedEpisode());
    }

    public void setReleaseDate(@NonNull String animeName) {
        logger.traceEntry(animeName);
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        logger.debug("Setting releaseDate to {} for anime {}", dayOfWeek, animeName);
        WCMAnime anime = getAnime(animeName);
        assert anime != null;
        anime.setReleaseDay(dayOfWeek);
        saveUpdatedAnime(anime);
        send("animeList", getAnimeList());
        logger.traceExit(anime.getReleaseDay());
    }


    /**
     * Methods that communicate with client
     * all ends with sendMessage();
     * are used in switch
     */

    public void send(String id, Object object) {
        sendMessage(new WCMessage(id, new Gson().toJson(object)));
    }

    public void send(String id, double object) {
        sendMessage(new WCMessage(id, new Gson().toJson(object)));
    }

    public void send(String id, String object) {
        sendMessage(new WCMessage(id, new Gson().toJson(object)));
    }

    public void send(String id) {
        sendMessage(new WCMessage(id));
    }

    public void addNewAnimeEntry(WCMessage wcMessage) {
        logger.traceEntry();
        try {
            // backup file before anything
            if (conf.getBoolProperty("isBcup"))
                backupAnimeListFile(copyOfAnimeListFile, animeListFile);
            AnimeList animeList = getAnimeList();
            // put file reader after the file was read, file reader will delete it's content
            FileWriter fileWriter = new FileWriter(animeListFile);
            // add new anime into list
            WCMAnime wcmAnime = new Gson().fromJson(wcMessage.getMessageBody(), WCMAnime.class);
            wcmAnime.setId(animeList.getSize());
            animeList.addAnime(wcmAnime);
            logger.debug("Size of server list: " + animeList.getSize());
            // save content of the list into file
            new Gson().toJson(animeList, fileWriter);
            fileWriter.close();
            logger.debug("Saving file: success");
            send(wcMessage.getMessageId(), animeList);

            if(wcMessage.getAdditionalData() != null) {
                searchAlreadyReleased(wcMessage, wcmAnime);
            }
        } catch (IOException e) {
            logger.error("File reader: ", e);
        }
        logger.traceExit();
    }

    public void getAnimeList(WCMessage wcMessage) {
        send(wcMessage.getMessageId(), getAnimeList());
    }

    public void setSetup(WCMessage wcMessage) {
        conf.setBotSetting(new Gson().fromJson(wcMessage.getMessageBody(), WCMSetup.class));
    }

    public WCMSetup getSetup() {
        return conf.getBotSetting();
    }

    public void getSetup(WCMessage wcMessage) {
        send(wcMessage.getMessageId(), conf.getBotSetting());
    }

    public void startIRCBot(WCMessage wcMessage) {
        logger.traceEntry();
        logger.info("Starting bot");
        WCMSetup setup = conf.getBotSetting();
        if (setup.getUserName() != null &&
                !setup.getUserName().isEmpty() &&
                setup.getDownloadFolder() != null &&
                !setup.getDownloadFolder().isEmpty() &&
                setup.getServerName() != null &&
                !setup.getServerName().isEmpty() &&
                setup.getChannelName() != null &&
                !setup.getChannelName().isEmpty()) {
            service.createBotThread(this);
            send(wcMessage.getMessageId());
        }
        logger.traceExit(service.isBotRunning());
    }

    public void updateAnime(WCMessage wcMessage) {
        logger.traceEntry();
        saveUpdatedAnime(new Gson().fromJson(wcMessage.getMessageBody(), WCMAnime.class));
        logger.debug("Saving file: success");
        send("animeList", getAnimeList());
        logger.traceExit();
    }

    public void removeAnimeFromList(WCMessage wcMessage) {
        logger.traceEntry();
        try {
            // backup file before anything
            if (conf.getBoolProperty("isBcup"))
                backupAnimeListFile(copyOfAnimeListFile, animeListFile);
            AnimeList animeList = getAnimeList();
            FileWriter fileWriter = new FileWriter(animeListFile);
            animeList.removeAnime(new Gson().fromJson(wcMessage.getMessageBody(), WCMAnime.class).getId());
            // recreate animeList
            int pos = 0;
            for (WCMAnime anime : animeList.getAnimeList()) {
                anime.setId(pos++);
            }
            // save content of the list into file
            new Gson().toJson(animeList, fileWriter);
            fileWriter.close();
            send("animeList", getAnimeList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.traceExit();
    }

    public void getAnimeToOpen(WCMessage wcMessage) {
        logger.traceEntry();
        String animeName = new Gson().fromJson(wcMessage.getMessageBody(), String.class);
        File folder = new File(conf.getProperty("downloadFolder") + "/" + animeName);
        logger.debug("Path is: {}", folder.getPath());
        if (folder.exists()) {
            File[] listOfFiles = folder.listFiles();
            String animeToOpen;
            assert listOfFiles != null;

            Arrays.sort(listOfFiles, Comparator.comparingLong(File::lastModified));

            for (File listOfFile : listOfFiles) {
                if (listOfFile.isFile()) {
                    System.out.println("File " + listOfFile.getName());
                } else if (listOfFile.isDirectory()) {
                    System.out.println("Directory " + listOfFile.getName());
                }
            }

            animeToOpen = listOfFiles[listOfFiles.length-1].getName();
            logger.info("Anime to open is {}", animeToOpen);

            send("setAnimeToOpen", animeName + "/" + animeToOpen);
        }
        else {
            logger.error("Folder {} at path {} didn't exist", folder.getName(), folder.getPath());
        }
        logger.traceExit();
    }
    //TODO
    public void getDownloadQueueList(WCMessage wcMessage) {
        send("setDownloadQueueList", botCommands.getDownloadQueueList());
    }
    //TODO
    public void getAlreadyReleasedQueueList(WCMessage wcMessage) {
        send("setAlreadyReleasedQueueList", botCommands.getAlreadyReleasedQueueList());
    }

    public void getCurrentlyDownloadingAnime(WCMessage wcMessage) {
        if (botCommands.getCurrentlyDownloadingAnime() != null)
            send("setCurrentlyDownloadingAnime", botCommands.getCurrentlyDownloadingAnime().getAnimeName());
        else
            send("setCurrentlyDownloadingAnime", "");
    }

    public void setWasDownloaded(WCMessage wcMessage) {
        setWasDownloaded(new Gson().fromJson(wcMessage.getMessageBody(), String.class), false);
    }
}
