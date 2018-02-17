package pt.taskforce;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class SentenceFetcher extends Task<Integer> {

    private volatile boolean exit = false;

    public SimpleStringProperty currentSentence = new SimpleStringProperty("");

    private ArrayList<String> sentences;
    private int sentenceTime;

    SentenceFetcher() {
        PropertiesLoader properties = PropertiesLoader.getInstance();

        sentenceTime = properties.getIntProperty("app.sentenceTime");
        sentences = getMotivationalSentences();
    }

    private ArrayList<String> getMotivationalSentences() {
        ArrayList<String> motivationalSentences = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(
                                new FileInputStream("frases.txt"), "UTF-8")
        )) {
            for(String line; (line = br.readLine()) != null; ) {
                motivationalSentences.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return motivationalSentences;
    }

    @Override
    protected Integer call() {
        if (sentences.size() == 0) {
            return 1;
        }

        int currentIndex = 0;
        while (!exit) {

            try {
                String sentence = sentences.get(currentIndex);
                Platform.runLater(() -> currentSentence.set(sentence));
            } catch (Exception e) {
                System.out.println((e.getMessage()));
            }

            sleep(sentenceTime * 1000);

            currentIndex = (++currentIndex) % sentences.size();
        }

        return 1;
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
