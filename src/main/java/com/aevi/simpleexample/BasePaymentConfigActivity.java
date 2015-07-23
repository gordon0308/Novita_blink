package com.aevi.simpleexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;

import com.aevi.helpers.CompatibilityException;
import com.aevi.payment.PaymentAppConfiguration;
import com.aevi.payment.PaymentAppConfigurationRequest;

public abstract class BasePaymentConfigActivity extends Activity {

    protected static final int PAYMENT_CONFIGURATION = 1001;
    private static final String TAG = BasePaymentConfigActivity.class.getSimpleName();


    @Override
    protected void onStart() {
        super.onStart();
        fetchPaymentAppConfiguration();
    }

    /**
     * Fetch the Payment Configuration
     */
    private void fetchPaymentAppConfiguration() {
        startActivityForResult(PaymentAppConfigurationRequest.createIntent(), PAYMENT_CONFIGURATION);
    }

     @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                PaymentAppConfiguration paymentConfiguration = PaymentAppConfiguration.fromIntent(data);
                onPaymentConfigurationResults(paymentConfiguration);
            } catch(CompatibilityException e) {
                showExitDialog(e.getMessage() + "\nActivity will now exit.");
            }
        }
     }

    private void showExitDialog(String messageStr) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        TextView textView = new TextView(this);
        textView.setText(messageStr);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(textView);
        builder.setPositiveButton("Ok", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    abstract void onPaymentConfigurationResults(PaymentAppConfiguration paymentAppConfiguration);

}
