package net.tiagonunes.yatt.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import net.tiagonunes.yatt.db.DbService;
import net.tiagonunes.yatt.model.Category;
import net.tiagonunes.yatt.model.Work;
import net.tiagonunes.yatt.model.WorkDone;
import net.tiagonunes.yatt.model.WorkPlanned;
import net.tiagonunes.yatt.report.Report;
import net.tiagonunes.yatt.ui.controls.WorkController;
import net.tiagonunes.yatt.ui.forms.WorkFormController;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
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
    private DatePicker datePicker;

    @FXML
    public void initialize() {
        LocalDate now = LocalDate.now();
        datePicker.setValue(now);
        datePicker.valueProperty().addListener(observable -> loadWork(datePicker.getValue()));
        loadWork(now);


        rootHBox.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.P) {
                planWork();
            } else if (keyEvent.getCode() == KeyCode.D) {
                doWork();
            }
        });
    }

    @FXML
    private void planWork() {
        LocalTime earliestAvailableTime = DbService.get().getEarliestAvailableTime(datePicker.getValue());
        List<Category> categories = DbService.get().reloadCategories();

        WorkFormController form;
        try {
            form = WorkFormController.getController();
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
            work.setDate(datePicker.getValue());

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
            form = WorkFormController.getController();
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
            work.setDate(datePicker.getValue());

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
        yAxis.setLabel("Time (hours)");
        yAxis.setMinorTickCount(0);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Time per category (planned vs actual)");
        chart.setCategoryGap(40);
        chart.setAnimated(false);

        LocalDate lastWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate today = LocalDate.now();

        loadReportData(chart, lastWeek, today);

        DatePicker datePickerFrom = new DatePicker(lastWeek);
        DatePicker datePickerTo = new DatePicker(today);

        datePickerFrom.valueProperty().addListener(o -> loadReportData(chart, datePickerFrom.getValue(), datePickerTo.getValue()));
        datePickerTo.valueProperty().addListener(o -> loadReportData(chart, datePickerFrom.getValue(), datePickerTo.getValue()));

        HBox hBox = new HBox(new Label("From"), datePickerFrom, new Label("To"), datePickerTo);
        hBox.setAlignment(Pos.BASELINE_RIGHT);
        hBox.setSpacing(10);

        VBox vBox = new VBox(hBox, chart);
        VBox.setVgrow(chart, Priority.ALWAYS);
        vBox.setSpacing(20);

        Stage stage = new Stage();
        stage.initOwner(rootHBox.getScene().getWindow());
        stage.initModality(Modality.NONE);
        Scene scene = new Scene(vBox, 1200, 400);
        stage.setScene(scene);
        stage.setTitle("Report");
        stage.show();
    }

    private void loadReportData(BarChart<String, Number> chart, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            return;
        }

        List<XYChart.Series<String, Number>> chartData = Report.getCategorySeries(from, to);
        Platform.runLater(() -> {
            chart.getData().clear();
            chart.getData().setAll(chartData);

            for (XYChart.Series<String, Number> entry : chart.getData()) {
                for (XYChart.Data<String, Number> datum : entry.getData()) {
                    if (datum != null && datum.getExtraValue() != null) {
                        Tooltip t = new Tooltip(datum.getExtraValue().toString());
                        hackTooltipTiming(t);
                        Tooltip.install(datum.getNode(), t);
                    }
                }
            }
        });
    }

    // https://stackoverflow.com/questions/26854301/how-to-control-the-javafx-tooltips-delay
    public void hackTooltipTiming(Tooltip tooltip) {
        try {
            Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            Object objBehavior = fieldBehavior.get(tooltip);

            Field activationTimer = objBehavior.getClass().getDeclaredField("activationTimer");
            activationTimer.setAccessible(true);
            Timeline activationTimeline = (Timeline) activationTimer.get(objBehavior);

            activationTimeline.getKeyFrames().clear();
            activationTimeline.getKeyFrames().add(new KeyFrame(new Duration(250)));

            Field hideTimer = objBehavior.getClass().getDeclaredField("hideTimer");
            hideTimer.setAccessible(true);
            Timeline hideTimeline = (Timeline) hideTimer.get(objBehavior);

            hideTimeline.getKeyFrames().clear();
            hideTimeline.getKeyFrames().add(new KeyFrame(new Duration(20_000)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Dialog<ButtonType> getButtonTypeDialog(WorkFormController form, String title) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().setContent(form.getRoot());

        return dialog;
    }

    private void loadWork(LocalDate date) {
        plannedPane.getChildren().clear();
        donePane.getChildren().clear();

        List<WorkPlanned> workPlanned = DbService.get().reloadWorkPlannedForDay(date);
        workPlanned.forEach(work -> loadAndInsertWorkNode(work, plannedPane));

        List<WorkDone> workDone = DbService.get().reloadWorkDoneForDay(date);
        workDone.forEach(work -> loadAndInsertWorkNode(work, donePane));
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
