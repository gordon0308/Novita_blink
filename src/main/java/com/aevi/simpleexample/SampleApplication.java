/*
 * This sample code is a preliminary draft for illustrative purposes only and not subject to any license granted by Wincor Nixdorf.
 * The sample code is provided "as is" and Wincor Nixdorf assumes no responsibility for errors or omissions of any kind out of the
 * use of such code by any third party.
 */
package com.aevi.simpleexample;

import android.app.Application;
import android.util.Log;

import com.aevi.helpers.CompatibilityException;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.sql.*;

/**
 * Main entry point for the Aevi sample application
 */
@ReportsCrashes(
        // This is required for backward compatibility but not used
        formKey = "",
        // when a crash occurs, the user is shown a Toast dialog
        mode = ReportingInteractionMode.TOAST,
        // toast text to display to the user when the app crashes.
        resToastText = R.string.crash_toast_text
)
public class SampleApplication extends Application {

    private static final String TAG = SampleApplication.class.getSimpleName();
    private CrashLogServiceSender crashLogServiceSender;
    Connection data_connect = null;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            crashLogServiceSender = new CrashLogServiceSender(this);

            Log.i(TAG, "Initializing ACRA");

            ACRA.init(this);

            // Setup ACRA with a custom sender
            ACRA.getErrorReporter().setReportSender(crashLogServiceSender);
        } catch(CompatibilityException e) {
            // Ignoring this exception here in sample as it will be displayed to the user later.
            // Production code would exit the application here
        }
    }

}
