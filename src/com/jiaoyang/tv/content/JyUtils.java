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
        for (int i = 0; i < 200; i ++) {
//            Movie movie = page.data[i];
            Movie movie = new Movie();
            movie.title = "相爱十年";
            movie.mid = "245";
            movie.imgurl = "http://newadmin.sun-tv.com.cn/ckfinder/userfiles/images/222.jpg";
            if (i == 0) {
                movie.layoutType = 1;
            } else {
                movie.layoutType = 0;
            }
            list.add(movie);
        }
        return list;
    }
}
