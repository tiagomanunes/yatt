package net.tiagonunes.yatt.ui.forms;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import net.tiagonunes.yatt.model.Category;
import net.tiagonunes.yatt.model.Work;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WorkFormController {

    public static WorkFormController getController() throws IOException {
        FXMLLoader loader = new FXMLLoader(WorkFormController.class.getResource("/fxml/forms/work.fxml"));
        loader.load();

        return loader.getController();
    }

    @FXML
    private VBox rootVBox;
    @FXML
    private TextField startTime;
    @FXML
    private TextField duration;
    @FXML
    private TextField name;
    @FXML
    private TextField tags;
    @FXML
    private ComboBox<Category> category;

    private final ObjectProperty<LocalTime> startTimeProperty = new SimpleObjectProperty<>(LocalTime.of(8, 0));
    private final IntegerProperty durationProperty = new SimpleIntegerProperty(60);
    private final ObservableList<Category> categories = FXCollections.observableArrayList();

    private final BooleanProperty isValidProperty = new SimpleBooleanProperty(false);


    public WorkFormController() {}

    @FXML
    public void initialize() {
        startTime.textProperty().bind(Bindings.createStringBinding(() -> startTimeProperty.get().format(DateTimeFormatter.ofPattern("HH:mm")), startTimeProperty));
        startTime.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.UP) {
                startTimeProperty.set(startTimeProperty.get().plusMinutes(15));
            } else if (keyEvent.getCode() == KeyCode.DOWN) {
                startTimeProperty.set(startTimeProperty.get().minusMinutes(15));
            }
        });
        Platform.runLater(() -> startTime.requestFocus());

        duration.textProperty().bind(Bindings.createStringBinding(() -> durationProperty.get() + "", durationProperty));
        duration.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.UP) {
                durationProperty.set(durationProperty.get() + 15);
            } else if (keyEvent.getCode() == KeyCode.DOWN) {
                durationProperty.set(durationProperty.get() - 15);
            }
        });

        category.setItems(categories.sorted());

        isValidProperty.bind(startTime.textProperty().isNotEmpty().and(
                duration.textProperty().isNotEmpty()).and(
                        name.textProperty().isNotEmpty()).and(
                                category.getSelectionModel().selectedItemProperty().isNotNull()));
    }

    public void setStartTime(LocalTime startTime) {
        startTimeProperty.setValue(startTime);
    }

    public void setCategories(List<Category> categories) {
        this.categories.addAll(categories);
    }

    public void setDuration(int duration) {
        durationProperty.set(duration);
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public void setTags(String tags) {
        this.tags.setText(tags);
    }

    public void setCategory(Category category) {
        this.category.getSelectionModel().select(category);
    }

    public BooleanProperty isValidProperty() {
        return isValidProperty;
    }

    public Node getRoot() {
        return rootVBox;
    }

    public <T extends Work> void fillWork(T work) {
        work.setStartTime(startTimeProperty.get());
        work.setDuration(durationProperty.get());
        work.setName(name.getText());
        work.setTags(tags.getText());
        work.setCategory(category.getSelectionModel().getSelectedItem());
    }
}
