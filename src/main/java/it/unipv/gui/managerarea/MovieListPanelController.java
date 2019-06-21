package it.unipv.gui.managerarea;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import it.unipv.DB.DBConnection;
import it.unipv.DB.MovieOperations;
import it.unipv.DB.ScheduleOperations;
import it.unipv.gui.common.*;
import it.unipv.utils.ApplicationException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

public class MovieListPanelController implements ICloseablePane {

    @FXML TextField searchBarTextfield;
    @FXML Label searchButton;
    @FXML ScrollPane movieListPanel;

    private GridPane grigliaFilm = new GridPane();
    private static int rowCount = 0;
    private static int columnCount = 0;
    private List<Movie> movies = new ArrayList<>();
    private ManagerHomeController managerHomeController;
    private Stage movieEditorControllerStage;
    private MovieOperations mo;
    private ScheduleOperations so;

    public void init(ManagerHomeController managerHomeController, DBConnection dbConnection) {
        this.mo = new MovieOperations(dbConnection);
        this.so = new ScheduleOperations(dbConnection);
        this.managerHomeController = managerHomeController;
        createMovieListGrid();
    }

    private void initMoviesList() {
        movies = mo.retrieveMovieListWithoutPoster();
        Collections.sort(movies);
    }

    private void createMovieListGrid() {
        grigliaFilm.getChildren().clear();
        initMoviesList();

        for (Movie movie : movies) {
            createViewFromMoviesList(movie);
        }

        GUIUtils.setScaleTransitionOnControl(searchButton);
        initRowAndColumnCount();
    }

    private void initRowAndColumnCount() {
        rowCount=0;
        columnCount=0;
    }

    private void createViewFromMoviesList(Movie movie) {
        Label movieTitleLabel = new Label(StringUtils.abbreviate(movie.getTitolo(),30));
        if(movie.getTitolo().length()>30) {
            movieTitleLabel.setTooltip(new Tooltip(movie.getTitolo()));
        }
        movieTitleLabel.setFont(Font.font("system", FontWeight.NORMAL, FontPosture.REGULAR, 18));
        movieTitleLabel.setTextFill(Color.WHITE);

        grigliaFilm.setHgap(15);
        grigliaFilm.setVgap(15);

        Label deleteIcon = new Label();
        deleteIcon.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        deleteIcon.setGraphic(GUIUtils.getIconView(getClass().getResourceAsStream("/images/Bin.png")));
        deleteIcon.setTooltip(new Tooltip("Elimina " + movie.getTitolo()));
        GUIUtils.setFadeInOutOnControl(deleteIcon);

        Label editIcon = new Label();
        editIcon.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        editIcon.setGraphic(GUIUtils.getIconView(getClass().getResourceAsStream("/images/Edit.png")));
        editIcon.setTooltip(new Tooltip("Modifica " + movie.getTitolo()));
        GUIUtils.setFadeInOutOnControl(editIcon);

        Label setVisibleIcon = new Label();
        setVisibleIcon.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setVisibleIcon.setGraphic(GUIUtils.getIconView(getClass().getResourceAsStream("/images/Visible.png")));
        setVisibleIcon.setTooltip(new Tooltip("Rendi programmabile " + movie.getTitolo()));
        GUIUtils.setFadeInOutOnControl(setVisibleIcon);

        AnchorPane pane = new AnchorPane();
        if(columnCount==1) {
            columnCount=0;
            rowCount++;
        }
        grigliaFilm.add(pane, columnCount, rowCount);
        columnCount++;

        movieListPanel.setContent(grigliaFilm);
        GridPane.setMargin(pane, new Insets(5,5,5,5));

        setVisibleIcon.setLayoutY(movieTitleLabel.getLayoutY());
        setVisibleIcon.setLayoutX(movieTitleLabel.getLayoutX()+270);
        setVisibleIcon.setOnMouseClicked(event -> {
            Optional<ButtonType> option =
                    GUIUtils.showConfirmationAlert( "Attenzione"
                                                  , "Richiesta conferma"
                                                  , "Sei sicuro di voler rendere " + movie.getTitolo() + " programmabile?");
            if(option.orElse(null)==ButtonType.YES) {
                movie.setStatus(MovieStatusTYPE.AVAILABLE);
                mo.updateMovieButNotPoster(movie);
                managerHomeController.triggerToHomeNewMovieEvent();
                refreshUI();
            }
        });

        editIcon.setLayoutY(movieTitleLabel.getLayoutY());
        editIcon.setLayoutX(movieTitleLabel.getLayoutX()+305);
        editIcon.setOnMouseClicked( event -> openMovieEditor(movie));

        deleteIcon.setLayoutY(movieTitleLabel.getLayoutY());
        deleteIcon.setLayoutX(movieTitleLabel.getLayoutX()+340);
        deleteIcon.setOnMouseClicked(e -> {
            Optional<ButtonType> option =
                    GUIUtils.showConfirmationAlert( "Attenzione"
                                                  , "Richiesta conferma:"
                                                  , "Sei sicuro di voler eliminare " + movie.getTitolo() + " e le sue relative programmazioni?");
            if(option.orElse(null)==ButtonType.YES) {
                removeAssociatedSchedules(movie);
                mo.deleteMovie(movie);
                managerHomeController.triggerToHomeNewMovieEvent();
                refreshUI();
            }
        });

        pane.getChildren().add(movieTitleLabel);
        if(!movie.getStatus().equals(MovieStatusTYPE.AVAILABLE)) {
            pane.getChildren().add(setVisibleIcon);
        }
        pane.getChildren().addAll(editIcon, deleteIcon);
    }

