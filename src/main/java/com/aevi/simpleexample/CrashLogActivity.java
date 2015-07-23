/*
 * This sample code is a preliminary draft for illustrative purposes only and not subject to any license granted by Wincor Nixdorf.
 * The sample code is provided "as is" and Wincor Nixdorf assumes no responsibility for errors or omissions of any kind out of the
 * use of such code by any third party. 
 */
package com.aevi.simpleexample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.aevi.configuration.TerminalConfiguration;

/**
 * This activity initializes a customer ACRA crash log sender that uses the CrashLogService to
 * store the data collected during the crash.
 * <p/>
 * Please note: The crash log handler is register at the Application level,
 * please take a look at the SampleApplication.java file to see how it is done.
 */
public class CrashLogActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crashlog);
        checkStatus();
    }

    /**
     * Throw an exception to test the Crash Log Service
     *
     * @param view the clicked view
     */
    public void throwExceptionClick(View view) {
        throw new IllegalStateException("deliberate exception thrown to test the Crash Log Service");
    }

    private void checkStatus() {
        TerminalConfiguration terminalConfiguration = TerminalConfiguration.getTerminalConfiguration(this);
        ButtonUtil.updateButton((Button) findViewById(R.id.throw_exception_crashlog_button), terminalConfiguration.isCrashLogApiUsable());
    }

}
