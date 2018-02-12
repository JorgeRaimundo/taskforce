package pt.taskforce;

import eu.hansolo.medusa.Gauge;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

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

    private ValueFetcher fetcher;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fetcher = new ValueFetcher();

        WattGaugeConfiguration wattGaugeConfiguration = new WattGaugeConfiguration();
        //VoltGaugeConfiguration voltGaugeConfiguration = new VoltGaugeConfiguration();
        //AmpGaugeConfiguration ampGaugeConfiguration = new AmpGaugeConfiguration();

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
    }

    public void stop() {
        fetcher.stop();
    }
}
