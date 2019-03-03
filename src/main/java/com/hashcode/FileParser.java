package com.hashcode;

import com.hashcode.models.Config;
import com.hashcode.models.Photo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.hashcode.Main.positionToPhotoIds;
import static com.hashcode.Main.tagToPhotos;

public class FileParser {

    public List<Photo> readFromFile(String fileName) throws IOException {
        File f = new File(fileName);
        BufferedReader b = new BufferedReader(new FileReader(f));
        System.out.println("Reading file " + fileName + " using Buffered Reader");

        // first line - config
        String readLine = b.readLine();
//        String[] line = readLine.split(" ");
        Config config = new Config();
        config.NUMBER_OF_PHOTOS = Integer.valueOf(readLine);
        Main.config = config;




        List<Photo> photos = new ArrayList<>(config.NUMBER_OF_PHOTOS);
        int photoId = 0;
        while ((readLine = b.readLine()) != null) {
            String[] line = readLine.split(" ");
            String position = line[0];
            int tagCount = Integer.valueOf(line[1]);
            String[] tags = Arrays.copyOfRange(line, 2, line.length);

            List<String> tagsList = Arrays.asList(tags);
            Photo photo = new Photo(photoId, tagCount, position);
            photo.tags.addAll(tagsList);

            for (String tag : tagsList) {
                if (tagToPhotos.containsKey(tag)) {
                    tagToPhotos.get(tag).add(photo);
                } else {
                    ArrayList<Photo> photoIds = new ArrayList<>();
                    photoIds.add(photo);
                    tagToPhotos.put(tag, photoIds);
                }
            }

            positionToPhotoIds.get(position).add(photo);


            photos.add(photo);
            photoId++;
        }
        return photos;
    }
}
