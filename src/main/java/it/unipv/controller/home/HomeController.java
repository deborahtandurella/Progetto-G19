package it.unipv.controller.home;

import it.unipv.controller.common.*;
import it.unipv.db.DBConnection;
import it.unipv.conversion.UserInfo;
import it.unipv.model.Movie;
import it.unipv.controller.login.LoginController;
import it.unipv.controller.login.RegistrazioneController;
import it.unipv.model.User;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.DataReferences;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeController implements IHomeTrigger, IHomeInitializer {
    @FXML private Rectangle rectangleMenu;
    @FXML private AnchorPane menuWindow, menuContainer;
    @FXML private Label logLabel, nonRegistratoQuestionLabel, registerButton, areaRiservataButton, statusLabel, tipsLabel;
    @FXML private ProgressBar statusPBar;
    @FXML private AnchorPane logoutPane;
    @FXML private BorderPane homePanel;
    private final Stage stageRegistrazione = new Stage();
    private DBConnection dbConnection;
    private final Stage stageLogin = new Stage();
    private User loggedUser;
    private IManagerAreaInitializer managerHomeController;
    private IUserReservedAreaInitializer areaRiservataInitializer;
    private HallListPanelController hallListPanelController;
    private MovieListPanelController movieListPanelController;
    private Stage reservedAreaStage, managerAreaStage;
    private List<ICloseablePane> iCloseablePanes = new ArrayList<>();
    private Thread tipsThread;

    @Override
    public void init(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
        if(checkIfThereIsAlreadyUserSaved()) {
            loggedUser = UserInfo.getUserInfo();
            setupLoggedUser();
        } else {
            logoutPane.setVisible(false);
            areaRiservataButton.setVisible(false);
        }
        initWelcomePage(loggedUser);
        menuWindow.setVisible(false);
        menuWindow.setPickOnBounds(false);
        menuContainer.setPickOnBounds(false);
        statusLabel.setVisible(false);
        statusPBar.setVisible(false);
        animateTipsLabel();
    }

    private void animateTipsLabel() {
        tipsThread = GUIUtils.getTipsThread(DataReferences.HOMETIPS, tipsLabel, 10000);
        tipsThread.start();
    }

    @FXML
    private void animationMenu(){
        if(!menuWindow.isVisible()){
            menuWindow.setOpacity(0);
            menuWindow.setVisible(true);
            new Timeline(new KeyFrame(Duration.seconds(0.3), new KeyValue(rectangleMenu.widthProperty(), rectangleMenu.getWidth() +81))).play();
            new Timeline(new KeyFrame(Duration.seconds(0.3), new KeyValue(rectangleMenu.heightProperty(), rectangleMenu.getHeight()+244))).play();

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.4), menuWindow);
            fadeIn.setDelay(Duration.seconds(0.2));
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        } else {
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.1), menuWindow);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0);
            fadeOut.play();
            menuWindow.setVisible(false);

            new Timeline(new KeyFrame(Duration.seconds(0.15), new KeyValue(rectangleMenu.heightProperty(), rectangleMenu.getHeight() - 244))).play();
            new Timeline(new KeyFrame(Duration.seconds(0.15), new KeyValue(rectangleMenu.widthProperty(), rectangleMenu.getWidth() - 81))).play();
        }
    }

    private void initWelcomePage(User user) {
        try {
            homePanel.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home/welcome.fxml"));
            AnchorPane welcomePanel = loader.load();
            welcomePanel.prefWidthProperty().bind(homePanel.widthProperty());
            welcomePanel.prefHeightProperty().bind(homePanel.heightProperty());
            WelcomePanelController wpc = loader.getController();
            wpc.init(user, dbConnection, stageRegistrazione);
            homePanel.setCenter(welcomePanel);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    @FXML
    private void homeClick() {
        closeAllSubWindows();
        KeyFrame kf1 = new KeyFrame(Duration.millis(100), e -> animationMenu());
        KeyFrame kf2 = new KeyFrame(Duration.millis(290), e -> openHomePanel());
        Platform.runLater(new Timeline(kf1,kf2)::play);
    }

    @FXML
    private void salaClick() {
        closeAllSubWindows();
        KeyFrame kf1 = new KeyFrame(Duration.millis(100), e -> animationMenu());
        KeyFrame kf2 = new KeyFrame(Duration.millis(290), e -> openHallList());
        Platform.runLater(new Timeline(kf1,kf2)::play);
    }

    @FXML
    private void infoClick() {
        closeAllSubWindows();
        openInfo();
        animationMenu();
    }

    @FXML
    private void areaRiservataClick() {
        openReservedArea();
        animationMenu();
    }

    @FXML
    private void registrationWindow(){
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

    @FXML
    private void loginWindow(){
        if(!stageLogin.isShowing()){
            if(loggedUser==null) {
                openLogin();
            } else {
                openReservedArea();
            }
        }
    }

    @FXML private void logoutListener() { doLogout(); }

    private void openHomePanel() {
        movieListPanelController = openNewPanel("/fxml/home/movieList.fxml").getController();
        movieListPanelController.init(this, homePanel.getWidth(), dbConnection);
    }

    private FXMLLoader openNewPanel(String fxmlpath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlpath));
            AnchorPane pane = loader.load();
            pane.prefWidthProperty().bind(homePanel.widthProperty());
            pane.prefHeightProperty().bind(homePanel.heightProperty());
            homePanel.setCenter(pane);
            return loader;
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    private void openHallList() {
        hallListPanelController = openNewPanel("/fxml/home/hallList.fxml").getController();
        hallListPanelController.init(this, homePanel.getWidth(), dbConnection);
        if(!iCloseablePanes.contains(hallListPanelController)) { iCloseablePanes.add(hallListPanelController); }
    }

    private void openInfo() {
        openNewPanel("/fxml/home/info.fxml");
    }

    private void openRegistrazione(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login/Registrazione.fxml"));
            Parent p = loader.load();
            RegistrazioneController rc = loader.getController();
            rc.init(dbConnection);
            stageRegistrazione.setScene(new Scene(p));
            stageRegistrazione.setResizable(false);
            stageRegistrazione.setTitle("Registrazione");
            stageRegistrazione.show();
            stageRegistrazione.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    private void doLogout() {
        triggerStartStatusEvent("Disconnessione in corso...");
        logLabel.setText("effettua il login");
        nonRegistratoQuestionLabel.setText("non sei registrato?");
        registerButton.setText("Registrati");
        areaRiservataButton.setVisible(false);
        logoutPane.setVisible(false);
        loggedUser = null;

        if(checkIfThereIsAlreadyUserSaved()) {
            UserInfo.deleteUserInfoFileInUserDir();
        }

        closeReservedArea();
        closeManagerArea();
        closeAllSubWindows();

        initWelcomePage(loggedUser);
        triggerEndStatusEvent("Disconnessione avvenuta con successo!");
    }

    private void closeManagerArea() {
        if(managerAreaStage != null) {
            if(managerAreaStage.isShowing()) {
                isManagerAreaOpened = false;
                managerHomeController.closeAllSubWindows();
                managerAreaStage.close();
            }
        }
    }

    private void closeReservedArea() {
        if(reservedAreaStage != null) {
            if(reservedAreaStage.isShowing()) {
                isReservedAreaOpened = false;
                areaRiservataInitializer.closeAllSubWindows();
                reservedAreaStage.close();
            }
        }
    }

    private void closeAllSubWindows() {
        for(ICloseablePane i : iCloseablePanes) {
            i.closeAllSubWindows();
        }
    }

    private void openLogin(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login/Login.fxml"));
            Parent p = loader.load();
            LoginController lc = loader.getController();
            lc.init(this, dbConnection);
            stageLogin.setScene(new Scene(p));
            stageLogin.setTitle("Login");
            stageLogin.setResizable(false);
            stageLogin.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
            stageLogin.show();
        } catch (IOException ex) {
            throw new ApplicationException(ex);
        }
    }

    private void openReservedArea() {
        if(isHimAnAdmin(loggedUser)) {
            doOpenManagerArea();
        } else {
            doOpenReservedArea();
        }
    }

    private boolean isManagerAreaOpened = false;
    private void doOpenManagerArea() {
        if(!isManagerAreaOpened) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/managerarea/ManagerHome.fxml"));
                Parent root = loader.load();
                managerHomeController = loader.getController();
                managerHomeController.init(this, dbConnection);
                managerAreaStage = new Stage();
                managerAreaStage.setScene(new Scene(root));
                managerAreaStage.setTitle("Area Manager");
                managerAreaStage.setMinHeight(710);
                managerAreaStage.setMinWidth(1020);
                managerAreaStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
                managerAreaStage.setOnCloseRequest( event -> {
                    isManagerAreaOpened = false;
                    managerHomeController.closeAllSubWindows();
                });
                managerAreaStage.show();
                isManagerAreaOpened = true;
            } catch (IOException e) {
                throw new ApplicationException(e);
            }
        }

    }

    private boolean isReservedAreaOpened = false;
    private void doOpenReservedArea() {
        if(!isReservedAreaOpened) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/userarea/AreaRiservataHome.fxml"));
                Parent p = loader.load();
                areaRiservataInitializer = loader.getController();
                areaRiservataInitializer.init(loggedUser, true, dbConnection);
                reservedAreaStage = new Stage();
                reservedAreaStage.setScene(new Scene(p));
                reservedAreaStage.setMinHeight(710);
                reservedAreaStage.setMinWidth(1020);
                reservedAreaStage.setTitle("Area riservata di " + loggedUser.getNome());
                reservedAreaStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
                reservedAreaStage.setOnCloseRequest( event -> {
                    isReservedAreaOpened = false;
                    areaRiservataInitializer.closeAllSubWindows();
                });
                reservedAreaStage.show();
                isReservedAreaOpened = true;
            } catch (IOException ex) {
                throw new ApplicationException(ex);
            }
        }
    }

    private void setupLoggedUser() {
        logLabel.setText(loggedUser.getNome());
        logoutPane.setVisible(true);
        nonRegistratoQuestionLabel.setText("Vuoi uscire?");
        registerButton.setText("logout");
        if(isHimAnAdmin(loggedUser)) {
            areaRiservataButton.setText("Area Manager");
        } else {
            areaRiservataButton.setText("Area Riservata");
        }
        areaRiservataButton.setVisible(true);
        initWelcomePage(loggedUser);
    }

    private boolean isHimAnAdmin(User user) {
        return user.getNome().equalsIgnoreCase(DataReferences.ADMINUSERNAME)
            && user.getPassword().equalsIgnoreCase(DataReferences.ADMINPASSWORD);
    }

    private boolean checkIfThereIsAlreadyUserSaved() {
        return UserInfo.checkIfUserInfoFileExists();
    }

    @Override
    public void triggerNewLogin(User user) {
        closeAllSubWindows();
        loggedUser = user;
        setupLoggedUser();
    }

    @Override
    public void triggerNewMovieEvent() {
        if(movieListPanelController !=null) {
            movieListPanelController.triggerNewMovieEvent();
        }
    }

    @Override
    public void triggerNewHallEvent() {
        if(hallListPanelController !=null) {
            hallListPanelController.triggerNewHallEvent();
        }
    }

    @Override public void triggerMovieClicked(Movie movie) {
        openSingleMoviePanel(movie);
    }

    private void openSingleMoviePanel(Movie movie) {
        SingleMoviePanelController smpc = openNewPanel("/fxml/home/singleMoviePanel.fxml").getController();
        smpc.init(this, movie, loggedUser, dbConnection);
        if(!iCloseablePanes.contains(smpc)) { iCloseablePanes.add(smpc); }
    }

    @Override public void triggerOpenHomePanel() { openHomePanel(); }

    @Override public void triggerOpenReservedArea() { doOpenReservedArea(); }

    @Override
    public void triggerStartStatusEvent(String text) {
        statusLabel.setVisible(true);
        statusPBar.setVisible(true);
        statusLabel.setText(text);
        statusPBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
    }

    private static Timeline timeline;
    @Override
    public void triggerEndStatusEvent(String text) {
        if(timeline!=null) { timeline.stop(); }

        KeyFrame kf1 = new KeyFrame(Duration.millis(100), event -> {
            statusLabel.setText(text);
            statusPBar.setProgress(100);
        });

        KeyFrame kf2 = new KeyFrame(Duration.seconds(5), e -> {
            statusLabel.setVisible(false);
            statusPBar.setVisible(false);
        });

        timeline = new Timeline(kf1, kf2);

        Platform.runLater(timeline::play);
    }

    @Override
    public void closeAll() {
        if(tipsThread!=null) { tipsThread.interrupt(); }
        closeReservedArea();
        closeManagerArea();
        closeAllSubWindows();
    }
}
