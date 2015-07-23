/*
 * This sample code is a preliminary draft for illustrative purposes only and not subject to any license granted by Wincor Nixdorf.
 * The sample code is provided "as is" and Wincor Nixdorf assumes no responsibility for errors or omissions of any kind out of the
 * use of such code by any third party.
 */
package com.aevi.simpleexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.aevi.payment.PaymentAppConfiguration;

public class IntegrationModeReceiver extends BroadcastReceiver {

    public static final String TAG = IntegrationModeReceiver.class.getSimpleName();

    private static Toast toastMessage;

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean intMode = intent.getBooleanExtra(PaymentAppConfiguration.EXTRA_INTEGRATED_MODE, false);
        Log.i(TAG, "Received integration mode broadcast event. Value: " + intMode);

        if(toastMessage != null) {
            toastMessage.cancel();
        }

        toastMessage = Toast.makeText(context, "Received integration mode broadcast event in AeviSample application. Value: " + intMode, Toast.LENGTH_LONG);
        toastMessage.show();
    }

}
