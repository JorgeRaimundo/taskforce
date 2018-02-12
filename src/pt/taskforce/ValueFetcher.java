package pt.taskforce;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;

import com.fazecast.jSerialComm.*;

import java.io.IOException;
import java.io.InputStream;

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
    public SimpleDoubleProperty sumBike1 = new SimpleDoubleProperty(0);
    public SimpleDoubleProperty sumBike2 = new SimpleDoubleProperty(0);
    public SimpleDoubleProperty sumBike3 = new SimpleDoubleProperty(0);
    public SimpleDoubleProperty sumBike4 = new SimpleDoubleProperty(0);
    public SimpleDoubleProperty sumBike5 = new SimpleDoubleProperty(0);

    private int baudRate;
    private int dataBits;
    private int stopBits;
    private int parity;
    private int hwFlowCtrl;

    private String comString;
    private String portString;

    ValueFetcher() {
        PropertiesLoader properties = PropertiesLoader.getInstance();

        this.baudRate = properties.getIntProperty("baudRate");
        this.dataBits = properties.getIntProperty("dataBits");
        this.stopBits = properties.getIntProperty("stopBits");
        this.parity = properties.getIntProperty("parity");
        this.hwFlowCtrl = properties.getIntProperty("hwFlowCtrl");
        this.comString = properties.getProperty("comString");
        this.portString = properties.getProperty("portString");
    }

    @Override
    protected Integer call() {
        while (!exit) {

            SerialPort comPort = connect();

            SimpleDoubleProperty[] bikeValues = new SimpleDoubleProperty[]{
                valueBike1,
                valueBike2,
                valueBike3,
                valueBike4,
                valueBike5
            };

            SimpleDoubleProperty[] sumValues = new SimpleDoubleProperty[]{
                sumBike1,
                sumBike2,
                sumBike3,
                sumBike4,
                sumBike5
            };

            InputStream in = getInputStream(comPort);

            try {
                System.out.print("Network id\tSensor id\tVgen\tIgen\tWgen\n");

                while (!exit) {
                    int networkId = (byte) in.read();
                    int sensorId = (byte) in.read();

                    if (networkId != 1 || sensorId < 1 || sensorId > 5) {
                        System.out.println("Lost packet synch. Trying from another starting point");
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

                    System.out.print(String.format("%d\t%d\t%f\t%f\t%f\n", networkId, sensorId, vgen, igen, wgen));

                    try {
                        int index = sensorId - 1;
                        double previousSumValue = sumValues[index].get();
                        Platform.runLater(() -> bikeValues[index].set(wgen));
                        Platform.runLater(() -> sumValues[index].set(previousSumValue + wgen));
                    } catch (Exception e) {
                        System.out.println(String.format(
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

            System.out.println("Could not find Rx");
        }

        System.out.println("Rx Connected");
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
            System.out.println("Could not get input from RX!\nIs it already connected to something else?");
            in = comPort.getInputStream();
        }

        try {
            int i = 0;
            while (in.available() != 0) {
                if (i % 10 == 0) {
                    System.out.println("Flushing the input");
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
}