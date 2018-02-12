package pt.taskforce;

class WattGaugeConfiguration extends GaugeConfiguration {
    WattGaugeConfiguration() {
        super();
        readConfig("watt");
    }
}
