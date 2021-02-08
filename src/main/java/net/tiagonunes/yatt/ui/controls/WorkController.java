package net.tiagonunes.yatt.ui.controls;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import net.tiagonunes.yatt.db.DbService;
import net.tiagonunes.yatt.model.Category;
import net.tiagonunes.yatt.model.Work;
import net.tiagonunes.yatt.ui.forms.WorkFormController;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        loadData();

        rootVBox.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                WorkFormController form;
                try {
                    form = WorkFormController.getController();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                List<Category> categories = DbService.get().reloadCategories();

                form.setStartTime(work.getStartTime());
                form.setCategories(categories);
                form.setDuration(work.getDuration());
                form.setName(work.getName());
                form.setTags(work.getTags());
                form.setCategory(work.getCategory());

                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Edit work");
                dialog.getDialogPane().setContent(form.getRoot());
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                dialog.getDialogPane().lookupButton(ButtonType.OK).disableProperty().bind(form.isValidProperty().not());

                ButtonType buttonType = dialog.showAndWait().orElse(ButtonType.CANCEL);

                if (buttonType != ButtonType.CANCEL) {
                    try {
                        form.fillWork(work);
                        DbService.get().updateWork(work);
                        Platform.runLater(this::loadData);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void loadData() {
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
