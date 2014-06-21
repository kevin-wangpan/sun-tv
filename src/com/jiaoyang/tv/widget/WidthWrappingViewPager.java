package com.jiaoyang.tv.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class WidthWrappingViewPager extends ViewPager {
    public WidthWrappingViewPager(Context context) {
        super(context);
    }

    public WidthWrappingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        boolean wrapWidth = MeasureSpec.getMode(widthMeasureSpec)
                == MeasureSpec.AT_MOST;

        if (wrapWidth) {
            /**
             * The first super.onMeasure call made the pager take up all the available height. Since we really wanted to
             * wrap it, we need to remeasure it. Luckily, after that call the first child is now available. So, we take
             * the height from it.
             */

            int width = getMeasuredWidth(), height = getMeasuredHeight();

            // Use the previously measured width but simplify the calculations
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

            /*
             * If the pager actually has any children, take the first child's height and call that our own
             */
            if (getChildCount() > 0) {
                View firstChild = getChildAt(0);

                /*
                 * The child was previously measured with exactly the full height. Allow it to wrap this time around.
                 */
                firstChild.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
                        heightMeasureSpec);

                width = firstChild.getMeasuredWidth();
            }

            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
