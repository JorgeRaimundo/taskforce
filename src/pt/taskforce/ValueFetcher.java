package pt.taskforce;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;

import com.fazecast.jSerialComm.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ValueFetcher extends Task<Integer> {

    private static final int NEW_READ_TIMEOUT = 100;
    private static final int NEW_WRITE_TIMEOUT = 0;

    private volatile boolean exit = false;

    public SimpleBooleanProperty rxMissing = new SimpleBooleanProperty(true);
    public SimpleDoubleProperty valueBike1 = new SimpleDoubleProperty(0);
    public SimpleDoubleProperty valueBike2 = new SimpleDoubleProperty(0);
    public SimpleDoubleProperty valueBike3 = new SimpleDoubleProperty(0);
    public SimpleDoubleProperty valueBike4 = new SimpleDoubleProperty(0);
    public SimpleDoubleProperty valueBike5 = new SimpleDoubleProperty(0);
    public SimpleDoubleProperty valueTotal = new SimpleDoubleProperty(0);
    public SimpleDoubleProperty sumBike1 = new SimpleDoubleProperty(0);
    public SimpleDoubleProperty sumBike2 = new SimpleDoubleProperty(0);
    public SimpleDoubleProperty sumBike3 = new SimpleDoubleProperty(0);
    public SimpleDoubleProperty sumBike4 = new SimpleDoubleProperty(0);
    public SimpleDoubleProperty sumBike5 = new SimpleDoubleProperty(0);
    public SimpleDoubleProperty sumTotal = new SimpleDoubleProperty(0);

    private double[] bikeValues = new double[6];
    private double[] sumValues = new double[6];

    private SimpleDoubleProperty[] boundBikeValues = new SimpleDoubleProperty[]{
            valueBike1,
            valueBike2,
            valueBike3,
            valueBike4,
            valueBike5,
            valueTotal
    };

    private SimpleDoubleProperty[] boundSumValues = new SimpleDoubleProperty[]{
            sumBike1,
            sumBike2,
            sumBike3,
            sumBike4,
            sumBike5,
            sumTotal
    };

    private int baudRate;
    private int dataBits;
    private int stopBits;
    private int parity;
    private int hwFlowCtrl;

    private String comString;
    private String portString;

    private boolean loggingEnabled;
    private String logFile;
    private final static Logger logger = Logger.getLogger(ValueFetcher.class.getName());
    private static FileHandler fh = null;

    ValueFetcher() {
        PropertiesLoader properties = PropertiesLoader.getInstance();

        this.baudRate = properties.getIntProperty("baudRate");
        this.dataBits = properties.getIntProperty("dataBits");
        this.stopBits = properties.getIntProperty("stopBits");
        this.parity = properties.getIntProperty("parity");
        this.hwFlowCtrl = properties.getIntProperty("hwFlowCtrl");
        this.comString = properties.getProperty("comString");
        this.portString = properties.getProperty("portString");
        loggingEnabled = Boolean.parseBoolean(properties.getProperty("app.logging"));
        logFile = properties.getProperty("app.logFile");

        if (loggingEnabled) {
            try {
                fh = new FileHandler(logFile, false);
            } catch (SecurityException | IOException e) {
                e.printStackTrace();
            }
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.CONFIG);
        }
    }

    @Override
    protected Integer call() {
        while (!exit) {

            SerialPort comPort = connect();

            InputStream in = getInputStream(comPort);

            try {
                log("Network id\tSensor id\tVgen\tIgen\tWgen\n");

                while (!exit) {
                    int networkId = (byte) in.read();
                    int sensorId = (byte) in.read();

                    if (networkId != 1 || sensorId < 1 || sensorId > 5) {
                        log("Lost packet synch. Trying from another starting point");
                        continue;
                    }

                    // packet[2] to packet [5] = measured microvolts on ADC0 (uint32_t)
                    int valCh0 = ((byte) in.read() << 24) + ((byte) in.read() << 16) + ((byte) in.read() << 8) + (byte) in.read();
                    // This gives the volts generated by the bike:
                    double vgen = 15.7 * ((double) valCh0) / 1e6;

                    //packet[6] to packet [9] = measured microvolts on ADC1 (uint32_t)
                    int valCh1 = ((byte) in.read() << 24) + ((byte) in.read() << 16) + ((byte) in.read() << 8) + (byte) in.read();
                    // This gives the amperes generated by the bike:
                    double igen = 54.644 * ((double) valCh1) / 1e6;

                    for (int i = 0; i < 21; ++i) {
                        in.read();
                    }

                    double wgen = vgen * igen;

                    log(String.format("%d\t%d\t%f\t%f\t%f\n", networkId, sensorId, vgen, igen, wgen));

                    try {
                        int index = sensorId - 1;
                        bikeValues[index] = wgen;
                        sumValues[index] += wgen;
                        updateBoundValues(index);
                        bikeValues[5] = bikeValues[0] + bikeValues[1] + bikeValues[2] + bikeValues[3] + bikeValues[4];
                        sumValues[5] = sumValues[0] + sumValues[1] + sumValues[2] + sumValues[3] + sumValues[4];
                        updateBoundValues(5);
                    } catch (Exception e) {
                        log(String.format(
                                "Failed: %d\t%d\t%f\t%f\t%f\n", networkId, sensorId, vgen, igen, wgen) + e.getMessage()
                        );
                    }

                }

                in.close();
            } catch (IOException ioException) {
                if (ioException.getMessage().equals("This port appears to have been shutdown or disconnected.")) {
                    rxMissing.set(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return 1;
    }

    private SerialPort connect() {
        SerialPort comPort = null;

        while(comPort == null) {
            sleep(1000);

            SerialPort[] comPorts = SerialPort.getCommPorts();
            for (SerialPort comPortI : comPorts) {
                if (comPortI.getSystemPortName().equals(comString) && comPortI.getDescriptivePortName().equals(portString)) {
                    comPort = comPortI;
                    break;
                }
            }

            log("Could not find Rx");
        }

        log("Rx Connected");
        rxMissing.set(false);

        comPort.setBaudRate(baudRate);
        comPort.setNumDataBits(dataBits);
        comPort.setNumStopBits(stopBits);
        comPort.setParity(parity);
        comPort.setFlowControl(hwFlowCtrl);

        return comPort;
    }

    private InputStream getInputStream(SerialPort comPort) {
        comPort.openPort();

        // Input/Output Stream
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, NEW_READ_TIMEOUT, NEW_WRITE_TIMEOUT);

        InputStream in = comPort.getInputStream();

        while (in == null) {
            sleep(1000);
            log("Could not get input from RX!\nIs it already connected to something else?");
            in = comPort.getInputStream();
        }

        try {
            int i = 0;
            while (in.available() != 0) {
                if (i % 10 == 0) {
                    log("Flushing the input");
                }
                in.read();

                ++i;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return in;
    }

    private void sleep(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        exit = true;
    }

    public void resetBike(int bike) {
        if (bike < 1 || bike > 6) {
            log("WTF - Reseting bike " + bike + "?");
            return;
        }
        int index = bike - 1;
        bikeValues[index] = 0;
        sumValues[index] = 0;
        updateBoundValues(index);
    }

    private void updateBoundValues(int index) {
        Platform.runLater(() -> boundBikeValues[index].set(bikeValues[index]));
        Platform.runLater(() -> boundSumValues[index].set(sumValues[index]));
    }

    public void resetAll() {
        for (int i = 1; i < 7; i++) {
            resetBike(i);
        }
    }

    private void log(String message) {
        if (loggingEnabled) {
            logger.config(message);
        }
    }
}
