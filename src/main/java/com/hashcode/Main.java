package com.hashcode;

import com.hashcode.models.Config;
import com.hashcode.models.Photo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {

    static Config config = new Config();
    static HashMap<String, List<Integer>> tagToPhotoIds = new HashMap<>();
    static Map<String, List<Photo>> positionToPhotoIds = new HashMap<>();
    static Set<Integer> usedPhotoIds = new HashSet<>();

    static {
        positionToPhotoIds.put("H", new ArrayList<Photo>());
        positionToPhotoIds.put("V", new ArrayList<Photo>());
    }

    public static void main(String[] args) throws IOException {
        String inputFile = args[0];
        List<Photo> photos = new FileParser().readFromFile(inputFile);


        // sort V
        List<Photo> sortedPhotos = positionToPhotoIds.get("V").stream()
                .sorted(Comparator.comparing(p -> p.tagCount))
                .collect(Collectors.toList());


        String outputFile = args[1];
//        List<List<Photo>> sliderToPhotosIds = processHorizontal(photos, outputFile);
        List<List<Photo>> sliderToPhotosIds = processVertical(photos, outputFile);

        new FileWriter().writeToFile(outputFile, sliderToPhotosIds);
    }

    public static List<List<Photo>> processHorizontal(List<Photo> photos, String outputFile) throws IOException {
        List<List<Photo>> slideToPhotoIds = new ArrayList<>();


        HashSet<Photo> horizontalSet = new HashSet<>(positionToPhotoIds.get("H"));
        System.out.println("H table size " + horizontalSet.size());


        // first photo
        Photo processedPhoto = positionToPhotoIds.get("H").get(8);
        slideToPhotoIds.add(List.of(processedPhoto));
        usedPhotoIds.add(processedPhoto.id);
        horizontalSet.remove(processedPhoto);


        while (true) {

            int andrzejFunctionResult = Integer.MAX_VALUE;
            Photo minResult = null;
            for (Photo testedPhoto : horizontalSet) {
                if (usedPhotoIds.contains(testedPhoto.id)) {
                    continue;
                }
                int tmpResult = andrzejfunction(List.of(processedPhoto), List.of(testedPhoto));
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

            if (usedPhotoIds.size() % 100 == 0) {
                System.out.println("Used collection size " + usedPhotoIds.size());
            }

            if (usedPhotoIds.size() % 1000 == 0) {
                System.out.println("Used collection size " + usedPhotoIds.size());
                new FileWriter().writeToFile(outputFile, slideToPhotoIds);
            }
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
                int tmpResult = andrzejfunction(processedSlide, testedSlide);
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
                new FileWriter().writeToFile(outputFile, slideToPhotoIds);
            }
        }
        return slideToPhotoIds;
    }


    public static int andrzejfunction(List<Photo> slide1, List<Photo> slide2){
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

        int result = Math.abs(half - sumSlide1Tags.size()) + Math.abs(sum1 - sum2);
        return result;
    }

    public static int interestFactor(Photo photo1, Photo photo2){
        Set<String> intersection = new HashSet<>(photo1.tags);
        intersection.retainAll(photo2.tags);

        return Math.min(intersection.size(),
                Math.min(photo1.tagCount - intersection.size(),
                        photo2.tagCount - intersection.size()));
    }
}
