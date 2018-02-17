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
    public Label motivationLabel;
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

    private ValueFetcher valueFetcher;
    private SentenceFetcher sentenceFetcher;

    private Pattern bikePattern = Pattern.compile("^Bicicleta (?<bikeNum>\\d+)$");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        valueFetcher = new ValueFetcher();
        sentenceFetcher = new SentenceFetcher();
        motivationLabel.textProperty().bind(sentenceFetcher.currentSentence);


        WattGaugeConfiguration wattGaugeConfiguration = new WattGaugeConfiguration();
        //VoltGaugeConfiguration voltGaugeConfiguration = new VoltGaugeConfiguration();
        //AmpGaugeConfiguration ampGaugeConfiguration = new AmpGaugeConfiguration();
        TotalGaugeConfiguration totalGaugeConfiguration = new TotalGaugeConfiguration();

        rxLabel.visibleProperty().bind(valueFetcher.rxMissing);
        motivationLabel.visibleProperty().bind(valueFetcher.rxMissing.not());
        gauge1.valueProperty().bind(valueFetcher.valueBike1);
        styleGauge(gauge1, Color.rgb(77,208,225), wattGaugeConfiguration);

        label1.textProperty().bind(valueFetcher.sumBike1.asString("%.2f Wh"));

        gauge2.valueProperty().bind(valueFetcher.valueBike2);
        styleGauge(gauge2, Color.rgb(255,183,77), wattGaugeConfiguration);

        label2.textProperty().bind(valueFetcher.sumBike2.asString("%.2f Wh"));

        gauge3.valueProperty().bind(valueFetcher.valueBike3);
        styleGauge(gauge3, Color.rgb(129,199,132), wattGaugeConfiguration);

        label3.textProperty().bind(valueFetcher.sumBike3.asString("%.2f Wh"));

        gauge4.valueProperty().bind(valueFetcher.valueBike4);
        styleGauge(gauge4, Color.rgb(149,117,205), wattGaugeConfiguration);

        label4.textProperty().bind(valueFetcher.sumBike4.asString("%.2f Wh"));

        gauge5.valueProperty().bind(valueFetcher.valueBike5);
        styleGauge(gauge5, Color.rgb(229,115,115), wattGaugeConfiguration);

        label5.textProperty().bind(valueFetcher.sumBike5.asString("%.2f Wh"));

        gaugeTotal.valueProperty().bind(valueFetcher.valueTotal);
        styleGauge(gaugeTotal, Color.rgb(255,255,255), totalGaugeConfiguration);

        labelTotal.textProperty().bind(valueFetcher.sumTotal.asString("%.2f Wh"));

        new Thread(valueFetcher).start();
        new Thread(sentenceFetcher).start();
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
                valueFetcher.resetAll();
                return;
            }

            Matcher bikeMatcher = bikePattern.matcher(gaugeTitle);
            bikeMatcher.find();
            valueFetcher.resetBike(Integer.parseInt(bikeMatcher.group("bikeNum")));
        };
        gauge.setOnButtonPressed(buttonEventEventHandler);
    }

    public void stop() {
        valueFetcher.stop();
        sentenceFetcher.stop();
    }
}