    private boolean isMovieEditorAlreadyOpened = false;
    private void openMovieEditor(Movie movie) {
        if(!isMovieEditorAlreadyOpened) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/managerarea/MovieEditor.fxml"));
                Parent p = loader.load();
                MovieEditorController mec = loader.getController();
                mec.init(movie, this);
                movieEditorControllerStage = new Stage();
                movieEditorControllerStage.setScene(new Scene(p));
                movieEditorControllerStage.setTitle("Modifica a: " + movie.getTitolo());
                movieEditorControllerStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
                movieEditorControllerStage.setOnCloseRequest(e -> isMovieEditorAlreadyOpened = false);
                movieEditorControllerStage.show();
                isMovieEditorAlreadyOpened = true;
            } catch (IOException ex) {
                throw new ApplicationException(ex);
            }
        }
    }

    private void removeAssociatedSchedules(Movie movie) {
        List<MovieSchedule> movieSchedules = so.retrieveMovieSchedules();
        for(MovieSchedule ms : movieSchedules) {
            if(movie.getCodice().equalsIgnoreCase(ms.getMovieCode())) {
                so.deleteMovieSchedule(ms);
            }
        }
    }

    private void refreshUI() {
        createMovieListGrid();
    }

    @FXML public void searchButtonListener() {
        String searchedMovieTitle = searchBarTextfield.getText();
        if(searchedMovieTitle!=null) {
            grigliaFilm.getChildren().clear();
            for(Movie m : movies) {
                if(m.getTitolo().toLowerCase().trim().contains(searchedMovieTitle.toLowerCase())){
                    createViewFromMoviesList(m);
                }
            }
            initRowAndColumnCount();
        } else {
            refreshUI();
        }
    }

    void triggerOverwriteMovieButNotPosterEvent(Movie movie) {
        mo.updateMovieButNotPoster(movie);
        refreshUI();
        managerHomeController.triggerToHomeNewMovieEvent();
    }

    void triggerOverwriteMovieEvent(Movie movie, FileInputStream posterStream) {
        mo.updateMovie(movie, posterStream);
        refreshUI();
        managerHomeController.triggerToHomeNewMovieEvent();
    }

    @Override
    public void closeAllSubWindows() {
        if(movieEditorControllerStage!=null) {
            if(movieEditorControllerStage.isShowing()) {
                movieEditorControllerStage.close();
                isMovieEditorAlreadyOpened = false;
            }
        }
    }
}