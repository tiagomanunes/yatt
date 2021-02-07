package net.tiagonunes.yatt.ui.controls;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import net.tiagonunes.yatt.model.Work;

import java.time.format.DateTimeFormatter;

public class WorkController {
    private final Work work;

    @FXML
    private VBox rootVBox;
    @FXML
    private Label start;
    @FXML
    private Label category;
    @FXML
    private Label name;

    public WorkController(Work work) {
        this.work = work;
    }

    @FXML
    public void initialize() {
        start.setText(work.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));

        int duration = work.getDuration();
        if (duration < 30) {
            category.setText(work.getName());
            name.setVisible(false);
            name.setManaged(false);
        } else {
            category.setText(work.getCategory().getName());
            name.setText(work.getName());
        }

        rootVBox.setPrefHeight(duration);
    }
}
