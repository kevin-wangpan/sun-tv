package com.jiaoyang.tv.data;

import java.io.Serializable;

public class Filter implements Serializable {

    private static final long serialVersionUID = 1L;

    public static class NamedValue {
        public String value;
        public String label;
        public boolean defaults;
    }

    public String name;
    public String label;
    public NamedValue[] values;

    private transient int mCurrentIndex = -1;

    public void setCurrentIndex(int index) {
        mCurrentIndex = index;
    }

    public NamedValue getValue() {
        return mCurrentIndex != -1 ? values[mCurrentIndex] : getDefaultValue();
    }

    public int getCurrentIndex() {
        return mCurrentIndex != -1 ? mCurrentIndex : getDefaultIndex();
    }

    public NamedValue getDefaultValue() {
        int defaultIndex = getDefaultIndex();

        return defaultIndex != -1 ? values[defaultIndex] : null;
    }

    public int getDefaultIndex() {
        int result = -1;

        for (int i = 0; i < values.length; i++) {
            if (values[i].defaults) {
                result = i;
                break;
            }
        }

        return result;
    }
}
