/*
 * This sample code is a preliminary draft for illustrative purposes only and not subject to any license granted by Wincor Nixdorf.
 * The sample code is provided "as is" and Wincor Nixdorf assumes no responsibility for errors or omissions of any kind out of the
 * use of such code by any third party. 
 */
package com.aevi.simpleexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aevi.configuration.TerminalConfiguration;
import com.aevi.crashlog.CrashLogService;
import com.aevi.helpers.CompatibilityException;
import com.aevi.helpers.ServiceState;
import com.aevi.magstripe.MagStripeService;
import com.aevi.payment.PaymentAppConfiguration;
import com.aevi.preferences.SharedPreferencesService;
import com.aevi.printing.PrintService;
import com.aevi.smartcard.SmartCardService;
import com.aevi.transactionlog.TransactionLogService;

/**
 * Main entry point of the sample application. Used to jump to the various
 * sample sections.
 */
public class SampleActivity extends Activity {

    private static final String TAG = SampleActivity.class.getSimpleName();

    private IntegrationModeReceiver intModeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialise UI
        setContentView(R.layout.activity_start);
    }

    @Override
    protected void onResume() {
        super.onResume();

        assureInstalledComponents();
    }

    private void assureInstalledComponents() {

        try {
            if (TerminalConfiguration.isTerminalServicesInstalled(this) == false) {
                showExitDialog("Terminal Services is not installed or installed incorrectly.\nThis application will now exit.");
                return;
            }
        } catch(CompatibilityException e) {
            showExitDialog(e.getMessage() + "\nThis application will now exit.");
            return;
        }

        // Ensure the payment application/simulator is installed. If not, present an alert dialog
        // to the user and exit.
        ServiceState paymentAppState = PaymentAppConfiguration.getPaymentApplicationStatus(this);
        Log.d(TAG, "Payment App State: " + paymentAppState);


        if (paymentAppState == ServiceState.NOT_INSTALLED) {
            showExitDialog("A payment application is not installed.\nThis application will now exit.");
            return;
        }
        if (paymentAppState == ServiceState.NO_PERMISSION) {
            showExitDialog("The Device and Payment App Services must be installed before this Application.\nPlease uninstall and re-install the Application.\nThis application will now exit.");
            return;
        }
        if (paymentAppState == ServiceState.UNAVAILABLE) {
            showExitDialog("The payment application is unavailable.\nThis application will now exit.");
            return;
        }



        // Ensure the printing service is installed. If not, present an alert
        // dialog to the user and exit.
        if (TerminalConfiguration.getServiceStatus(PrintService.class, this) != ServiceState.AVAILABLE) {
            showComponentNotInstalledDialog("Print service");
        }

        // Ensure the crash log service is installed. If not, present an alert
        // dialog to the user and exit.
        else if (TerminalConfiguration.getServiceStatus(CrashLogService.class, this) != ServiceState.AVAILABLE) {
            showComponentNotInstalledDialog("Crash log service");
        }

        // Ensure the transaction log service is installed. If not, present an alert
        // dialog to the user and exit.
        else if (TerminalConfiguration.getServiceStatus(TransactionLogService.class, this) != ServiceState.AVAILABLE) {
            showComponentNotInstalledDialog("Transaction log service");
        }

        // Ensure the shared preferences service is installed. If not, present an alert
        // dialog to the user and exit.
        else if (TerminalConfiguration.getServiceStatus(SharedPreferencesService.class, this) != ServiceState.AVAILABLE) {
            showComponentNotInstalledDialog("Shared Preferences service");
        }

        // Ensure the shared preferences service is installed. If not, present an alert
        // dialog to the user and exit.
        else if (TerminalConfiguration.getServiceStatus(SmartCardService.class, this) != ServiceState.AVAILABLE) {
            showComponentNotInstalledDialog("Smart card service");
        }

        // Ensure the shared preferences service is installed. If not, present an alert
        // dialog to the user and exit.
        else if (TerminalConfiguration.getServiceStatus(MagStripeService.class, this) != ServiceState.AVAILABLE) {
            showComponentNotInstalledDialog("Mag stripe service");
        } else {
            try {
                checkStatus();
            } catch (CompatibilityException e) {
                showExitDialog("The implementation version of the Aevi API that is installed is incompatible with the runtime version. " +
                        "Please update the Simulator or use an older version of the AeviSample.");
            }
        }
    }


    private void showComponentNotInstalledDialog(String componentName) {
        showExitDialog(componentName + " is not installed.\nThis application will now exit.");
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


    /**
     * Show the payment examples
     *
     * @param view the clicked view
     */
    public void paymentSamplesClick(View view) {
        // Start the PaymentActivity
        Intent startIntent = new Intent(this, PaymentActivity.class);
        startActivity(startIntent);
    }

    /**
     * Show the payment examples activity
     *
     * @param view the clicked view
     */
    public void printingSamplesClick(View view) {
        // Start the PrintingActivity
        Intent startIntent = new Intent(this, PrintingActivity.class);
        startActivity(startIntent);
    }

    /**
     * Show the configuration examples activity
     *
     * @param view the clicked view
     */
    public void configurationSamplesClick(View view) {
        // Start the ConfigurationActivity
        Intent startIntent = new Intent(this, ConfigurationActivity.class);
        startActivity(startIntent);
    }

    /**
     * Show the crash log examples activity
     *
     * @param view the clicked view
     */
    public void crashLogSampleClick(View view) {
        // Start the CrashLogActivity
        Intent startIntent = new Intent(this, CrashLogActivity.class);
        startActivity(startIntent);
    }

    /**
     * Show the transaction log examples activity
     *
     * @param view the clicked view
     */
    public void transactionLogSampleClick(View view) {
        // Start the TransactionLogActivity
        Intent startIntent = new Intent(this, TransactionLogActivity.class);
        startActivity(startIntent);
    }

    /**
     * Show the shared preferences examples activity
     *
     * @param view the clicked view
     */
    public void sharedPreferencesSampleClick(View view) {
        // Start the SharedPreferencesActivity
        Intent startIntent = new Intent(this, SharedPreferencesActivity.class);
        startActivity(startIntent);
    }

    /**
     * Show the user authentication examples activity
     *
     * @param view the clicked view
     */
    public void authenticationSampleClick(View view) {
        // Start the AuthenticationActivity
        Intent startIntent = new Intent(this, AuthenticationActivity.class);
        startActivity(startIntent);
    }

    /**
     * Show the smart card example
     *
     * @param view the clicked view
     */
    public void smartCardClick(View view) {
        startActivity(new Intent(this, SmartCardActivity.class));
    }

    /**
     * Show the mag stripe example
     *
     * @param view the clicked view
     */
    public void magStripeClick(View view) {
        startActivity(new Intent(this, MagStripeActivity.class));
    }

    /**
     * Show the system bar example
     *
     * @param view the clicked view
     */
    public void systemBarClick(View view) {
        startActivity(new Intent(this, SystemBarActivity.class));
    }

    /**
     * Show the email example
     *
     * @param view the clicked view
     */
    public void emailClick(View view) {
        startActivity(new Intent(this, MailActivity.class));
    }

    /**
     * Show the Secure Communications example
     *
     * @param view the clicked view
     */
    public void secureCommunicationsClick(View view) {
        startActivity(new Intent(this, SecureCommunicationsActivity.class));
    }

    /**
     * Show the Compatibility example
     *
     * @param view the clicked view
     */
    public void platformCompatibilityClick(View view) {
        startActivity(new Intent(this, PlatformCompatibilityActivity.class));
    }



    private void checkStatus() {
        TerminalConfiguration terminalConfiguration = TerminalConfiguration.getTerminalConfiguration(this);

        if (terminalConfiguration != null) {
            ButtonUtil.updateButton((Button) findViewById(R.id.menu_button_printing), terminalConfiguration.isPrintApiUsable());

        }
    }

}
