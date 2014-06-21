package com.jiaoyang.tv.data;

import android.os.Parcel;
import android.os.Parcelable;

public class RadarPoints {

    public int pcnt;
    public RadarPoint[] points;

    public static class RadarPoint {
        public RadarVideo[] movie;
    }

    public static class RadarVideo implements Parcelable{
        public int id;
        public String title;
        public int bitrate;
        public String size;
        public String url;


        public static final Parcelable.Creator<RadarVideo> CREATOR = new Creator<RadarVideo>() {

            @Override
            public RadarVideo createFromParcel(Parcel source) {
                RadarVideo rv = new RadarVideo();
                rv.id = source.readInt();
                rv.title = source.readString();
                rv.bitrate = source.readInt();
                rv.size = source.readString();
                rv.url = source.readString();
                return rv;
            }

            @Override
            public RadarVideo[] newArray(int size) {
                return new RadarVideo[size];
            }
        };
        @Override
        public int describeContents() {
            return 0;
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeString(title);
            dest.writeInt(bitrate);
            dest.writeString(size);
            dest.writeString(url);
        }
    }

}
