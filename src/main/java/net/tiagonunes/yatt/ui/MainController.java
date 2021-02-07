package net.tiagonunes.yatt.ui;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.tiagonunes.yatt.db.DbService;
import net.tiagonunes.yatt.model.Category;
import net.tiagonunes.yatt.model.Work;
import net.tiagonunes.yatt.model.WorkDone;
import net.tiagonunes.yatt.model.WorkPlanned;
import net.tiagonunes.yatt.report.Report;
import net.tiagonunes.yatt.ui.controls.WorkController;
import net.tiagonunes.yatt.ui.forms.WorkFormController;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class MainController {
    @FXML
    private HBox rootHBox;
    @FXML
    private FlowPane plannedPane;
    @FXML
    private FlowPane donePane;

    @FXML
    public void initialize() {
        List<WorkPlanned> workPlanned = DbService.get().reloadWorkPlannedForDay();
        workPlanned.forEach(work -> loadAndInsertWorkNode(work, plannedPane));

        rootHBox.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.P) {
                planWork();
            }
        });

        List<WorkDone> workDone = DbService.get().reloadWorkDoneForDay();
        workDone.forEach(work -> loadAndInsertWorkNode(work, donePane));

        rootHBox.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.D) {
                doWork();
            }
        });
    }

    @FXML
    private void planWork() {
        LocalTime earliestAvailableTime = DbService.get().getEarliestAvailableTime();
        List<Category> categories = DbService.get().reloadCategories();

        WorkFormController form;
        try {
            form = getWorkFormController();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        form.setStartTime(earliestAvailableTime);
        form.setCategories(categories);

        Dialog<ButtonType> dialog = getButtonTypeDialog(form, "Plan work");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.NEXT, ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.NEXT).disableProperty().bind(form.isValidProperty().not());
        dialog.getDialogPane().lookupButton(ButtonType.OK).disableProperty().bind(form.isValidProperty().not());

        ButtonType buttonType = dialog.showAndWait().orElse(ButtonType.CANCEL);

        if (buttonType != ButtonType.CANCEL) {
            WorkPlanned work = new WorkPlanned();
            form.fillWork(work);
            try {
                DbService.get().insertWorkPlanned(work);
                Platform.runLater(() -> loadAndInsertWorkNode(work, plannedPane));

                if (buttonType == ButtonType.NEXT) {
                    planWork();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void doWork() {
        LocalTime now = LocalTime.now();
        LocalTime startingTime = now.truncatedTo(ChronoUnit.HOURS).plusMinutes(15 * ((now.getMinute() / 15) + 1));
        List<Category> categories = DbService.get().reloadCategories();

        WorkFormController form;
        try {
            form = getWorkFormController();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        form.setStartTime(startingTime);
        form.setCategories(categories);

        Dialog<ButtonType> dialog = getButtonTypeDialog(form, "Do work");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.OK).disableProperty().bind(form.isValidProperty().not());

        ButtonType buttonType = dialog.showAndWait().orElse(ButtonType.CANCEL);

        if (buttonType != ButtonType.CANCEL) {
            WorkDone work = new WorkDone();
            form.fillWork(work);
            try {
                DbService.get().insertWorkDone(work);
                Platform.runLater(() -> loadAndInsertWorkNode(work, donePane));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void openReport() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Category");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Time (minutes)");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Time per category (planned vs actual)");
        chart.setCategoryGap(40);

        ObservableList<XYChart.Series<String, Number>> chartData = Report.getCategorySeries();
        chart.setData(chartData);

        Stage stage = new Stage();
        stage.initOwner(rootHBox.getScene().getWindow());
        stage.initModality(Modality.APPLICATION_MODAL);
        Scene scene = new Scene(chart, 1200, 400);
        stage.setScene(scene);
        stage.setTitle("Report");
        stage.show();
    }

    private WorkFormController getWorkFormController() throws IOException {
        FXMLLoader loader = new FXMLLoader(MainController.class.getResource("/fxml/forms/work.fxml"));
        loader.load();

        return loader.getController();
    }

    private Dialog<ButtonType> getButtonTypeDialog(WorkFormController form, String title) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().setContent(form.getRoot());

        return dialog;
    }

    private void loadAndInsertWorkNode(Work work, FlowPane pane) {
        FXMLLoader loader = new FXMLLoader(MainController.class.getResource("/fxml/controls/work.fxml"));
        loader.setControllerFactory(c -> new WorkController(work));
        try {
            pane.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
