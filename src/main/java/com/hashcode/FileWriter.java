package com.hashcode;

import com.hashcode.models.Photo;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileWriter {

    private PrintWriter outputData;
    private PrintWriter firstLineOutput;

    public FileWriter(String outputFile) throws IOException {
        outputData = new PrintWriter(outputFile + ".txt", StandardCharsets.UTF_8);
        firstLineOutput = new PrintWriter(outputFile + "_first_line.txt", StandardCharsets.UTF_8);
    }

    public void appendToFile(List<List<Photo>> slides, int from, int to) {
        firstLineOutput.println(slides.size());
        firstLineOutput.flush();

        for (int i = from; i <= to && i < slides.size(); i++) {
            List<Photo> slide = slides.get(i);
            if (slide.size() == 1) {
                outputData.append(Integer.toString(slide.get(0).id));
            } else {
                outputData.append(Integer.toString(slide.get(0).id)).append(" ").append(Integer.toString(slide.get(1).id));
            }
            outputData.append(System.lineSeparator());
        }
        outputData.flush();
    }

    public void closeStreams(){
        outputData.close();
        firstLineOutput.close();
    }
}
