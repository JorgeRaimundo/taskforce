package pt.taskforce;


abstract class GaugeConfiguration {

    public int minValue;
    public int maxValue;
    public String unit;
    public int animationDuration;

    private PropertiesLoader properties;

    GaugeConfiguration() {
        properties = PropertiesLoader.getInstance();

        // get the property value and print it out
        animationDuration = properties.getIntProperty("animationDuration");
    }

    void readConfig(String prefix) {
        minValue = properties.getIntProperty(prefix + ".minimumValue");
        maxValue = properties.getIntProperty(prefix + ".maximumValue");
        unit = properties.getProperty(prefix + ".unit");
    }
}
