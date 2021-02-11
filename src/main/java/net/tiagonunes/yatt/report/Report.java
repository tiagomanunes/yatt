package net.tiagonunes.yatt.report;

import javafx.scene.chart.XYChart;
import net.tiagonunes.yatt.db.DbService;
import net.tiagonunes.yatt.model.Category;
import net.tiagonunes.yatt.model.WorkDone;
import net.tiagonunes.yatt.model.WorkPlanned;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

public class Report {

    public static List<XYChart.Series<String, Number>> getCategorySeries(LocalDate from, LocalDate to) {
        XYChart.Series<String, Number> plannedSeries = new XYChart.Series<>();
        plannedSeries.setName("Planned");

        List<WorkPlanned> workPlanned = DbService.get().getWorkPlannedForRange(from, to);

        Map<Category, Integer> workPlannedPerCategory = workPlanned.stream()
                .collect(groupingBy(WorkPlanned::getCategory, summingInt(WorkPlanned::getDuration)));

        workPlannedPerCategory.entrySet().stream()
                .sorted(Comparator.comparing(o -> o.getKey().getName()))
                .map(e -> {
                    Category category = e.getKey();
                    Map<String, Double> tagBreakdown = workPlanned.stream()
                            .filter(w -> w.getCategory().equals(category))
                            .collect(groupingBy(wp -> wp.getTags() == null ? "untagged" : wp.getTags(), summingDouble(WorkPlanned::getDuration)));
                    tagBreakdown.entrySet().forEach(b -> b.setValue(b.getValue() / 60.0));
                    XYChart.Data<String, Number> data = new XYChart.Data<>(category.getName(), e.getValue() / 60.0);
                    data.setExtraValue(tagBreakdown);
                    return data;
                })
                .forEach(d -> plannedSeries.getData().add(d));

        XYChart.Series<String, Number> doneSeries = new XYChart.Series<>();
        doneSeries.setName("Actual");


        List<WorkDone> workDone = DbService.get().getWorkDoneForRange(from, to);

        Map<Category, Integer> workDonePerCategory = workDone.stream()
                .collect(groupingBy(WorkDone::getCategory, summingInt(WorkDone::getDuration)));

        workDonePerCategory.entrySet().stream()
                .sorted(Comparator.comparing(o -> o.getKey().getName()))
                .map(e -> {
                    Category category = e.getKey();
                    Map<String, Double> tagBreakdown = workDone.stream()
                            .filter(w -> w.getCategory().equals(category))
                            .collect(groupingBy(wd -> wd.getTags() == null ? "untagged" : wd.getTags(), summingDouble(WorkDone::getDuration)));
                    tagBreakdown.entrySet().forEach(b -> b.setValue(b.getValue() / 60.0));
                    XYChart.Data<String, Number> data = new XYChart.Data<>(category.getName(), e.getValue() / 60.0);
                    data.setExtraValue(tagBreakdown);
                    return data;
                })
                .forEach(d -> doneSeries.getData().add(d));

        List<XYChart.Series<String, Number>> data = new ArrayList<>();
        data.add(plannedSeries);
        data.add(doneSeries);

        return data;
    }
}
