package com.jiaoyang.tv.data;

public class StartUpPoster {
        public String apiVersion;
        public Poster[] start_page;
        public Poster[] background;

        public static class Poster {
            public String title;
            public String poster;
            public String description;
        }

        public String getStartup() {
            return start_page[0].poster;
        }

        public String getBg1() {
            return background[0].poster;
        }

        public String getBg2() {
            return background[1].poster;
        }
}
