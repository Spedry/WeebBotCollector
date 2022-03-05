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
import java.util.Iterator;

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

    public WBCWorkPlace() {
        this.service = new WBCService();
        this.conf = new Configuration();
        startIRCBot(new WCMessage("startIRCBot"));
        logger.debug("Testing if folders exists");
        createFolder(propertiesFolder);
        createFolder(jsonListFolder);
    }

    public void createFolder(@NonNull String path) {
        File directory = new File(path);
        if (!directory.exists()){
            logger.debug("Creating folder: {}", path);
            if(directory.mkdirs())
                logger.debug("Folder created");
        }
    }

    private JsonArray getJsonArray(@NonNull String pathTo) {
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
        return new JsonObject();
    }

    public AnimeList getAnimeList() {
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
        return animeList;
    }

    public WCMAnime getAnime(int id) {
        for (WCMAnime anime : getAnimeList().getAnimeList()) {
            if (anime.getId() == id)
                return anime;
        }
        return null;
    }

    public WCMAnime getAnime(String animeName) {
        for (WCMAnime anime : getAnimeList().getAnimeList()) {
            if (anime.getAnimeName().equals(animeName))
                return anime;
        }
        return null;
    }

    /**
     * Methods that communicate with client
     * all ends with sendMessage();
     * aren't used in switch
     */

    public void increaseAnimeDownload(@NonNull String animeName) {
        logger.debug("Increasing number of downloaded episodes of anime {}", animeName);
        WCMAnime anime = getAnime(animeName);
        assert anime != null;
        anime.increaseNumberOfDownloadedEpisodes();
        saveUpdatedAnime(anime);
        send("setProgress", 0);
    }

    public void setWasDownloaded(@NonNull String animeName, boolean wasDownloaded) {
        logger.debug("Setting wasDownloaded to {} for anime {}", wasDownloaded, animeName);
        WCMAnime anime = getAnime(animeName);
        assert anime != null;
        anime.setWasDownloaded(wasDownloaded);
        saveUpdatedAnime(anime);
        send("animeList", getAnimeList());
    }

    public void setReleaseDate(@NonNull String animeName) {
        LocalDate localDate = LocalDate.now();
        logger.debug("Setting releaseDate to {} for anime {}", localDate, animeName);
        WCMAnime anime = getAnime(animeName);
        assert anime != null;
        anime.setReleaseDate(localDate);
        saveUpdatedAnime(anime);
        send("animeList", getAnimeList());
    }

    private void saveUpdatedAnime(WCMAnime anime) {
        try {
            AnimeList animeList = getAnimeList();
            // put file reader after the file was read, file reader will delete it's content
            FileWriter fileWriter = new FileWriter(animeListFile);
            // edit existing anime in the list
            animeList.updateAnime(anime.getId(), anime);
            logger.debug("Update anime entry with id: {}", anime.getId());
            // save content of the list into file
            new Gson().toJson(animeList, fileWriter);
            fileWriter.close();
            send("editAnimeEntry", getAnimeList());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        try {
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
            }
        } catch (IOException e) {
            logger.error("File reader: ", e);
        }
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
    }

    public void updateAnime(WCMessage wcMessage) {
        saveUpdatedAnime(new Gson().fromJson(wcMessage.getMessageBody(), WCMAnime.class));
        logger.debug("Saving file: success");
        send("animeList", getAnimeList());
    }

    public void removeAnimeFromList(WCMessage wcMessage) {
        try {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getAnimeToOpen(WCMessage wcMessage) {
        String animeName = new Gson().fromJson(wcMessage.getMessageBody(), String.class);
        File folder = new File(conf.getProperty("downloadFolder") + "/" + animeName);
        logger.debug("Path is: {}", folder.getPath());
        if (folder.exists()) {
            File[] listOfFiles = folder.listFiles();

            /*for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    System.out.println("File " + listOfFiles[i].getName());
                } else if (listOfFiles[i].isDirectory()) {
                    System.out.println("Directory " + listOfFiles[i].getName());
                }
            }*/
            assert listOfFiles != null;
            send("setAnimeToOpen", animeName + "/" + listOfFiles[listOfFiles.length-1].getName());
        }
        else {
            logger.error("Folder {} at path {} didn't exist", folder.getName(), folder.getPath());
        }
    }
    //TODO
    public void getDownloadQueueList(WCMessage wcMessage) {
        send("setDownloadQueueList", botCommands.getDownloadQueueList());
    }
    //TODO
    public void getAlreadyReleasedQueueList(WCMessage wcMessage) {
        send("setAlreadyReleasedQueueList", botCommands.getAlreadyReleasedQueueList());
    }
    //TODO
    public void getCurrentlyDownloadingAnime(WCMessage wcMessage) {
        send("setCurrentlyDownloadingAnime", botCommands.getCurrentlyDownloadingAnime());
    }

    public void setWasDownloaded(WCMessage wcMessage) {
        setWasDownloaded(new Gson().fromJson(wcMessage.getMessageBody(), String.class), false);
    }
}
