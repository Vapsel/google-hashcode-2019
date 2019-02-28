package com.hashcode;

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


        List<List<Photo>> sliderToPhotosIds = compute(photos);

        String outputFile = args[1];
        new FileWriter().writeToFile(outputFile, sliderToPhotosIds);
    }

    public static List<List<Photo>> compute(List<Photo> photos){
        List<List<Photo>> slideToPhotoIds = new ArrayList<>();


        // first photo
        Photo processedPhoto = positionToPhotoIds.get("H").get(0);
        slideToPhotoIds.add(List.of(processedPhoto));

        System.out.println("H table size " + positionToPhotoIds.get("H").size());

        while(true) {

            int andrzejFunctionResult = Integer.MAX_VALUE;
            Photo minResult = null;
            for (Photo testedPhoto : positionToPhotoIds.get("H")) {
                if (usedPhotoIds.contains(testedPhoto.id)) {
                    continue;
                }
                int tmpResult = andrzejfunction(List.of(processedPhoto), List.of(testedPhoto));
                if (tmpResult < andrzejFunctionResult) {
                    andrzejFunctionResult = tmpResult;
                    minResult = testedPhoto;
                }
            }

            if (andrzejFunctionResult == Integer.MAX_VALUE || andrzejFunctionResult == 0) {
                break;
            }

            slideToPhotoIds.add(List.of(minResult));
            usedPhotoIds.add(minResult.id);
            processedPhoto = minResult;

            if (usedPhotoIds.size() % 100 == 0) {
                System.out.println("Used collection size " + usedPhotoIds.size());
            }
        }
        return slideToPhotoIds;

//                .stream()
//                .filter(testedPhoto -> {
//                    if (usedPhotoIds.contains(testedPhoto.id)){
//                        return false;
//                    }


//                    int factor = interestFactor(processedPhoto, testedPhoto);
//                    int halfOfTagCount = processedPhoto.tagCount / 2;
//                    return factor - 1 >= halfOfTagCount && factor + 1 <= halfOfTagCount;
//                })
//                .findAny();


//        if (found.isPresent()){
//            slideToPhotoIds.add(List.of(found.get()));
//            usedPhotoIds.add(found.get().id);
//        } else {
//
//        }


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
