package com.tuzhan;

import android.annotation.TargetApi;
import android.os.Build;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by chenchangheng on 8/12/17.
 */

@TargetApi(Build.VERSION_CODES.N)
public class Utils {

    static String concatenate(List<?> objects){
        return objects.stream().map(Object::toString).collect(Collectors.joining("-"));
    }

    static List<String> split(String formattedStr){
        String[] arr = formattedStr.split("-");
        List<String> list = new ArrayList<>();

        list.addAll(Arrays.asList(arr));

        return list;
    }

    static List<Integer> splitToInts(String formattedStr){
        return map(split(formattedStr), Integer::valueOf);
    }

    static <E, R> List map(List<E> objects, Function<E, R> func){
        return objects.stream().map(func).collect(Collectors.toList());
    }

    static String getUserEmail(){
        return FirebaseAuth.getInstance().getCurrentUser().getEmail();
    }

}
