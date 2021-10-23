package sk.spedry.weebbotcollector.work;

import com.google.gson.*;
import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.ircbot.WBCService;
import sk.spedry.weebbotcollector.util.WCMAnime;
import sk.spedry.weebbotcollector.util.WCMServer;
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

    public AnimeList getAnimeList(String listFile) {
        AnimeList animeList = new AnimeList();
        JsonArray jsonArray = getJsonArray(listFile);
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

    public WCMSetup getSetup() {
        return new Gson().fromJson(getJsonObject(propertiesFile + "setup.json"), WCMSetup.class);
    }

    /**
     * Methods that communicate with client
     *
     */
    //OLD
    public void addServer(WCMessage wcMessage) {
        try {
            ServerList serverList = getServerList(serverListFile);
            // put file reader after the file was read, file reader will delete it's content
            FileWriter fileWriter = new FileWriter(serverListFile);
            // add new server into list
            WCMServer wcmServer = new Gson().fromJson(wcMessage.getMessageBody(), WCMServer.class);
            wcmServer.setId((serverList.getSize()));
            serverList.addServer(wcmServer);
            logger.debug("Size of server list: " + serverList.getSize());
            // save content of the list into file
            new Gson().toJson(serverList, fileWriter);
            fileWriter.close();
            logger.debug("Saving file: success");
            sendMessage(new WCMessage("serverList", new Gson().toJson(serverList)));
        } catch (IOException e) {
            logger.error("Couldn't find the file in given path", e);
        }
    }
    //OLD
    public void getServerList(WCMessage wcMessage) {
        sendMessage(new WCMessage(wcMessage.getMessageId(), new Gson().toJson(getServerList(serverListFile))));
    }

    public void addNewAnimeEntry(WCMessage wcMessage) {
        try {
            AnimeList animeList = getAnimeList(animeListFile);
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
            sendMessage(new WCMessage(wcMessage.getMessageId(), new Gson().toJson(animeList)));
        } catch (IOException e) {
            logger.error("File reader: ", e);
        }
    }

    public void getAnimeList(WCMessage wcMessage) {
        sendMessage(new WCMessage(wcMessage.getMessageId(), new Gson().toJson(getAnimeList(animeListFile))));
    }

    public void setSetup(WCMessage wcMessage) {
        try {
            FileWriter fileWriter = new FileWriter(propertiesFile + "setup.json");
            WCMSetup setup = new Gson().fromJson(wcMessage.getMessageBody(), WCMSetup.class);
            new Gson().toJson(setup, fileWriter);
            fileWriter.close();
            sendMessage(new WCMessage("getSetup", new Gson().toJson(getSetup())));
        } catch (IOException e) {
            logger.error("Couldn't find the file in given path", e);
        }
    }

    public void getSetup(WCMessage wcMessage) {
        sendMessage(new WCMessage("setSetup", new Gson().toJson(getSetup())));
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
            sendMessage(wcMessage.getMessageId());
        }
    }

    public void updateAnime(WCMessage wcMessage) {
        try {
            AnimeList animeList = getAnimeList(animeListFile);
            // put file reader after the file was read, file reader will delete it's content
            FileWriter fileWriter = new FileWriter(animeListFile);
            // edit existing anime in the list
            WCMAnime wcmAnime = new Gson().fromJson(wcMessage.getMessageBody(), WCMAnime.class);
            animeList.updateAnime(wcmAnime.getId(), wcmAnime);
            logger.debug("Update anime entry with id: {}", wcmAnime.getId());
            // save content of the list into file
            new Gson().toJson(animeList, fileWriter);
            fileWriter.close();
            logger.debug("Saving file: success");
            sendMessage(new WCMessage("animeList", new Gson().toJson(animeList)));
        } catch (IOException e) {
            logger.error("File reader: ", e);
        }
    }

    public void removeAnimeFromList(WCMessage wcMessage) {

    }
}
