package com.hashcode;

import com.hashcode.models.Config;
import com.hashcode.models.Photo;

import java.io.IOException;
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

    static Config config = new Config();
    // result
    static List<List<Photo>> slideToPhotoIds = new ArrayList<>();



    static HashMap<String, List<Integer>> tagToPhotoIds = new HashMap<>();
    static Map<String, List<Photo>> positionToPhotoIds = new HashMap<>();
    static Set<Integer> usedPhotoIds = new HashSet<>();

    static {
        positionToPhotoIds.put("H", new ArrayList<>());
        positionToPhotoIds.put("V", new ArrayList<>());
    }

    static Instant start;
    static Instant lastLog;

    public static void main(String[] args) throws IOException {
        start = Instant.now();
        lastLog = Instant.now();

        String inputFile = args[0];
        List<Photo> photos = new FileParser().readFromFile(inputFile);

        String timeFromStart = Duration.between(start, Instant.now()).toString();
        System.out.println("File parsed in: " + timeFromStart);

        String outputFile = args[1];
        fileWriter = new FileWriter(outputFile);

        // sort V
        List<Photo> sortedPhotos = positionToPhotoIds.get("V").stream()
                .sorted(Comparator.comparing(p -> p.tagCount))
                .collect(Collectors.toList());


        processHorizontal();
//        List<List<Photo>> sliderToPhotosIds = processVertical(photos, outputFile);

        fileWriter.closeStreams();
    }

    public static List<List<Photo>> processHorizontal() {

//        List<Photo> horizontalSet = positionToPhotoIds.get("H").stream()
//                .sorted((p1, p2) -> p2.tagCount.compareTo(p1.tagCount))
//                .collect(Collectors.toList());
        HashSet<Photo> horizontalSet = new HashSet<>(positionToPhotoIds.get("H"));
        System.out.println("H table size " + horizontalSet.size());

        // Test histogram by tag count
//        TreeSet<Photo> justForTest = new TreeSet<>((p1, p2) -> p2.tagCount.compareTo(p1.tagCount));
//        justForTest.addAll(horizontalSet);

        // first photo
        Photo processedPhoto = positionToPhotoIds.get("H").get(0);
        slideToPhotoIds.add(List.of(processedPhoto));
        usedPhotoIds.add(processedPhoto.id);
        horizontalSet.remove(processedPhoto);


        int lastStoredPhotoIndex = 0;
        while (true) {

            int andrzejFunctionResult = Integer.MAX_VALUE;
            Photo minResult = null;
            int percent30 = horizontalSet.size() / 3;
            for (Photo testedPhoto : horizontalSet) {
                if (usedPhotoIds.contains(testedPhoto.id)) {
                    continue;
                }
                int tmpResult = slidesDifference(List.of(processedPhoto), List.of(testedPhoto));
                if (tmpResult < andrzejFunctionResult) {
                    andrzejFunctionResult = tmpResult;
                    minResult = testedPhoto;
                }
            }

            if (andrzejFunctionResult == Integer.MAX_VALUE) {
                break;
            }

            slideToPhotoIds.add(List.of(minResult));
            usedPhotoIds.add(minResult.id);
            processedPhoto = minResult;
            horizontalSet.remove(minResult);

            lastStoredPhotoIndex = continuousStoreAndLog(lastStoredPhotoIndex);
        }
        return slideToPhotoIds;
    }

    public static List<List<Photo>> processVertical(List<Photo> photos, String outputFile) throws IOException {
        HashSet<Photo> verticalSet = new HashSet<>(positionToPhotoIds.get("V"));
        System.out.println("V table size " + verticalSet.size());
        List<Photo> get = positionToPhotoIds.get("V");
        List<List<Photo>> slideToPhotoIds = new ArrayList<>();

        List<Photo> processedSlide = List.of(get.get(0), get.get(1));

        while (true) {

            int andrzejFunctionResult = Integer.MAX_VALUE;
            List<Photo> minResult = null;


            for (int i = 2; i < get.size(); i = i + 2) {
                Photo testedPhoto = get.get(i);
                Photo testedPhoto2 = get.get(i + 1);
                if (usedPhotoIds.contains(testedPhoto.id)) {
                    continue;
                }
                List<Photo> testedSlide = List.of(testedPhoto, testedPhoto2);
                int tmpResult = slidesDifference(processedSlide, testedSlide);
                if (tmpResult < andrzejFunctionResult) {
                    andrzejFunctionResult = tmpResult;
                    minResult = testedSlide;
                }
            }


            if (andrzejFunctionResult == Integer.MAX_VALUE) {
                break;
            }

            slideToPhotoIds.add(minResult);
            usedPhotoIds.add(minResult.get(0).id);
            usedPhotoIds.add(minResult.get(1).id);
            processedSlide = minResult;

            if (usedPhotoIds.size() % 100 == 0) {
                System.out.println("Used collection size " + usedPhotoIds.size());
            }

            if (usedPhotoIds.size() % 1000 == 0) {
                System.out.println("Used collection size " + usedPhotoIds.size());
//                new FileWriter().appendToFile(outputFile, slideToPhotoIds);
            }
        }
        return slideToPhotoIds;
    }

    private static int continuousStoreAndLog(int lastStoredPhotoIndex) {
        int storePeriod = 100;
        if (usedPhotoIds.size() % storePeriod == 0) {
            System.out.println("Used collection size " + usedPhotoIds.size());
            Instant now = Instant.now();
            String timeFromStart = Duration.between(start, now).toString();
            String timeFromLastLog = Duration.between(lastLog, now).toString();

            System.out.println("[Timer] Elapsed from start: " + timeFromStart + "   \t \t Elapsed from last log: " + timeFromLastLog);
            lastLog = now;

            fileWriter.appendToFile(slideToPhotoIds, lastStoredPhotoIndex, lastStoredPhotoIndex + storePeriod - 1);
            lastStoredPhotoIndex += storePeriod;
        }
        return lastStoredPhotoIndex;
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
}
