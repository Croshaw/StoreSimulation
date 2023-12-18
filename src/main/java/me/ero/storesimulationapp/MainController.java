package me.ero.storesimulationapp;

import javafx.animation.TranslateTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.StringConverter;
import me.ero.storesimulationapp.simulation.SimulationController;
import me.ero.storesimulationapp.simulation.store_api.util.DurationUtils;
import me.ero.storesimulationapp.simulation.store_api.util.FileHelper;

import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.time.Duration;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

public class MainController implements Initializable {
    private SimulationController simulationController;
    private boolean isPause;
    @FXML
    private Button pauseBTN;
    @FXML
    private Button startBTN;
    @FXML
    private Button stopBTN;
    @FXML
    private Canvas simulationCanvas;
    @FXML
    private Slider speedSlider;
    @FXML
    private Label speedSliderInfo;
    @FXML
    private TextField seedField;
    @FXML
    private TextField durationSimulationField;
    @FXML
    private TextField storeReceiptsFromField;
    @FXML
    private TextField storeReceiptsToField;
    @FXML
    private TextField storeReceiptServiceTimeFromField;
    @FXML
    private TextField storeReceiptServiceTimeToField;
    @FXML
    private Slider simulationStepSlider;
    @FXML
    private Label simulationStepSliderInfo;

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        simulationStepSlider.valueProperty().addListener((observableValue, number, t1) -> {
            simulationStepSliderInfo.setText("%d мин".formatted(t1.intValue()));
        });
        speedSliderInfo.setText("%f".formatted(speedSlider.getValue()));
        speedSlider.valueProperty().addListener((observableValue, number, t1) -> {
            if(simulationController != null && simulationController.getSpeed() != t1.doubleValue()) {
                simulationController.setSpeed(t1.doubleValue());
            }
            speedSliderInfo.setText("%f".formatted(t1.doubleValue()));
        });
        seedField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    seedField.setText(newValue.replaceAll("[^\\d]", ""));
                }
        });

        durationSimulationField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                durationSimulationField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        storeReceiptsFromField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                storeReceiptsFromField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        storeReceiptsToField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                storeReceiptsToField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        storeReceiptServiceTimeFromField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                storeReceiptServiceTimeFromField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        storeReceiptServiceTimeToField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                storeReceiptServiceTimeToField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    protected void saveSimulationController() {
        if(simulationController != null) {
            simulationController.pause();
            FileHelper.writeToFile("Temp.json", SimulationController.serialize(simulationController));
        }
    }

    @FXML
    protected void loadSimulationController() {

    }

    @FXML
    protected void fillDefaultButtonClick() {
        seedField.setText("0");
        durationSimulationField.setText("7");
        storeReceiptsFromField.setText("0");
        storeReceiptsToField.setText("60");
        storeReceiptServiceTimeFromField.setText("60");
        storeReceiptServiceTimeToField.setText("2400");
        simulationStepSlider.setValue(10);
    }
    @FXML
    protected void onStartButtonClick() {
        pauseBTN.setVisible(true);
        stopBTN.setVisible(true);
        startBTN.setVisible(false);
        if(simulationController != null)
            simulationController.stop();
        simulationController = new SimulationController(Integer.parseInt(seedField.getText()),
                simulationStepSlider.valueProperty().intValue()* 60L,
                Duration.ofDays(Integer.parseInt(durationSimulationField.getText())),
                Integer.parseInt(storeReceiptsFromField.getText()),
                Integer.parseInt(storeReceiptsToField.getText()),
                Integer.parseInt(storeReceiptServiceTimeFromField.getText()),
                Integer.parseInt(storeReceiptServiceTimeToField.getText()),
                simulationCanvas);
        simulationController.setSpeed(speedSlider.getValue());
        simulationController.simulate();
    }
    @FXML
    protected void onStopButtonClick() {
        pauseBTN.setVisible(false);
        stopBTN.setVisible(false);
        startBTN.setVisible(true);
        simulationController.stop();
    }
    @FXML
    protected void onPauseButtonClick() {
        isPause = !isPause;
        if(isPause) {
            pauseBTN.setText("Возобновить");
            simulationController.pause();
        }
        else {
            pauseBTN.setText("Пауза");
            simulationController.resume();
        }
    }
}