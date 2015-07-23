package com.aevi.simpleexample;

import android.widget.Button;

public class ButtonUtil {

    private ButtonUtil() {
    }


    public static void updateButton(Button button, boolean paymentApiUsable) {

        if (paymentApiUsable) {
            button.setBackgroundResource(R.drawable.button_style);
        } else {
            button.setBackgroundResource(R.drawable.button_style2);
        }
    }

}
