package pt.taskforce;

class TotalGaugeConfiguration extends GaugeConfiguration {
    TotalGaugeConfiguration() {
        super();
        readConfig("watt");
        animationDuration /= 5;
        maxValue *= 5;
    }
}
