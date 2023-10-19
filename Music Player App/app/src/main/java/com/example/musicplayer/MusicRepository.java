package com.example.musicplayer;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;

public class MusicRepository {
    private static MusicRepository instance;
    private MutableLiveData<String> title, artist, category, singer, year, lyric;
    private MutableLiveData<Bitmap> image;
    private MutableLiveData<Integer> progress, duration;
    private MutableLiveData<Boolean> playStatus, loading;
    private String id;

    public static MusicRepository getInstance() {
        if (instance == null) {
            instance = new MusicRepository();
        }
        return instance;
    }

    public String getId() {
        if (id==null)
            id = "-1";
        return id;
    }

    public void setId(String _id) {
        id = _id;
    }

    public MutableLiveData<String> getTitle() {
        if (title == null)
            title = new MutableLiveData<String>();
        return title;
    }

    public void setTitle(String name) {
        title.setValue(name);
    }

    public MutableLiveData<String> getArtist() {
        if (artist == null)
            artist = new MutableLiveData<String>();
        return artist;
    }

    public void setArtist(String artistTxt) {
        artist.setValue(artistTxt);
    }

    public MutableLiveData<String> getCategory() {
        if (category == null)
            category = new MutableLiveData<String>();
        return category;
    }

    public void setCategory(String categoryTxt) {
        category.setValue(categoryTxt);
    }

    public MutableLiveData<String> getSinger() {
        if (singer == null)
            singer = new MutableLiveData<String>();
        return singer;
    }

    public void setSinger(String singerTxt) {
        singer.setValue(singerTxt);
    }

    public MutableLiveData<String> getYear() {
        if (year == null)
            year = new MutableLiveData<String>();
        return year;
    }

    public void setYear(String yearTxt) {
        year.setValue(yearTxt);
    }

    public MutableLiveData<String> getLyric() {
        if (lyric == null)
            lyric = new MutableLiveData<String>();
        return lyric;
    }

    public void setLyric(String lyricTxt) {
        lyric.setValue(lyricTxt);
    }

    public MutableLiveData<Bitmap> getImage() {
        if (image == null)
            image = new MutableLiveData<>();
        return image;
    }

    public void setImage(Bitmap _image) {
        image.setValue(_image);
    }

    public MutableLiveData<Integer> getProgress() {
        if (progress == null)
            progress = new MutableLiveData<>();
        return progress;
    }

    public void setProgress(Integer _progress) {
        progress.setValue(_progress);
    }

    public MutableLiveData<Integer> getDuration() {
        if (duration == null)
            duration = new MutableLiveData<>();
        return duration;
    }

    public void setDuration(Integer _duration) {
        duration.setValue(_duration);
    }

    public MutableLiveData<Boolean> getPlayStatus() {
        if (playStatus == null) {
            playStatus = new MutableLiveData<>();
            playStatus.setValue(false);
        }
        return playStatus;
    }

    public void setPlayStatus(Boolean _playStatus) {
        playStatus.setValue(_playStatus);
    }

    public MutableLiveData<Boolean> getLoading() {
        if (loading == null) {
            loading = new MutableLiveData<>();
            loading.postValue(false);
        }
        return loading;
    }

    public void setLoading(Boolean _loading) {
        loading.setValue(_loading);
    }
    public void postLoading(Boolean _loading) {
        loading.postValue(_loading);
    }
}
