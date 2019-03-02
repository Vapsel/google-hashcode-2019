package com.hashcode;

import com.hashcode.models.Config;
import com.hashcode.models.Photo;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {

    static FileWriter fileWriter;
    static int lastStoredSlideIndex = 0;

    static Config config = new Config();
    // result
    static List<List<Photo>> slideToPhotoIds = new ArrayList<>();

    static HashMap<String, List<Integer>> tagToPhotoIds = new HashMap<>();
    static Map<String, List<Photo>> positionToPhotoIds = new HashMap<>();
    static {
        positionToPhotoIds.put("H", new ArrayList<>());
        positionToPhotoIds.put("V", new ArrayList<>());
    }

    static Instant start;
    static Instant lastLog;

    /**
     * 1 argument: input data file e.g. b_lovely_landscapes.txt
     * 2 argument: output data file without extension e.g. stream/b
     */
    public static void main(String[] args) throws IOException {
        start = Instant.now();
        lastLog = Instant.now();

        String inputFile = args[0];
        List<Photo> photos = new FileParser().readFromFile(inputFile);

        String timeFromStart = Duration.between(start, Instant.now()).toString();
        System.out.println("File parsed in: " + timeFromStart);

        String outputFile = args[1];
        fileWriter = new FileWriter(outputFile);

        // Test histogram by tag count
        histogramVisualisationFile(photos, outputFile);

        if (!positionToPhotoIds.get("H").isEmpty()) {
            processHorizontal();
        }
        if (!positionToPhotoIds.get("V").isEmpty()) {
            processVertical();
        }

        fileWriter.closeStreams();
    }

    public static void processHorizontal() {
        List<Photo> horizontalSet = new ArrayList<>(positionToPhotoIds.get("H"));
        System.out.println("H table size " + horizontalSet.size());

        // first photo
        int firstPhotoIndex = 0;
        // start with photo that has more tags
        Photo processedPhoto = horizontalSet.get(firstPhotoIndex);
        slideToPhotoIds.add(List.of(processedPhoto));
        horizontalSet.remove(firstPhotoIndex);

        while (true) {

            int minSlidesDifference = Integer.MAX_VALUE;
            Photo minResult = null;
            int minResultIndex = 0;

            for (int i = 0; i < horizontalSet.size(); i++) {
                Photo testedPhoto = horizontalSet.get(i);
                int tmpResult = slidesDifference(List.of(processedPhoto), List.of(testedPhoto));
                if (tmpResult < minSlidesDifference) {
                    minSlidesDifference = tmpResult;
                    minResult = testedPhoto;
                    minResultIndex = i;
                }
            }

            if (minSlidesDifference == Integer.MAX_VALUE) {
                // store last piece of results
                fileWriter.appendToFile(slideToPhotoIds, lastStoredSlideIndex, Integer.MAX_VALUE);
                break;
            }

            slideToPhotoIds.add(List.of(minResult));
            processedPhoto = minResult;
            horizontalSet.remove(minResultIndex);

            lastStoredSlideIndex = continuousStoreAndLog(lastStoredSlideIndex);
        }
    }

    public static void processVertical() {
        List<Photo> verticalSet = new ArrayList<>(positionToPhotoIds.get("V"));
        System.out.println("V table size " + verticalSet.size());

        // first photo
        int firstSlidePhoto1Index = 0, firstSlidePhoto2Index = 1;
        List<Photo> processedSlide = List.of(verticalSet.get(firstSlidePhoto1Index), verticalSet.get(firstSlidePhoto2Index));
        slideToPhotoIds.add(processedSlide);
        verticalSet.remove(firstSlidePhoto2Index);
        verticalSet.remove(firstSlidePhoto1Index);

        while (true) {

            int minSlidesDifference = Integer.MAX_VALUE;
            List<Photo> minResult = null;
            int minResultIndex = 0;

            for (int i = 0; i < verticalSet.size(); i = i + 2) {
                Photo testedPhoto1 = verticalSet.get(i);
                Photo testedPhoto2 = verticalSet.get(i + 1);
                List<Photo> testedSlide = List.of(testedPhoto1, testedPhoto2);
                int tmpResult = slidesDifference(processedSlide, testedSlide);
                if (tmpResult < minSlidesDifference) {
                    minSlidesDifference = tmpResult;
                    minResult = testedSlide;
                    minResultIndex = i;
                }
            }

            if (minSlidesDifference == Integer.MAX_VALUE) {
                // store last piece of results
                fileWriter.appendToFile(slideToPhotoIds, lastStoredSlideIndex, Integer.MAX_VALUE);
                break;
            }


            slideToPhotoIds.add(minResult);
            processedSlide = minResult;
            verticalSet.remove(minResultIndex + 1);
            verticalSet.remove(minResultIndex);

            lastStoredSlideIndex = continuousStoreAndLog(lastStoredSlideIndex);
        }
    }

    private static int continuousStoreAndLog(int lastStoredSlideIndex) {
        int storePeriod = 100;
        if (slideToPhotoIds.size() % storePeriod == 0) {
            System.out.println("Ready slides count: " + slideToPhotoIds.size());
            Instant now = Instant.now();
            String timeFromStart = Duration.between(start, now).toString();
            String timeFromLastLog = Duration.between(lastLog, now).toString();

            System.out.println("[Timer] Elapsed from start: " + timeFromStart + "   \t \t Elapsed from last log: " + timeFromLastLog);
            lastLog = now;

            fileWriter.appendToFile(slideToPhotoIds, lastStoredSlideIndex, lastStoredSlideIndex + storePeriod - 1);
            lastStoredSlideIndex += storePeriod;
        }
        return lastStoredSlideIndex;
    }


    /**
     * Result is at least 0 or greater than zero.
     * Minimum result is better.
     */
    public static int slidesDifference(List<Photo> slide1, List<Photo> slide2){
        int sum1 = slide1.get(0).tagCount;
        if (slide1.size() == 2){
            sum1 += slide1.get(1).tagCount;
        }
        int half = sum1/2;

        int sum2 = slide2.get(0).tagCount;
        if (slide2.size() == 2){
            sum2 += slide2.get(1).tagCount;
        }


        Set<String> sumSlide1Tags = new HashSet<>(slide1.get(0).tags);
        if (slide1.size() == 2){
            sumSlide1Tags.addAll(slide1.get(1).tags);
        }

        Set<String> sumSlide2Tags = slide2.get(0).tags;
        if (slide2.size() == 2){
            sumSlide1Tags.addAll(slide2.get(1).tags);
        }

        sumSlide1Tags.retainAll(sumSlide2Tags);

        return Math.abs(half - sumSlide1Tags.size()) + Math.abs(sum1 - sum2);
    }

    public static int interestFactor(Photo photo1, Photo photo2){
        Set<String> intersection = new HashSet<>(photo1.tags);
        intersection.retainAll(photo2.tags);

        return Math.min(intersection.size(),
                Math.min(photo1.tagCount - intersection.size(),
                        photo2.tagCount - intersection.size()));
    }

    private static void histogramVisualisationFile(List<Photo> photos, String outputFile) throws IOException {
        List<String> csvLines = new ArrayList<>();
        csvLines.add("Tag count, Number of photos");

        photos.stream()
                .collect(Collectors.groupingBy(photo -> photo.tagCount))
                .entrySet().stream()
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .map(entry -> entry.getKey() + ", " + entry.getValue().size())
                    .forEachOrdered(csvLines::add);

        PrintWriter csvFileWriter = new PrintWriter(outputFile + "_histogram.csv", StandardCharsets.UTF_8);
        csvLines.forEach(csvFileWriter::println);
        csvFileWriter.close();
    }
}
