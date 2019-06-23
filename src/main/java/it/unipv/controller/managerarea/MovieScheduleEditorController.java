package it.unipv.controller.managerarea;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import it.unipv.db.*;
import it.unipv.dao.HallDao;
import it.unipv.dao.MovieDao;
import it.unipv.dao.ScheduleDao;
import it.unipv.dao.HallDaoImpl;
import it.unipv.dao.MovieDaoImpl;
import it.unipv.dao.ScheduleDaoImpl;
import it.unipv.controller.common.GUIUtils;
import it.unipv.model.Movie;
import it.unipv.model.Schedule;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.ApplicationUtils;
import it.unipv.utils.DataReferences;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

public class MovieScheduleEditorController {

    @FXML DatePicker datePicker;
    @FXML AnchorPane timeSpinnerContainer;
    @FXML ComboBox hallComboBox;
    @FXML Label salvaProgrammazioneButton;
    private CustomTimeSpinner timeSpinner;
    private Movie movie;
    private MovieSchedulerController moviePanelController;
    private MovieDao movieDao;
    private HallDao hallDao;
    private ScheduleDao scheduleDao;

    void init(MovieSchedulerController movieSchedulerController, Movie movie, DBConnection dbConnection) {
        this.movieDao = new MovieDaoImpl(dbConnection);
        this.hallDao = new HallDaoImpl(dbConnection);
        this.scheduleDao = new ScheduleDaoImpl(dbConnection);
        this.moviePanelController = movieSchedulerController;
        this.movie = movie;
        GUIUtils.setScaleTransitionOnControl(salvaProgrammazioneButton);
        initTimePicker();
        initHallSelector();
    }

    private void initHallSelector() {
        hallComboBox.getItems().clear();
        List<String> hallNames = hallDao.retrieveHallNames();
        Collections.sort(hallNames);
        hallComboBox.setItems(FXCollections.observableList(hallNames));
    }

    private void initTimePicker() {
        timeSpinner = new CustomTimeSpinner();

        timeSpinnerContainer.getChildren().addAll(timeSpinner);
        timeSpinner.prefWidthProperty().bind(timeSpinnerContainer.widthProperty());
        timeSpinner.prefHeightProperty().bind(timeSpinnerContainer.heightProperty());
    }

    @FXML private void initSaveButtonListener() {
        String date = datePicker.getValue() == null ? "" : formatDate(datePicker.getValue().toString());
        String time = timeSpinner.getValue() == null ? "" : formatTime(timeSpinner.getValue().toString());
        String hall = hallComboBox.getValue() == null ? "" : hallComboBox.getValue().toString();

        if( date.trim().equalsIgnoreCase("")
         || time.trim().equalsIgnoreCase("")
         || hall.trim().equalsIgnoreCase("") ){
            GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore", "Devi compilare tutti i campi!");
        } else if (ApplicationUtils.checkIfDateIsPassed(date + " " + time)) {
            GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore", "Non puoi programmare un film nel passato!");
        } else if(checkIfSomethingIsAlreadyScheduledInThatTemporalGap(hall, date + " " + time, Integer.parseInt(movie.getDurata())) ) {
            GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore", "C'è già una programmazione in questo periodo!");
        } else {
            Schedule schedule = new Schedule();
            schedule.setMovieCode(movie.getCodice());
            schedule.setDate(date);
            schedule.setTime(time);
            schedule.setHallName(hall);
            scheduleDao.insertNewMovieSchedule(schedule);
            moviePanelController.triggerNewScheduleEvent();
            GUIUtils.showAlert(Alert.AlertType.INFORMATION, "Successo", "Operazione riuscita: ", "Salvataggio riuscito correttamente!");
        }
    }

    private String formatDate(String toFormat) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
            return sdf1.format(sdf.parse(toFormat));
        } catch (ParseException e) {
            throw new ApplicationException(e);
        }
    }

    //Non hallDao ben capito perché a volte lo spinner ritorna una data con formato HH:mm:ss.SSS
    private String formatTime(String toFormat) {
        String[] time = toFormat.split(":");
        if(time.length>2) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
                SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
                return sdf1.format(sdf.parse(toFormat));
            } catch (ParseException e) {
                throw new ApplicationException(e);
            }
        } else {
            return toFormat;
        }
    }

    private boolean checkIfSomethingIsAlreadyScheduledInThatTemporalGap(String hall, String incomingScheduleDate, int incomingMovieDuration) {
        List<Schedule> schedules = scheduleDao.retrieveMovieSchedules();
        for(Schedule ms : schedules) {
            if( (ms.getDate().trim().equalsIgnoreCase(incomingScheduleDate) && ms.getHallName().trim().equalsIgnoreCase(hall))
             || (ms.getHallName().trim().equalsIgnoreCase(hall)) ) {
                int existingMovieDuration = 0;
                List<Movie> movies = movieDao.retrieveMovieListWithoutPoster();
                for(Movie m : movies) {
                    if(ms.getMovieCode().equalsIgnoreCase(m.getCodice())) {
                        existingMovieDuration = Integer.parseInt(m.getDurata());
                        break;
                    }
                }
                if(!checkIfICanAddThisSchedule(ms.getDate() + " " + ms.getTime(), existingMovieDuration, incomingScheduleDate, incomingMovieDuration)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkIfICanAddThisSchedule(String existingScheduleDate, int existingMovieDuration, String incomingScheduleDate, int incomingMovieDuration) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Calendar realIncomingScheduleDate = Calendar.getInstance();
        try {
            realIncomingScheduleDate.setTime(sdf.parse(incomingScheduleDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return realIncomingScheduleDate.before(getTimeOccupiedBySchedule(existingScheduleDate, incomingMovieDuration, false))
                || realIncomingScheduleDate.after(getTimeOccupiedBySchedule(existingScheduleDate, existingMovieDuration, true));
    }

    private Calendar getTimeOccupiedBySchedule(String existingScheduleDate, int movieDuration, boolean isItToAdd) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Calendar result = Calendar.getInstance();
        try {
            result.setTime(sdf.parse((existingScheduleDate)));
        } catch (ParseException e) {
            throw new ApplicationException(e);
        }
        if(isItToAdd) { result.add(Calendar.MINUTE, movieDuration+DataReferences.PAUSEAFTERMOVIE); }
        if(!isItToAdd) { result.add(Calendar.MINUTE, -(movieDuration+DataReferences.PAUSEAFTERMOVIE)); }
        return result;
    }
}