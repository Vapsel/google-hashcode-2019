package com.hashcode;

import com.hashcode.models.Photo;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileWriter {

    public void writeToFile(String outputFile, List<List<Photo>> slideToPhotoIds) throws IOException {
        PrintWriter writer = new PrintWriter(outputFile, StandardCharsets.UTF_8);
        writer.println(slideToPhotoIds.size());

        for (List<Photo> slide : slideToPhotoIds) {
            StringBuilder stringBuilder = new StringBuilder();
            if (slide.size() == 1){
                stringBuilder.append(slide.get(0).id);
            } else {
                stringBuilder.append(slide.get(0).id).append(" ").append(slide.get(1).id);
            }
            writer.println(stringBuilder.toString());
        }
        writer.close();
    }
}
