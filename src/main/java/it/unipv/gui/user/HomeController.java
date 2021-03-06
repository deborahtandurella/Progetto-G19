package it.unipv.gui.user;

import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import it.unipv.conversion.CSVToDraggableSeats;
import it.unipv.conversion.CSVToMovieList;
import it.unipv.conversion.CSVToMovieScheduleList;
import it.unipv.gui.common.*;
import it.unipv.gui.login.LoginController;
import it.unipv.gui.login.User;
import it.unipv.gui.login.UserInfo;
import it.unipv.gui.manager.ManagerHomeController;
import it.unipv.gui.user.areariservata.AreaRiservataHomeController;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.ApplicationUtils;
import it.unipv.utils.CloseableUtils;
import it.unipv.utils.DataReferences;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class HomeController implements Initializable {
    
    @FXML private Rectangle rectangleMenu, rectangleGenere, rectangle2D3D;
    @FXML private AnchorPane menuWindow, genereWindow, anchorInfo, homePane, singleFilmPane, welcomePanel, welcomeFooter, logoutPane;
    private GridPane filmGrid = new GridPane();
    @FXML private GridPane filmGridFiltered = new GridPane();
    @FXML private ScrollPane filmScroll, filmScrollFiltered, hallPanel;
    @FXML private Label labelIVA, labelCellulari, labelCosti, infoUtili, genreLabel, logLabel, nonRegistratoQuestionLabel, registerButton, areaRiservataButton, welcomeLabel;
    @FXML private Line lineGenere;
    @FXML private Label goBackToHomeButton;

    private ManagerHomeController mhc;
    private AreaRiservataHomeController arhc;
    private MoviePrenotationController mpc;
    private final Stage stageRegistrazione = new Stage();
    private final Stage stageLogin = new Stage();
    private Stage reservedAreaStage, managerAreaStage, prenotationStage;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static int rowCount = 0;
    private static int columnCount = 0;
    private static int columnCountMax = 0;
    private List<Movie> film = new ArrayList<>();
    private User loggedUser;

    /* ************************************************************* METODO PRINCIPALE **************************************************************/
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if(checkIfThereIsAlreadyUserSaved()) {
            loggedUser = UserInfo.getUserInfo();
            setupLoggedUser();
        } else {
            logoutPane.setVisible(false);
            areaRiservataButton.setVisible(false);
        }
        dtf.format(LocalDateTime.now());
        rectangle2D3D.setVisible(false);
        anchorInfo.setVisible(false);
        menuWindow.setVisible(false);
        lineGenere.setVisible(false);
        genereWindow.setVisible(false);
        singleFilmPane.setVisible(false);
        homePane.setVisible(false);
        hallPanel.setVisible(false);
        welcomePanel.setVisible(true);
    }
    /* **********************************************************************************************************************************************/


    /* ************************************************************* METODI RIGUARDANTI IL MENU **************************************************************/
    public void homeClick(){
        hallPanel.setVisible(false);
        welcomePanel.setVisible(false);
        anchorInfo.setVisible(false);
        singleFilmPane.setVisible(false);
        rectangle2D3D.setVisible(false);
        animationMenu();
        homePane.setVisible(true);
        filmScroll.setVisible(true);
        filmScrollFiltered.setVisible(false);
        if(film.isEmpty()){ initMovieGrid(); }
        genreLabel.setText("genere");
        type = null;
    }

    public void listaSaleClick(){
        anchorInfo.setVisible(false);
        welcomePanel.setVisible(false);
        homePane.setVisible(false);
        singleFilmPane.setVisible(false);
        hallPanel.setVisible(true);
        initHallGrid();
        animationMenu();
    }

    public void infoClick() {
        hallPanel.setVisible(false);
        homePane.setVisible(false);
        singleFilmPane.setVisible(false);
        filmScroll.setVisible(false);
        filmScrollFiltered.setVisible(false);
        welcomePanel.setVisible(false);
        rectangle2D3D.setVisible(false);
        animationMenu();
        Stage stage = (Stage) homePane.getScene().getWindow();
        infoUtili.setLayoutX(stage.getWidth()/2-infoUtili.getWidth()/2);
        labelCellulari.setLayoutX(stage.getWidth()/2-labelCellulari.getWidth()/2);
        labelIVA.setLayoutX(stage.getWidth()/2-labelIVA.getWidth()/2);
        labelCosti.setLayoutX(stage.getWidth()/2-labelCosti.getWidth()/2);
        anchorInfo.setVisible(true);
    }

    @FXML
    public void areaRiservataClick() {
        openReservedArea();
        animationMenu();
    }

    public void animationMenu(){
        KeyValue widthValueForward = new KeyValue(rectangleMenu.widthProperty(), rectangleMenu.getWidth() +81);
        KeyValue widthValueBackwards = new KeyValue(rectangleMenu.widthProperty(), rectangleMenu.getWidth() -81);
        KeyValue heightValueForward = new KeyValue(rectangleMenu.heightProperty(), rectangleMenu.getHeight()+244);
        KeyValue heightValueBackwards = new KeyValue(rectangleMenu.heightProperty(), rectangleMenu.getHeight()-244);
        KeyFrame forwardW = new KeyFrame(javafx.util.Duration.seconds(0.3), widthValueForward);
        KeyFrame backwardW = new KeyFrame(javafx.util.Duration.seconds(0.15), widthValueBackwards);
        KeyFrame forwardH = new KeyFrame(javafx.util.Duration.seconds(0.3), heightValueForward);
        KeyFrame backwardH = new KeyFrame(javafx.util.Duration.seconds(0.15), heightValueBackwards);
        Timeline timelineForwardH = new Timeline(forwardH);
        Timeline timelineBackwardH = new Timeline(backwardH);
        Timeline timelineForwardW = new Timeline(forwardW);
        Timeline timelineBackwardW = new Timeline(backwardW);
        FadeTransition fadeIn = new FadeTransition(javafx.util.Duration.seconds(0.4), menuWindow);
        FadeTransition fadeOut = new FadeTransition(javafx.util.Duration.seconds(0.1), menuWindow);

        if(!menuWindow.isVisible()){
            menuWindow.setOpacity(0);
            menuWindow.setVisible(true);
            timelineForwardW.play();
            timelineForwardH.play();

            fadeIn.setDelay(javafx.util.Duration.seconds(0.2));
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        } else {
            if(menuWindow.isVisible()){
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0);
                fadeOut.play();
                menuWindow.setVisible(false);

                timelineBackwardH.play();
                timelineBackwardW.play();
            }
        }
    }
    /* *******************************************************************************************************************************************************/


    /* ************************************************************* METODI RIGUARDANTI LA HOME **************************************************************/
    private void initMovieGrid(){
        filmGrid.getChildren().clear();
        filmGridFiltered.getChildren().clear();
        film = CSVToMovieList.getMovieListFromCSV(DataReferences.MOVIEFILEPATH);
        Collections.sort(film);

        filmGrid.setHgap(80);
        filmGrid.setVgap(80);

        filmGridFiltered.setHgap(80);
        filmGridFiltered.setVgap(80);

        if (lineGenere.getScene().getWindow().getWidth() > 1360) {
            columnCountMax = 5;
            filmGrid.setHgap(150);
            filmGridFiltered.setHgap(150);
        } else {
            columnCountMax = 3;
        }

        for (Movie movie : film) {
            if(movie.getStatus().equals(MovieStatusTYPE.AVAILABLE)) {
                addMovie(movie, filmGrid, filmScroll);
            }
        }

        initRowAndColumnCount();
    }

    private void initRowAndColumnCount() {
        rowCount = 0;
        columnCount = 0;
    }

    private void addMovie(Movie movie, GridPane grid, ScrollPane scroll){
        FileInputStream fis = null;
        ImageView posterPreview;

        try {
            fis = new FileInputStream(movie.getLocandinaPath());
            posterPreview = new ImageView(new Image(fis, 1000, 0, true, true));
        } catch(FileNotFoundException ex) {
            throw new ApplicationException(ex);
        } finally {
            CloseableUtils.close(fis);
        }

        posterPreview.setPreserveRatio(true);
        posterPreview.setFitWidth(200);
        CloseableUtils.close(fis);

        AnchorPane anchor = new AnchorPane();


        if (columnCount == columnCountMax) {
            columnCount = 0;
            rowCount++;
        }

        grid.add(anchor, columnCount, rowCount);
        columnCount++;

        scroll.setContent(grid);
        GridPane.setMargin(anchor, new Insets(15, 0, 5, 15));

        anchor.getChildren().addAll(posterPreview);
        posterPreview.setLayoutX(30);
        if (rowCount == 0) {
            posterPreview.setLayoutY(20);
        }

        posterPreview.setOnMouseClicked(e -> populateSingleFilmPane(movie));

        GUIUtils.setScaleTransitionOnControl(posterPreview);
    }

    private void populateSingleFilmPane(Movie movie) {
        homePane.setVisible(false);
        singleFilmPane.getChildren().clear();

        Label title = new Label();
        Label movieTitle = new Label();

        Label genre = new Label();
        Label movieGenre = new Label();

        Label direction = new Label();
        Label movieDirection = new Label();

        Label cast = new Label();
        TextArea movieCast = new TextArea();
        movieCast.getStylesheets().add("css/TextAreaStyle.css");
        movieCast.sceneProperty().addListener((observableNewScene, oldScene, newScene) -> {
            if (newScene != null) {
                movieCast.applyCss();
                Node text = movieCast.lookup(".text");

                movieCast.prefHeightProperty().bind(Bindings.createDoubleBinding(() -> movieCast.getFont().getSize() + text.getBoundsInLocal().getHeight(), text.boundsInLocalProperty()));

                text.boundsInLocalProperty().addListener((observableBoundsAfter, boundsBefore, boundsAfter) -> Platform.runLater(movieCast::requestLayout)
                );
            }
        });
        movieCast.getStyleClass().add("movieCastTA");
        movieCast.setWrapText(true);

        Label time = new Label();
        Label movieTime = new Label();

        Label year = new Label();
        Label movieYear = new Label();

        Label programmationsLabel = new Label();

        Font infoFont = new Font("Bebas Neue Regular", 24);

        ImageView poster;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(movie.getLocandinaPath());
            poster = new ImageView(new Image(fis, 1000, 0, true, true));
            poster.setPreserveRatio(true);
        } catch (FileNotFoundException exce){
            throw new ApplicationException(exce);
        } finally {
            CloseableUtils.close(fis);
        }

        poster.setFitWidth(350);

        title.setText("TITOLO: ");
        title.setTextFill(Color.valueOf("db8f00"));
        title.setLayoutX(poster.getLayoutX() + poster.getFitWidth() + 20);
        title.setLayoutY(poster.getLayoutY() + 25);
        title.setFont(infoFont);

        movieTitle.setText(movie.getTitolo());
        movieTitle.setTextFill(Color.WHITE);
        movieTitle.setLayoutX(title.getLayoutX() + 110);
        movieTitle.setLayoutY(title.getLayoutY());
        movieTitle.setFont(infoFont);

        genre.setText("GENERE: ");
        genre.setTextFill(Color.valueOf("db8f00"));
        genre.setLayoutX(title.getLayoutX());
        genre.setLayoutY(title.getLayoutY() + 50);
        genre.setFont(infoFont);

        movieGenre.setText(movie.getGenere());
        movieGenre.setTextFill(Color.WHITE);
        movieGenre.setLayoutX(movieTitle.getLayoutX());
        movieGenre.setLayoutY(genre.getLayoutY());
        movieGenre.setFont(infoFont);

        direction.setText("REGIA: ");
        direction.setTextFill(Color.valueOf("db8f00"));
        direction.setLayoutX(title.getLayoutX());
        direction.setLayoutY(genre.getLayoutY() + 50);
        direction.setFont(infoFont);

        movieDirection.setText(movie.getRegia());
        movieDirection.setTextFill(Color.WHITE);
        movieDirection.setLayoutX(movieTitle.getLayoutX());
        movieDirection.setLayoutY(direction.getLayoutY());
        movieDirection.setFont(infoFont);

        cast.setText("CAST: ");
        cast.setTextFill(Color.valueOf("db8f00"));
        cast.setLayoutX(title.getLayoutX());
        cast.setLayoutY(direction.getLayoutY() + 50);
        cast.setFont(infoFont);

        movieCast.setText(StringUtils.abbreviate(movie.getCast(),170));
        if(movie.getCast().length()>170) {
            movieCast.setTooltip(new Tooltip(getFormattedTooltipText(movie, ',')));
        }

        movieCast.setEditable(false);
        movieCast.setLayoutX(movieDirection.getLayoutX() -15);
        movieCast.setLayoutY(cast.getLayoutY()-8);
        movieCast.setFont(infoFont);

        singleFilmPane.getChildren().addAll(cast, movieCast);
        singleFilmPane.applyCss();
        singleFilmPane.layout();

        time.setText("DURATA: ");
        time.setTextFill(Color.valueOf("db8f00"));
        time.setLayoutX(title.getLayoutX());
        time.setLayoutY(movieCast.getLayoutY() + movieCast.prefHeightProperty().getValue());
        time.setFont(infoFont);

        movieTime.setText(movie.getDurata() + " minuti");
        movieTime.setTextFill(Color.WHITE);
        movieTime.setLayoutX(movieTitle.getLayoutX());
        movieTime.setLayoutY(time.getLayoutY());
        movieTime.setFont(infoFont);

        year.setText("ANNO: ");
        year.setTextFill(Color.valueOf("db8f00"));
        year.setLayoutX(title.getLayoutX());
        year.setLayoutY(time.getLayoutY() + 50);
        year.setFont(infoFont);

        movieYear.setText(movie.getAnno());
        movieYear.setTextFill(Color.WHITE);
        movieYear.setLayoutX(movieTitle.getLayoutX());
        movieYear.setLayoutY(year.getLayoutY());
        movieYear.setFont(infoFont);

        programmationsLabel.setText("PROGRAMMATO PER: ");
        programmationsLabel.setTextFill(Color.valueOf("db8f00"));
        programmationsLabel.setLayoutX(title.getLayoutX());
        programmationsLabel.setLayoutY(year.getLayoutY()+60);
        programmationsLabel.setFont(infoFont);

        double x = title.getLayoutX()+20;
        double y = programmationsLabel.getLayoutY()+40;
        int count = 0;
        int i = 0;

        //Metto massimo 10 date perché se no si sovrappongono con la trama.. tanto ogni giorno toglie una data vecchia e ne aggiunge una nuova
        List<MovieSchedule> schedules = getProgrammationListFromMovie(movie);
        for(MovieSchedule ms : schedules) {
            if(i<10) {
                if(!ApplicationUtils.checkIfDateIsPassed(ms.getDate())) {
                    Label scheduleLabel = new Label();
                    scheduleLabel.setText("  " + ms.getDate() + "  ");
                    scheduleLabel.setTextFill(Color.WHITE);
                    if(count>=5) {
                        y+=50;
                        x = title.getLayoutX()+20;
                        count = 0;
                    }
                    scheduleLabel.setLayoutY(y);
                    scheduleLabel.setLayoutX(x);
                    scheduleLabel.setFont(infoFont);
                    scheduleLabel.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
                    scheduleLabel.setOnMouseEntered(event -> {
                        scheduleLabel.setBorder(new Border(new BorderStroke(Color.YELLOW, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
                        scheduleLabel.setCursor(Cursor.HAND);
                    });
                    scheduleLabel.setOnMouseExited(event -> {
                        scheduleLabel.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
                        scheduleLabel.setCursor(Cursor.DEFAULT);
                    });

                    scheduleLabel.setOnMouseClicked(event -> {
                        if(loggedUser==null) {
                            GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore", "Devi aver effettuato il login per poter accedere alla prenotazione!");
                        } else if (!isHimANormalUser(loggedUser)){
                            GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore", "Non puoi effettuare una prenotazione con questo account!");
                        } else {
                            openPrenotationStage(movie, scheduleLabel);
                        }
                    });
                    x += 190;
                    count++;
                    singleFilmPane.getChildren().add(scheduleLabel);
                }
            }
            i++;
        }

        Label synopsis = new Label("Trama:");
        synopsis.setTextFill(Color.valueOf("db8f00"));
        synopsis.setLayoutX(poster.getLayoutX() + 15);
        synopsis.setLayoutY(poster.getLayoutY()+515);
        synopsis.setFont(infoFont);

        TextArea movieSynopsis = new TextArea();
        movieSynopsis.setText(movie.getTrama());
        movieSynopsis.getStylesheets().add("css/TextAreaStyle.css");
        movieSynopsis.sceneProperty().addListener((observableNewScene, oldScene, newScene) -> {
            if (newScene != null) {
                movieSynopsis.applyCss();
                Node text = movieSynopsis.lookup(".text");

                movieSynopsis.prefHeightProperty().bind(Bindings.createDoubleBinding(() -> movieSynopsis.getFont().getSize() + text.getBoundsInLocal().getHeight(), text.boundsInLocalProperty()));

                text.boundsInLocalProperty().addListener((observableBoundsAfter, boundsBefore, boundsAfter) -> Platform.runLater(movieSynopsis::requestLayout)
                );
            }
        });
        movieSynopsis.getStyleClass().add("movieSynopsisTA");
        movieSynopsis.setFont(infoFont);
        movieSynopsis.setWrapText(true);
        movieSynopsis.setEditable(false);
        movieSynopsis.setLayoutX(synopsis.getLayoutX()-15);
        movieSynopsis.setLayoutY(synopsis.getLayoutY()+30);
        movieSynopsis.setPrefWidth(1400);

        singleFilmPane.getChildren().addAll( title, movieTitle
                                           , genre, movieGenre
                                           , direction, movieDirection
                                           , time, movieTime
                                           , year, movieYear
                                           , programmationsLabel
                                           , poster
                                           , goBackToHomeButton
                                           , synopsis, movieSynopsis);

        GUIUtils.setScaleTransitionOnControl(goBackToHomeButton);
        goBackToHomeButton.getStylesheets().add("css/BebasNeue.css");
        goBackToHomeButton.setOnMouseClicked(event -> {
            singleFilmPane.setVisible(false);
            homePane.setVisible(true);
        });

        singleFilmPane.setVisible(true);
    }

    private String getFormattedTooltipText(Movie movie, char escape) {
        StringBuilder res = new StringBuilder();
        char[] temp = movie.getCast().toCharArray();
        int cont = 0;
        for(int i=0; i<movie.getCast().length();i++) {
            if(cont == 5) {
                res.append("\n").append(temp[i]);
                cont = 0;
            } else {
                res.append(temp[i]);
            }
            if(temp[i] == escape) {
                cont++;
            }
        }
        return res.toString();
    }

    private boolean isPrenotationAreaOpened = false;
    private void openPrenotationStage(Movie movie, Label scheduleLabel) {
        if(!isPrenotationAreaOpened) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/MoviePrenotation.fxml"));
                Parent p = loader.load();
                mpc = loader.getController();
                mpc.init(this, scheduleLabel.getText().trim(), movie, loggedUser);
                prenotationStage = new Stage();
                prenotationStage.setScene(new Scene(p));
                prenotationStage.setResizable(false);
                prenotationStage.setTitle("Prenotazione " + movie.getTitolo() + " " + scheduleLabel.getText().trim());
                prenotationStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
                prenotationStage.setOnCloseRequest( event -> isPrenotationAreaOpened = false);
                prenotationStage.show();
                isPrenotationAreaOpened = true;
            } catch (IOException ex) {
                throw new ApplicationException(ex);
            }
        }
    }

    private List<MovieSchedule> getProgrammationListFromMovie(Movie m) {
        String date = "";
        List<MovieSchedule> allSchedules = CSVToMovieScheduleList.getMovieScheduleListFromCSV(DataReferences.MOVIESCHEDULEFILEPATH);
        Collections.sort(allSchedules);
        List<MovieSchedule> res = new ArrayList<>();
        for(MovieSchedule ms : allSchedules) {
            if(ms.getMovieCode().equals(m.getCodice())) {
                if(!date.equals(ms.getDate())) {
                    res.add(ms);
                    date = ms.getDate();
                }
            }
        }
        return res;
    }

    public void animationGenere(){
        KeyValue heightValueForward = new KeyValue(rectangleGenere.heightProperty(), rectangleGenere.getHeight()+305);
        KeyValue heightValueBackwards = new KeyValue(rectangleGenere.heightProperty(), rectangleGenere.getHeight()-305);
        KeyFrame forwardH = new KeyFrame(javafx.util.Duration.seconds(0.3), heightValueForward);
        KeyFrame backwardH = new KeyFrame(javafx.util.Duration.seconds(0.3), heightValueBackwards);
        Timeline timelineForwardH = new Timeline(forwardH);
        Timeline timelineBackwardH = new Timeline(backwardH);
        FadeTransition fadeIn = new FadeTransition(javafx.util.Duration.seconds(0.4), genereWindow);
        FadeTransition fadeOut = new FadeTransition(javafx.util.Duration.seconds(0.1), genereWindow);

        if(!genereWindow.isVisible()){
            genereWindow.setOpacity(0);
            genereWindow.setVisible(true);
            timelineForwardH.play();

            fadeIn.setDelay(javafx.util.Duration.seconds(0.2));
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        } else {
            if(genereWindow.isVisible()){
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0);
                fadeOut.play();
                genereWindow.setVisible(false);

                timelineBackwardH.play();
            }
        }
    }


    private MovieTYPE type;

    public void hoverGenereEnter(MouseEvent event){
        Label label = (Label) event.getSource();
        if(genereWindow.isVisible()){
            lineGenere.setVisible(true);
        }

        lineGenere.setLayoutY(label.getLayoutY()+32);
        lineGenere.setStartX(-label.getWidth()/2);
        lineGenere.setEndX(label.getWidth()/2);
    }

    public void genereClicked(MouseEvent event) {
        Label l = (Label)event.getSource();
        genreLabel.setText(l.getText());

        if(type==null) {
            filterMoviesByMovieGenre(genreLabel.getText());
        } else {
            filterMoviesByMovieTYPEAndMovieGenre(type, genreLabel.getText());
        }

        animationGenere();
    }

    public void animation2D3D(MouseEvent event){
        rectangle2D3D.setVisible(true);
        TranslateTransition transition = new TranslateTransition(javafx.util.Duration.seconds(0.2), rectangle2D3D);
        Label label = (Label) event.getSource();
        transition.setToX(label.getLayoutX()-rectangle2D3D.getLayoutX()-rectangle2D3D.getWidth()/4);
        transition.play();

        switch(label.getText()) {
            case "2D":
                if(genreLabel.getText().toLowerCase().equalsIgnoreCase("genere")) {
                    filterMoviesByMovieTYPE(MovieTYPE.TWOD);
                } else {
                    filterMoviesByMovieTYPEAndMovieGenre(MovieTYPE.TWOD, genreLabel.getText());
                }
                type = MovieTYPE.TWOD;
                break;
            case "3D":
                if(genreLabel.getText().toLowerCase().equalsIgnoreCase("genere")) {
                    filterMoviesByMovieTYPE(MovieTYPE.THREED);
                } else {
                    filterMoviesByMovieTYPEAndMovieGenre(MovieTYPE.THREED, genreLabel.getText());
                }
                type = MovieTYPE.THREED;
                break;
        }
    }

    private void filterMoviesByMovieTYPE(MovieTYPE type) {
        filmGridFiltered.getChildren().clear();
        for(Movie m : film) {
            if(m.getStatus().equals(MovieStatusTYPE.AVAILABLE)) {
                if(m.getTipo().equals(type)) {
                    addMovie(m, filmGridFiltered, filmScrollFiltered);
                }
            }
        }
        initRowAndColumnCount();
        filmScroll.setVisible(false);
        filmScrollFiltered.setVisible(true);
    }

    private void filterMoviesByMovieGenre(String genere) {
        filmGridFiltered.getChildren().clear();
        for(Movie m : film) {
            if(m.getStatus().equals(MovieStatusTYPE.AVAILABLE)) {
                if(m.getGenere().toLowerCase().contains(genere.toLowerCase())) {
                    addMovie(m, filmGridFiltered, filmScrollFiltered);
                }
            }
        }
        initRowAndColumnCount();
        filmScroll.setVisible(false);
        filmScrollFiltered.setVisible(true);
    }

    private void filterMoviesByMovieTYPEAndMovieGenre(MovieTYPE type, String genere) {
        filmGridFiltered.getChildren().clear();
        for(Movie m : film) {
            if(m.getStatus().equals(MovieStatusTYPE.AVAILABLE)) {
                if(m.getGenere().toLowerCase().contains(genere.toLowerCase()) && m.getTipo().equals(type)) {
                    addMovie(m, filmGridFiltered, filmScrollFiltered);
                }
            }
        }
        initRowAndColumnCount();
        filmScroll.setVisible(false);
        filmScrollFiltered.setVisible(true);
    }
    /* ****************************************************************************************************************************************************/


    /* ************************************************************* METODI RIGUARDANTI LISTA SALE **************************************************************/
    private File[] listOfPreviews;
    private static int hallRowCount = 0;
    private static int hallColumnCount = 0;
    private static int hallColumnCountMax = 0;
    private GridPane grigliaSale = new GridPane();

    private void initListOfPreviews() {
        listOfPreviews = new File(DataReferences.PIANTINEPREVIEWSFOLDERPATH).listFiles();
    }

    private void initHallGrid() {
        initListOfPreviews();
        grigliaSale.getChildren().clear();
        if (lineGenere.getScene().getWindow().getWidth() > 1360) {
            hallColumnCountMax = 5;
        } else {
            hallColumnCountMax = 3;
        }


        for (File file : Objects.requireNonNull(listOfPreviews)) {
            createViewFromPreviews(file);
        }

        hallRowCount = 0;
        hallColumnCount = 0;
    }

    private void createViewFromPreviews(File file) {
        Font font = Font.font("system", FontWeight.NORMAL, FontPosture.REGULAR, 15);

        Label nomeSalaLabel = new Label(FilenameUtils.removeExtension(file.getName()));
        nomeSalaLabel.setFont(font);
        nomeSalaLabel.setTextFill(Color.WHITE);

        List<Seat> seatList = initDraggableSeatsList(nomeSalaLabel.getText().trim());

        Label numPostiTotaliLabel = new Label("Capienza: " + seatList.size() + " posti");
        numPostiTotaliLabel.setFont(font);
        numPostiTotaliLabel.setTextFill(Color.WHITE);

        int numPostiVIP = getSeatNumberPerType(seatList, SeatTYPE.VIP);
        int numPostiDisabili = getSeatNumberPerType(seatList, SeatTYPE.DISABILE);

        Label numPostiDisabiliLabel = new Label("Posti per disabili: " + numPostiDisabili);
        numPostiDisabiliLabel.setFont(font);
        numPostiDisabiliLabel.setTextFill(Color.WHITE);
        if (numPostiDisabili == 0) {
            numPostiDisabiliLabel.setVisible(false);
        }

        Label numPostiVIPLabel = new Label("Posti VIP: " + numPostiVIP);
        numPostiVIPLabel.setFont(font);
        numPostiVIPLabel.setTextFill(Color.WHITE);
        if (numPostiVIP == 0) {
            numPostiVIPLabel.setVisible(false);
        }

        grigliaSale.setHgap(150);
        grigliaSale.setVgap(60);

        ImageView snapHallView;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            snapHallView = new ImageView(new Image(fis, 220, 395, true, true));

            snapHallView.setOnMouseClicked(event -> openHallPreview(file, nomeSalaLabel));
        } catch (FileNotFoundException ex) {
            throw new ApplicationException(ex);
        } finally {
            CloseableUtils.close(fis);
        }

        AnchorPane pane = new AnchorPane();
        if (hallColumnCount == hallColumnCountMax) {
            hallColumnCount = 0;
            hallRowCount++;
        }
        grigliaSale.add(pane, hallColumnCount, hallRowCount);
        hallColumnCount++;

        hallPanel.setContent(grigliaSale);
        GridPane.setMargin(pane, new Insets(15, 0, 0, 15));

        nomeSalaLabel.setLayoutY(snapHallView.getLayoutY() + 133);
        numPostiTotaliLabel.setLayoutY(nomeSalaLabel.getLayoutY() + 15);
        numPostiDisabiliLabel.setLayoutY(numPostiTotaliLabel.getLayoutY() + 15);
        numPostiVIPLabel.setLayoutY(numPostiDisabiliLabel.getLayoutY() + 15);

        pane.getChildren().addAll( snapHallView
                , nomeSalaLabel
                , numPostiTotaliLabel
                , numPostiDisabiliLabel
                , numPostiVIPLabel);

        GUIUtils.setScaleTransitionOnControl(snapHallView);
    }

    private void openHallPreview(File file, Label nomeSalaLabel) {
        BorderPane borderPane = new BorderPane();
        ImageView imageView = new ImageView();

        Image image;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            image = new Image(fis);
        } catch (FileNotFoundException e) {
            throw new ApplicationException(e);
        } finally {
            CloseableUtils.close(fis);
        }

        imageView.setImage(image);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        borderPane.setCenter(imageView);
        Stage stage = new Stage();
        stage.setTitle(nomeSalaLabel.getText());
        Scene scene = new Scene(borderPane);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
        stage.setScene(scene);
        stage.show();
    }

    private int getSeatNumberPerType(List<Seat> mdsList, SeatTYPE type) {
        int res = 0;
        for(Seat mds : mdsList) {
            if(mds.getType().equals(type)) {
                res++;
            }
        }
        return res;
    }

    private List<Seat> initDraggableSeatsList(String nomeSala) {
        return CSVToDraggableSeats.getMyDraggableSeatListFromCSV(DataReferences.PIANTINEFOLDERPATH+nomeSala+".csv");
    }
    /* **********************************************************************************************************************************************************/


    /* ************************************************************* METODI RIGUARDANTI LOGIN/LOGOUT/REGISTRAZIONE **************************************************************/
    private void openRegistrazione(){
        try {
            stageRegistrazione.setScene(new Scene(new FXMLLoader(getClass().getResource("/fxml/login/Registrazione.fxml")).load()));
            stageRegistrazione.setResizable(false);
            stageRegistrazione.setTitle("Registrazione");
            stageRegistrazione.show();
            stageRegistrazione.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    private void openLogin(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login/Login.fxml"));
            Parent p = loader.load();
            LoginController lc = loader.getController();
            lc.init(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(p));
            stage.setTitle("Login");
            stage.setResizable(false);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
            stage.show();
        } catch (IOException ex) {
            throw new ApplicationException(ex);
        }
    }

    public void loginWindow(){
        if(!stageLogin.isShowing()){
            if(loggedUser==null) {
                openLogin();
            } else {
                openReservedArea();
            }
        }
    }

    private void doLogout() {
        logLabel.setText("effettua il login");
        nonRegistratoQuestionLabel.setText("non sei registrato?");
        registerButton.setText("Registrati");
        areaRiservataButton.setVisible(false);
        logoutPane.setVisible(false);
        loggedUser = null;

        if(checkIfThereIsAlreadyUserSaved()) {
            UserInfo.deleteUserInfoFileInUserDir();
        }

        anchorInfo.setVisible(false);
        singleFilmPane.setVisible(false);
        homePane.setVisible(false);
        hallPanel.setVisible(false);
        welcomePanel.setVisible(true);
        welcomeLabel.setText("Benvenuto in Golden Movie Studio");
        welcomeFooter.setVisible(true);

        if(reservedAreaStage != null) {
            if(reservedAreaStage.isShowing()) {
                isReservedAreaOpened = false;
                arhc.closeAllSubWindows();
                reservedAreaStage.close();
            }
        }

        if(managerAreaStage != null) {
            if(managerAreaStage.isShowing()) {
                isManagerAreaOpened = false;
                mhc.closeAllSubWindows();
                managerAreaStage.close();
            }
        }

        if(prenotationStage != null) {
            if(prenotationStage.isShowing()) {
                isPrenotationAreaOpened = false;
                mpc.closeAllSubWindows();
                prenotationStage.close();
            }
        }
    }

    @FXML private void logoutListener() { doLogout(); }

    public void registrationWindow(MouseEvent event){
        Label label = (Label) event.getSource();
        
        KeyValue XValue = new KeyValue(label.scaleXProperty(), 0.85);
        KeyFrame forwardX = new KeyFrame(javafx.util.Duration.seconds(0.125), XValue);
        Timeline timelineX = new Timeline(forwardX);
        timelineX.setAutoReverse(true);
        timelineX.setCycleCount(2);
        timelineX.play();
        KeyValue YValue = new KeyValue(label.scaleYProperty(), 0.85);
        KeyFrame forwardY = new KeyFrame(javafx.util.Duration.seconds(0.125), YValue);
        Timeline timelineY = new Timeline(forwardY);
        timelineY.setAutoReverse(true);
        timelineY.setCycleCount(2);
        timelineY.play();
        
        if(!stageRegistrazione.isShowing()){
            if(loggedUser==null) {
                openRegistrazione();
                animationMenu();
            } else {
                doLogout();
                animationMenu();
            }
        }
    }

    @FXML public void welcomeRegisterButtonListener() { openRegistrazione(); }


    private void openReservedArea() {
        if(!isHimANormalUser(loggedUser)) {
            doOpenManagerArea();
        } else {
            doOpenReservedArea();
        }
    }

    private boolean isManagerAreaOpened = false;
    private void doOpenManagerArea() {
        if(!isManagerAreaOpened) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/manager/ManagerHome.fxml"));
                Parent root = loader.load();
                mhc = loader.getController();
                mhc.init(this);
                managerAreaStage = new Stage();
                managerAreaStage.setScene(new Scene(root));
                managerAreaStage.setTitle("Area Manager");
                managerAreaStage.setMinHeight(850);
                managerAreaStage.setMinWidth(1100);
                managerAreaStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
                managerAreaStage.setOnCloseRequest( event -> {
                    isManagerAreaOpened = false;
                    mhc.closeAllSubWindows();
                });
                managerAreaStage.show();
                isManagerAreaOpened = true;
            } catch (IOException e) {
                throw new ApplicationException(e);
            }
        }

    }

    private boolean isReservedAreaOpened = false;
    void doOpenReservedArea() {
        if(!isReservedAreaOpened) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/areariservata/AreaRiservataHome.fxml"));
                Parent p = loader.load();
                arhc = loader.getController();
                arhc.init(loggedUser, true);
                reservedAreaStage = new Stage();
                reservedAreaStage.setScene(new Scene(p));
                reservedAreaStage.setMinHeight(850);
                reservedAreaStage.setMinWidth(1200);
                reservedAreaStage.setTitle("Area riservata di " + loggedUser.getName());
                reservedAreaStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
                reservedAreaStage.setOnCloseRequest( event -> {
                    isReservedAreaOpened = false;
                    arhc.closeAllSubWindows();
                });
                reservedAreaStage.show();
                isReservedAreaOpened = true;
            } catch (IOException ex) {
                throw new ApplicationException(ex);
            }
        }
    }

    private void setupLoggedUser() {
        logLabel.setText(loggedUser.getName());
        logoutPane.setVisible(true);
        nonRegistratoQuestionLabel.setText("Vuoi uscire?");
        registerButton.setText("logout");
        if(!isHimANormalUser(loggedUser)) {
            areaRiservataButton.setText("Area Manager");
        } else {
            areaRiservataButton.setText("Area Riservata");
        }
        welcomeLabel.setText(loggedUser.getName() + ", bentornato in Golden Movie Studio!");
        welcomeFooter.setVisible(false);
        areaRiservataButton.setVisible(true);
    }

    private boolean isHimANormalUser(User user) {
        return !( user.getName().equalsIgnoreCase(DataReferences.ADMINUSERNAME)
                &&  user.getPassword().equalsIgnoreCase(DataReferences.ADMINPASSWORD));
    }

    private boolean checkIfThereIsAlreadyUserSaved() {
        return UserInfo.checkIfUserInfoFileExists();
    }
    /* ************************************************************************************************************************************************************************/


    /* ************************************************************* TRIGGER DI AGGIORNAMENTO **************************************************************/
    public void triggerNewLogin(User user) {
        loggedUser = user;
        setupLoggedUser();
    }

    public void triggerNewMovieEvent() {
        if(homePane.isVisible() || singleFilmPane.isVisible()) {
            filmScroll.setVisible(true);
            homePane.setVisible(true);
            filmScrollFiltered.setVisible(false);
            rectangle2D3D.setVisible(false);
            singleFilmPane.setVisible(false);
            genreLabel.setText("genere");
        }
        initMovieGrid();
    }

    public void triggerNewHallEvent() { initHallGrid(); }
    /* *****************************************************************************************************************************************************/
}