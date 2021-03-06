package it.unipv.gui.user.areariservata;

import it.unipv.gui.login.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomePanelController {

    @FXML private Label usernameInjectedLabel;
    @FXML private Label emailInjectedLabel;
    @FXML private Label codeInjectedLabel;

    public void init(User loggedUser) {
        usernameInjectedLabel.setText(loggedUser.getName());
        emailInjectedLabel.setText(loggedUser.getEmail());
        codeInjectedLabel.setText(loggedUser.getCodice());
    }
    
}
