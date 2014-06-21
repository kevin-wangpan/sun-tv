package com.jiaoyang.tv.player;

public enum DisplayRatio {
    
    DISPLAY_RATIO_AUTO(0),DISPLAY_RATIO_ORIGIN(1),DISPLAY_RATIO_4TO3(2),DISPLAY_RATIO_16TO9(3),DISPLAY_RATIO_FULLSCREEN(4);
    
    private DisplayRatio(int flag) {
        mDisplayRatioFlag = flag;
    }
    
    private int mDisplayRatioFlag = 0;

    public int getmDisplayRatioFlag() {
        return mDisplayRatioFlag;
    }

    public void setmDisplayRatioFlag(int mDisplayRatioFlag) {
        this.mDisplayRatioFlag = mDisplayRatioFlag;
    }
    
}
