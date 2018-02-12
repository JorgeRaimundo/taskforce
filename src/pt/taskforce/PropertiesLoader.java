package pt.taskforce;

import java.util.Properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class PropertiesLoader {
    private static final PropertiesLoader instance = new PropertiesLoader();

    private Properties properties;

    //private constructor to avoid client applications to use constructor
    private PropertiesLoader(){
        properties = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream("config.properties");

            // load a properties file
            properties.load(input);

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public static PropertiesLoader getInstance(){
        return instance;
    }

    public int getIntProperty(String propName) {
        return Integer.parseInt(properties.getProperty(propName));
    }

    public String getProperty(String propName) {
        return properties.getProperty(propName);
    }
}
