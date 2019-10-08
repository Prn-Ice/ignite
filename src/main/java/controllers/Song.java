package controllers;


import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import ealvatag.audio.AudioFile;
import ealvatag.audio.AudioFileIO;
import ealvatag.audio.AudioHeader;
import ealvatag.audio.exceptions.CannotReadException;
import ealvatag.audio.exceptions.CannotWriteException;
import ealvatag.audio.exceptions.InvalidAudioFrameException;
import ealvatag.tag.FieldKey;
import ealvatag.tag.Tag;
import ealvatag.tag.TagException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Song extends RecursiveTreeObject<Song> {
    private StringProperty title;
    private StringProperty artist;
    private StringProperty duration;
    private File localLocation;
    private AudioFile song;

    Song(File localLocation) throws TagException, CannotReadException, InvalidAudioFrameException, IOException, CannotWriteException {
        this.localLocation = localLocation;
        this.song = AudioFileIO.read(localLocation);
        this.title = new SimpleStringProperty(getSongDetails(this.song)[0]);
        this.artist = new SimpleStringProperty(getSongDetails(this.song)[1]);
        this.duration = new SimpleStringProperty(getSongDetails(this.song)[2]);
    }

    public StringProperty getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = new SimpleStringProperty(title);
    }

    public StringProperty getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = new SimpleStringProperty(artist);
    }

    public StringProperty getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = new SimpleStringProperty(duration);
    }

    public File getLocalLocation() {
        return localLocation;
    }

    public void setLocalLocation(File localLocation) {
        this.localLocation = localLocation;
    }

    public AudioFile getSong() {
        return song;
    }

    public void setSong(AudioFile song) {
        this.song = song;
    }

    private String[] getSongDetails(AudioFile song) throws CannotWriteException {
        String title, artist, duration;
        Tag song_tag = song.getTagOrSetNewDefault();
        title = song_tag.getFirst(FieldKey.TITLE);
        artist = song_tag.getFirst(FieldKey.ARTIST);

        AudioHeader song_head = song.getAudioHeader();
        double preciseDuration = song_head.getDuration(TimeUnit.SECONDS, true);
        int minutes = (int) (preciseDuration/60);
        int seconds = (int) (preciseDuration - minutes*60);
        duration = String.format("%d:%2d", minutes, seconds);
        return new String[] {title, artist, duration};
    }

    @Override
    public String toString() {
        return this.title+"\t"+this.artist+"\t"+this.duration;
    }
}
