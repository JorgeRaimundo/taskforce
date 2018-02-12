package pt.taskforce;

import eu.hansolo.medusa.Gauge;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller implements Initializable{

    @FXML
    public Gauge gauge1;
    @FXML
    public Gauge gauge2;
    @FXML
    public Gauge gauge3;
    @FXML
    public Gauge gauge4;
    @FXML
    public Gauge gauge5;
    @FXML
    public Gauge gaugeTotal;
    @FXML
    public Label rxLabel;
    @FXML
    public Label label1;
    @FXML
    public Label label2;
    @FXML
    public Label label3;
    @FXML
    public Label label4;
    @FXML
    public Label label5;
    @FXML
    public Label labelTotal;

    private ValueFetcher fetcher;

    private Pattern bikePattern = Pattern.compile("^Bike (?<bikeNum>\\d+)$");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fetcher = new ValueFetcher();

        WattGaugeConfiguration wattGaugeConfiguration = new WattGaugeConfiguration();
        //VoltGaugeConfiguration voltGaugeConfiguration = new VoltGaugeConfiguration();
        //AmpGaugeConfiguration ampGaugeConfiguration = new AmpGaugeConfiguration();
        TotalGaugeConfiguration totalGaugeConfiguration = new TotalGaugeConfiguration();

        rxLabel.visibleProperty().bind(fetcher.rxMissing);
        gauge1.valueProperty().bind(fetcher.valueBike1);
        styleGauge(gauge1, Color.rgb(77,208,225), wattGaugeConfiguration);

        label1.textProperty().bind(fetcher.sumBike1.asString("%.2f Wh"));

        gauge2.valueProperty().bind(fetcher.valueBike2);
        styleGauge(gauge2, Color.rgb(255,183,77), wattGaugeConfiguration);

        label2.textProperty().bind(fetcher.sumBike2.asString("%.2f Wh"));

        gauge3.valueProperty().bind(fetcher.valueBike3);
        styleGauge(gauge3, Color.rgb(129,199,132), wattGaugeConfiguration);

        label3.textProperty().bind(fetcher.sumBike3.asString("%.2f Wh"));

        gauge4.valueProperty().bind(fetcher.valueBike4);
        styleGauge(gauge4, Color.rgb(149,117,205), wattGaugeConfiguration);

        label4.textProperty().bind(fetcher.sumBike4.asString("%.2f Wh"));

        gauge5.valueProperty().bind(fetcher.valueBike5);
        styleGauge(gauge5, Color.rgb(229,115,115), wattGaugeConfiguration);

        label5.textProperty().bind(fetcher.sumBike5.asString("%.2f Wh"));

        gaugeTotal.valueProperty().bind(fetcher.valueTotal);
        styleGauge(gaugeTotal, Color.rgb(255,255,255), totalGaugeConfiguration);

        labelTotal.textProperty().bind(fetcher.sumTotal.asString("%.2f Wh"));

        new Thread(fetcher).start();
    }

    private void styleGauge(Gauge gauge, Color color, GaugeConfiguration gaugeConfiguration) {
        gauge.setMinValue(gaugeConfiguration.minValue);
        gauge.setMaxValue(gaugeConfiguration.maxValue);
        gauge.setAnimationDuration(gaugeConfiguration.animationDuration);
        gauge.setAnimated(true);
        gauge.setSkinType(Gauge.SkinType.MODERN);
        gauge.setUnit(gaugeConfiguration.unit);
        gauge.setBarColor(color);
        gauge.setInteractive(true);
        EventHandler<Gauge.ButtonEvent> buttonEventEventHandler = event -> {
            String gaugeTitle = ((Gauge) event.getTarget()).getTitle();

            if (gaugeTitle.equals("Total")) {
                fetcher.resetAll();
                return;
            }

            Matcher bikeMatcher = bikePattern.matcher(gaugeTitle);
            bikeMatcher.find();
            fetcher.resetBike(Integer.parseInt(bikeMatcher.group("bikeNum")));
        };
        gauge.setOnButtonPressed(buttonEventEventHandler);
    }

    public void stop() {
        fetcher.stop();
    }
}
