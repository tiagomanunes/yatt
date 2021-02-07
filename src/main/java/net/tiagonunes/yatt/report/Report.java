package net.tiagonunes.yatt.report;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import net.tiagonunes.yatt.db.DbService;
import net.tiagonunes.yatt.model.Category;
import net.tiagonunes.yatt.model.WorkDone;
import net.tiagonunes.yatt.model.WorkPlanned;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

public class Report {

    public static ObservableList<XYChart.Series<String, Number>> getCategorySeries() {
        XYChart.Series<String, Number> plannedSeries = new XYChart.Series<>();
        plannedSeries.setName("Planned");
        List<WorkPlanned> workPlanned = DbService.get().reloadWorkPlannedForDay(LocalDate.now());
        Map<Category, Integer> workPlannedPerCategory = workPlanned.stream()
                .collect(groupingBy(WorkPlanned::getCategory, summingInt(WorkPlanned::getDuration)));
        workPlannedPerCategory.entrySet().stream()
                .sorted(Comparator.comparing(o -> o.getKey().getName()))
                .map(e -> new XYChart.Data<>(e.getKey().getName(), (Number) e.getValue()))
                .forEach(d -> plannedSeries.getData().add(d));

        XYChart.Series<String, Number> doneSeries = new XYChart.Series<>();
        doneSeries.setName("Actual");
        List<WorkDone> workDone = DbService.get().reloadWorkDoneForDay(LocalDate.now());
        Map<Category, Integer> workDonePerCategory = workDone.stream()
                .collect(groupingBy(WorkDone::getCategory, summingInt(WorkDone::getDuration)));
        workDonePerCategory.entrySet().stream()
                .sorted(Comparator.comparing(o -> o.getKey().getName()))
                .map(e -> new XYChart.Data<>(e.getKey().getName(), (Number) e.getValue()))
                .forEach(d -> doneSeries.getData().add(d));

        ObservableList<XYChart.Series<String, Number>> data = FXCollections.observableArrayList();
        data.add(plannedSeries);
        data.add(doneSeries);

        return data;
    }
}
