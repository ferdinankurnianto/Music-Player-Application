package com.example.musicplayer;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MusicViewModel extends ViewModel {
    private MusicRepository musicRepository = MusicRepository.getInstance();

    public String getId() {
        return musicRepository.getId();
    }

    public void setId(String id) {
        musicRepository.setId(id);
    }

    public MutableLiveData<String> getTitle() {
        return musicRepository.getTitle();
    }

    public void setTitle(String title) {
        musicRepository.setTitle(title);
    }

    public MutableLiveData<String> getArtist() {
        return musicRepository.getArtist();
    }

    public void setArtist(String artist) {
        musicRepository.setArtist(artist);
    }

    public MutableLiveData<String> getCategory() {
        return musicRepository.getCategory();
    }

    public void setCategory(String category) {
        musicRepository.setCategory(category);
    }

    public MutableLiveData<String> getSinger() {
        return musicRepository.getSinger();
    }

    public void setSinger(String singer) {
        musicRepository.setSinger(singer);
    }

    public MutableLiveData<String> getYear() {
        return musicRepository.getYear();
    }

    public void setYear(String year) {
        musicRepository.setYear(year);
    }

    public MutableLiveData<String> getLyric() {
        return musicRepository.getLyric();
    }

    public void setLyric(String lyric) {
        musicRepository.setLyric(lyric);
    }

    public MutableLiveData<Bitmap> getImage() {
        return musicRepository.getImage();
    }

    public void setImage(Bitmap image) {
        musicRepository.setImage(image);
    }

    public MutableLiveData<Integer> getProgress() {
        return musicRepository.getProgress();
    }

    public MutableLiveData<Integer> getDuration() {
        return musicRepository.getDuration();
    }

    public void setDuration(Integer duration) {
        musicRepository.setDuration(duration);
    }

    public MutableLiveData<Boolean> getPlayStatus() {
        return musicRepository.getPlayStatus();
    }

    public void setPlayStatus(Boolean status) {
        musicRepository.setPlayStatus(status);
    }

    public LiveData<Boolean> getLoading() {
        return musicRepository.getLoading();
    }
}
