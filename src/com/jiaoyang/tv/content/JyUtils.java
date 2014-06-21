package com.jiaoyang.tv.content;

import java.util.ArrayList;

import com.jiaoyang.tv.data.Channel;
import com.jiaoyang.tv.data.HomePage;
import com.jiaoyang.tv.data.Movie;

public class JyUtils {

    public static ArrayList<Movie> transferToArrayList(HomePage page) {
        if (page == null) {
            return null;
        }
        ArrayList<Movie> list = new ArrayList<Movie>(20);
        for (Movie m : page.movies) {
            if (m.tv_display == 1) {
                list.add(m);
            }
        }
        for (int i = 0; i < page.channels.length; i++) {
            Channel channel = page.channels[i];
            for (Movie m : channel.movies) {
                if (m.tv_display == 1) {
                    list.add(m);
                }
            }
        }
        return list;
    }
}
