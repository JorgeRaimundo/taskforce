package pt.taskforce;

class VoltGaugeConfiguration extends GaugeConfiguration {
    VoltGaugeConfiguration() {
        super();
        readConfig("volt");
    }
}
