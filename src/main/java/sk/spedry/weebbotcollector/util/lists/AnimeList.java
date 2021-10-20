package sk.spedry.weebbotcollector.util.lists;

import sk.spedry.weebbotcollector.util.WCMAnime;

import java.util.ArrayList;
import java.util.List;

public class AnimeList {
    private final List<WCMAnime> list;

    public AnimeList() {
        this.list = new ArrayList<WCMAnime>();
    }

    public List<WCMAnime> getAnimeList() {
        return list;
    }

    public void addAnime(WCMAnime anime) {
        this.list.add(anime);
    }

    public int getSize() {
        return this.list.size();
    }

    public void updateAnime(int id, WCMAnime anime) {
        list.set(id, anime);
    }

    public void removeAnime(WCMAnime anime) {
        list.remove(anime);
    }

    public void removeAnime(int id) {
        list.remove(id);
    }
}
