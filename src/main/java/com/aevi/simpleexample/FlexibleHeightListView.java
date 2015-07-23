/*
 * This sample code is a preliminary draft for illustrative purposes only and not subject to any license granted by Wincor Nixdorf.
 * The sample code is provided "as is" and Wincor Nixdorf assumes no responsibility for errors or omissions of any kind out of the
 * use of such code by any third party.
 */
package com.aevi.simpleexample;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ListView;

public class FlexibleHeightListView extends ListView {

    public FlexibleHeightListView(Context context) {
        super(context);
    }

    public FlexibleHeightListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlexibleHeightListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Calculate entire height by providing a very large height hint.
        // View.MEASURED_SIZE_MASK represents the largest height possible.
        int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }

}
