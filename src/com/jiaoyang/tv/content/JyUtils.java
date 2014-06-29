package com.jiaoyang.tv.content;

import java.util.ArrayList;

import com.jiaoyang.tv.data.HomePageData;
import com.jiaoyang.tv.data.Movie;

public class JyUtils {

    public static ArrayList<Movie> transferToArrayList(HomePageData page) {
        if (page == null) {
            return null;
        }
        ArrayList<Movie> list = new ArrayList<Movie>(20);
        for (int i = 0; i < page.data.length; i ++) {
            Movie movie = page.data[i];
            if (i == 0 || i == 1) {
                movie.layoutType = 1;
            } else {
                movie.layoutType = 0;
            }
            list.add(movie);
        }
        return list;
    }
}
