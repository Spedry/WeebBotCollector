package sk.spedry.weebbotcollector.work;

import com.google.gson.*;
import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.ircbot.WBCService;
import sk.spedry.weebbotcollector.util.WCMAnime;
import sk.spedry.weebbotcollector.util.WCMProgress;
import sk.spedry.weebbotcollector.util.WCMSetup;
import sk.spedry.weebbotcollector.util.WCMessage;
import sk.spedry.weebbotcollector.util.lists.AnimeList;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

public class WBCWorkPlace extends WBCMessageSender {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final WBCService service;

    private final String userDir = System.getProperty("user.dir");
    private final String propertiesFolder = userDir + "/properties/";
    private final String jsonListFolder = propertiesFolder + "jsonListFile/";
    @Getter
    private final String animeListFile = jsonListFolder + "animeList.json";

    public WBCWorkPlace() {
        service = new WBCService();
        startIRCBot(new WCMessage("startIRCBot"));
        logger.debug("Testing if folders exists");
        createFolder(propertiesFolder);
        createFolder(jsonListFolder);
    }

    public void createFolder(@NonNull String path) {
        File directory = new File(path);
        if (!directory.exists()){
            logger.debug("Creating folder: {}", path);
            directory.mkdirs();
        }
    }

    private JsonArray getJsonArray(@NonNull String pathTo) {
        try {
            File file = new File(pathTo);
            if (!file.createNewFile()) {
                logger.debug("File {} already exists", file.getName());
                if (file.length() == 0) {
                    logger.debug("File {} was empty", file.getName());
                    return new JsonArray();
                } // if file exists but is empty create empty array
                else {
                    FileReader fileReader = new FileReader(pathTo);
                    JsonObject jsonObject = (JsonObject) JsonParser.parseReader(fileReader);
                    logger.debug("The content of json is: " + jsonObject);
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
        // put already existed animes into list
        int pos = 0;
        while (iterator.hasNext()) {
            WCMAnime wcmAnime = new Gson().fromJson(iterator.next().toString(), WCMAnime.class);
            wcmAnime.setId(pos++);
            animeList.addAnime(wcmAnime);
        }
        return animeList;
    }

    private WCMAnime getAnime(int id) {
        for (WCMAnime anime : getAnimeList().getAnimeList()) {
            if (anime.getId() == id)
                return anime;
        }
        return null;
    }

    private WCMAnime getAnime(String animeName) {
        for (WCMAnime anime : getAnimeList().getAnimeList()) {
            if (anime.getAnimeName().equals(animeName))
                return anime;
        }
        return null;
    }

    public void increaseAnimeDownload(@NonNull String animeName) {
        WCMAnime anime = getAnime(animeName);
        anime.increaseNumberOfDownloadedEpisodes();
        saveUpdatedAnime(anime);
        send("setProgress", new WCMProgress(0));
    }

    public WCMSetup getSetup() {
        return new Gson().fromJson(getJsonObject(propertiesFolder + "setup.json"), WCMSetup.class);
    }

    /**
     * Methods that communicate with client
     * all ends with sendMessage();
     */

    public void send(String id, Object object) {
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
        } catch (IOException e) {
            logger.error("File reader: ", e);
        }
    }

    public void getAnimeList(WCMessage wcMessage) {
        send(wcMessage.getMessageId(), getAnimeList());
    }

    public void setSetup(WCMessage wcMessage) {
        try {
            FileWriter fileWriter = new FileWriter(propertiesFolder + "setup.json");
            WCMSetup setup = new Gson().fromJson(wcMessage.getMessageBody(), WCMSetup.class);
            new Gson().toJson(setup, fileWriter);
            fileWriter.close();
            send("getSetup", getSetup());
        } catch (IOException e) {
            logger.error("Couldn't find the file in given path", e);
        }
    }

    public void getSetup(WCMessage wcMessage) {
        send(wcMessage.getMessageId(), getSetup());
    }

    public void startIRCBot(WCMessage wcMessage) {
        logger.info("Starting bot");
        WCMSetup setup = getSetup();
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
}
