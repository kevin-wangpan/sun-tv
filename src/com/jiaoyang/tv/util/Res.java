package com.jiaoyang.tv.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Res {

    
    //*******************上下文为Activity*******************
    
    public static LinearLayout ll(Activity context, int id) {
        return (LinearLayout) context.findViewById(id);
    }
    
    public static RelativeLayout rl(Activity context, int id) {
        return (RelativeLayout) context.findViewById(id);
    }
    
    public static FrameLayout fl(Activity context, int id) {
        return (FrameLayout) context.findViewById(id);
    }
    
    public static GridView gv(Activity context, int id) {
        return (GridView) context.findViewById(id);
    }
    
    public static ImageView i(Activity context, int id) {
        return (ImageView) context.findViewById(id);
    }
    
    public static Button b(Activity context, int id) {
        return (Button) context.findViewById(id);
    }
    
    public static ImageButton ib(Activity context, int id) {
        return (ImageButton) context.findViewById(id);
    }
    
    public static CheckBox cb(Activity context, int id) {
        return (CheckBox) context.findViewById(id);
    }
    
    public static EditText e(Activity context, int id) {
        return (EditText) context.findViewById(id);
    }
    
    public static TextView t(Activity context, int id) {
        return (TextView) context.findViewById(id);
    }
    
    
    
    
  //*******************上下文为View*******************
    
    public static LinearLayout ll(View context, int id) {
        return (LinearLayout) context.findViewById(id);
    }
    
    public static RelativeLayout rl(View context, int id) {
        return (RelativeLayout) context.findViewById(id);
    }
    
    public static FrameLayout fl(View context, int id) {
        return (FrameLayout) context.findViewById(id);
    }
    
    public static GridView gv(View context, int id) {
        return (GridView) context.findViewById(id);
    }
    
    public static ImageView i(View context, int id) {
        return (ImageView) context.findViewById(id);
    }
    
    public static Button b(View context, int id) {
        return (Button) context.findViewById(id);
    }
    
    public static ImageButton ib(View context, int id) {
        return (ImageButton) context.findViewById(id);
    }
    
    public static CheckBox cb(View context, int id) {
        return (CheckBox) context.findViewById(id);
    }
    
    public static EditText e(View context, int id) {
        return (EditText) context.findViewById(id);
    }
    
    public static TextView t(View context, int id) {
        return (TextView) context.findViewById(id);
    }
    
    public static ProgressBar p(View context, int id){
        return (ProgressBar)context.findViewById(id);
    }
    
    public static int id(Context context, String name) {
        int id = 0;
        String pkg = context.getPackageName();
        Resources r = context.getResources();
        if(r != null)
            id = r.getIdentifier(name, "id", pkg);
        return id;
    }
    
}
