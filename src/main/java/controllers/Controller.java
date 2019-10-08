package controllers;

import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import ealvatag.audio.AudioFile;
import ealvatag.audio.AudioFileIO;
import ealvatag.audio.exceptions.CannotReadException;
import ealvatag.audio.exceptions.CannotWriteException;
import ealvatag.audio.exceptions.InvalidAudioFrameException;
import ealvatag.tag.FieldKey;
import ealvatag.tag.Tag;
import ealvatag.tag.TagException;
import ealvatag.tag.images.Artwork;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Controller {

    private final String home = System.getProperty("user.home");
    private final String[] filters = {"*.mp3", "*.m4a"};
    //    private final String[] filters = {"*.mp3", "*.m4a"};
    private LinkedList<File> files;
    private List<Song> songDetails = new LinkedList<>();
    private Song currentSong;
    private Iterator<Song> iterator;
    private boolean playing = false;
    private boolean playlistShown = false;
    private Media media;// = new Media(currentSong.toURI().toString())
    private MediaPlayer myPlayer; // = new MediaPlayer(media)



    @FXML
    private AnchorPane mainPane;
    @FXML
    private AnchorPane scanPane;
    @FXML
    private AnchorPane musicPane;
    @FXML
    private Label titleLabel;
    @FXML
    private Label artistLabel;
    @FXML
    private Label songsAdded;
    @FXML
    private ProgressIndicator scanProgress;
    @FXML
    public AnchorPane playlistPane;
    @FXML
    private JFXTreeTableView<Song> playlist;


    @FXML
//    Removed throws InterruptedException from scanMusic()
    void scanMusic() {
        fadeInAnimation(mainPane, 3.0, -600);
        /*fadeInAnimation(2.0, scanPane);
        mainPane.visibleProperty().setValue(false);*/
        opAnimation(scanPane);
        scanPane.visibleProperty().setValue(true);

        FindSong findSong = new FindSong();
        Task<Void> findSongs = findSong.findAllSongs();

        findSongs.setOnSucceeded(event -> {
            System.out.println("It succeeded");
            try {
                Task<Void> switchPanel = findSong.switchPane();
                new Thread(switchPanel).start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

//        findSongs.setOnFailed(event -> System.out.println("Finding songs failed"));

        new Thread(findSongs).start();
    }

    @FXML
    void playSong() {
        System.out.println(media.getSource());
/*
        String name = new File("/home/havoc/Music/02 Birthday Party.mp3").toURI().toString();
        System.out.println(name);
        File file = new File("/home/havoc/Music/bb.mp3");
        String path = file.getAbsolutePath();
        String path = file.getAbsolutePath().replace(" ", "%20");
*/
        if (playing) {
            myPlayer.pause();
            playing = false;
        } else {
            myPlayer.play();
            playing = true;
        }
    }

    @FXML
    void nextSong(ActionEvent event) {
        /*
         *For Linear Playback
          int nextSongPos = songDetails.indexOf(currentSong) + 1;
        if (songDetails.get(nextSongPos) != null) {
            System.out.println(iterator.next().getLocalLocation());
            currentSong = songDetails.get(nextSongPos);
            myPlayer.stop();
            play(currentSong);
            System.out.println(media.getSource());
            myPlayer.play();
        }*/
        // * For Random Playback
        myPlayer.stop();
        play();
        System.out.println(media.getSource());
        myPlayer.play();
        playing = true;
    }

    @FXML
    void prevSong(ActionEvent event) {
        int prevSongPos = songDetails.indexOf(currentSong) - 1;
        try {
            currentSong = songDetails.get(prevSongPos);
            myPlayer.stop();
            play(currentSong);
            System.out.println(media.getSource());
            myPlayer.play();
            playing = true;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            media = new Media(currentSong.getLocalLocation().toURI().toString());
            myPlayer = new MediaPlayer(media);
            System.out.println(media.getSource());
            play(currentSong);
            myPlayer.play();
            playing = true;
        }
        /*
         *Same With LBYL
         if (songDetails.get(prevSongPos) != null) {
            currentSong = songDetails.get(prevSongPos);
            myPlayer.stop();
            play(currentSong);
            System.out.println(media.getSource());
            myPlayer.play();
        } else {
            media = new Media(currentSong.getLocalLocation().toURI().toString());
            myPlayer = new MediaPlayer(media);
            System.out.println(media.getSource());
            playSong();
        }*/
    }


    @FXML
    void showList(MouseEvent event) {
        if (playlistShown) {
            fadeInAnimation(playlistPane, 1, -600);
            playlistShown = false;
        }
        fadeInAnimation(musicPane, 1,-600);
        opAnimation(playlistPane);
        playlistPane.visibleProperty().setValue(true);

        /*Create Table Columns*/
        JFXTreeTableColumn<Song, String> songTitle = new JFXTreeTableColumn<>("Title");
        songTitle.setPrefWidth(300);
        songTitle.setCellValueFactory(param -> param.getValue().getValue().getTitle());
        JFXTreeTableColumn<Song, String> songArtist = new JFXTreeTableColumn<>("Artist");
        songArtist.setPrefWidth(200);
        songArtist.setCellValueFactory(param -> param.getValue().getValue().getArtist());
        JFXTreeTableColumn<Song, String> songDuration = new JFXTreeTableColumn<>("Duration");
//        songDuration.setPrefWidth(80);
        songDuration.setCellValueFactory(param -> param.getValue().getValue().getDuration());

        /*Populate Cable*/
        ObservableList<Song> songTableData = FXCollections.observableArrayList();
        System.out.println(songDetails.size());
        songTableData.addAll(songDetails);
        final TreeItem<Song> root =new RecursiveTreeItem<>(songTableData, RecursiveTreeObject::getChildren);
        playlist.getColumns().setAll(songTitle, songArtist, songDuration);
        playlist.setRoot(root);
        playlist.setShowRoot(false);
    }

    @FXML
    void goBackToPlay(ActionEvent event) {
        if (!playlistShown) {
            fadeInAnimation(playlistPane, 1, 600);
            playlistShown = true;
        }
        fadeInAnimation(musicPane,  1,600);
        opAnimation(musicPane);
        musicPane.visibleProperty().setValue(true);
    }

    @FXML
    void handleSongItemClicked(){
        Song clickedSong = playlist.getSelectionModel().getSelectedItem().getValue();
        System.out.println(clickedSong);
        myPlayer.stop();
        play(clickedSong);
        myPlayer.play();
        playing = true;
    }


    /*ANIMATIONS*/
    private void fadeInAnimation(Node node, double duration, double byX) {
        TranslateTransition transition = new TranslateTransition(Duration.seconds(duration), node);
        transition.setByX(byX);
        transition.play();
    }

    private void opAnimation(Node node) {
        double OPACITY_ANIMATION_DURATION = 3.0;
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(OPACITY_ANIMATION_DURATION), node);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }


    /*CLASS CONTROLLER FUNCTIONS*/
    private void play(Song song) {
        currentSong = song;
        String tagTitle = "Title";
        String tagArtist = "Artist";

        Artwork artwork;
        try {
            AudioFile audioFile = AudioFileIO.read(song.getLocalLocation());
            if (audioFile.getTag().isPresent()) {
                if (audioFile.getTag().get().getFirstArtwork().isPresent()){
                    artwork = audioFile.getTag().get().getFirstArtwork().get();
                }
            }
            Tag tag = audioFile.getTagOrSetNewDefault();
            tagTitle = tag.getFirst(FieldKey.TITLE);
            tagArtist = tag.getFirst(FieldKey.ARTIST);
        } catch (CannotReadException | TagException | InvalidAudioFrameException | IOException | CannotWriteException e) {
            e.printStackTrace();
        }
        playlist.getSelectionModel().select(new TreeItem<>(song));
        titleLabel.setText(tagTitle);
        artistLabel.setText(tagArtist);
        media = new Media(song.getLocalLocation().toURI().toString());
        myPlayer = new MediaPlayer(media);
        myPlayer.play();
        playing = true;
        myPlayer.setOnEndOfMedia(() -> {
            myPlayer.stop();
            playing = false;
            /*
            *For Linear Playback
            if (iterator.hasNext()) {
                System.out.println(iterator.next().getLocalLocation());
                currentSong = iterator.next();
                play(songDetails.get(getRandomSong()));
                System.out.println(getRandomSong());
                myPlayer.play();
            }*/
//            *For Random Playback
            play(songDetails.get(getRandomSong()));
            myPlayer.play();
            playing = true;
        });
    }

    private void play() {
        Song song = songDetails.get(getRandomSong());
        play(song);
    }

    private int getRandomSong(){
        int max = songDetails.size();
        int no = (int) (((max - 1) * Math.random()));
        System.out.println(no);
        return no;
    }

    @FXML
    public void initialize() {
//        allSongs.getItems().setAll(songDetails);
//        allSongs.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

    }


    /*CLASS CONTROLLER SUBCLASS FOR FINDING SONGS IN USER DRIVE AND POPULATE LIST*/
    private class FindSong{
        private Task<Void> findAllSongs() {
            return new Task<>() {
                @Override
//                Removed throws Exception from call()
                protected Void call() throws IOException, CannotWriteException, TagException, InvalidAudioFrameException, CannotReadException {
                    files = (LinkedList<File>) getAllFilesThatMatchFilenameExtension(home, filters);
                    System.out.println(files.size());
                    songDetails = new LinkedList<>();
                    for (File file :
                            files) {
//                        System.out.println(file);
                        /*TODO HOW TO CATCH EXCEPTIONS THROWN FROM METHOD SIGNATURE*/
                        songDetails.add(new Song(file));
                    }
//                    System.out.println(files.get(2585));
                    iterator = songDetails.iterator();
                    String rootPath = System.getProperty("file.separator");
                    System.out.println("rootPath is " + rootPath);
                    System.out.println("total size is "+files.size());
                    return null;
                }


                @Override
                protected void succeeded() {
                    songsAdded.setText(String.valueOf(files.size()));
                    super.succeeded();
                }
            };

        }

        private Collection<File> getAllFilesThatMatchFilenameExtension(String directoryName, String[] extensions)
        {
            File directory = new File(directoryName);
            return FileUtils.listFiles(directory, new WildcardFileFilter(extensions), TrueFileFilter.INSTANCE);
        }

        /*SWITCH TO MUSICPANE WHEN SCAN IS DONE*/
        private Task<Void> switchPane() throws InterruptedException{
            return new Task<>() {
                @Override
                protected Void call() throws Exception {
                    Thread.sleep(2000);
                    fadeInAnimation(scanPane, 1, -600);
                    opAnimation(musicPane);
                    musicPane.visibleProperty().setValue(true);
                    scanProgress.setVisible(false);
                    Platform.runLater(Controller.this::play);
                    return null;
                }
            };
        }

    }

}


