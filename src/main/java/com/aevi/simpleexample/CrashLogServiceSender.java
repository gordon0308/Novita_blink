/*
 * This sample code is a preliminary draft for illustrative purposes only and not subject to any license granted by Wincor Nixdorf.
 * The sample code is provided "as is" and Wincor Nixdorf assumes no responsibility for errors or omissions of any kind out of the
 * use of such code by any third party.
 */
package com.aevi.simpleexample;

import android.content.Context;
import android.util.Log;

import com.aevi.configuration.TerminalConfiguration;
import com.aevi.crashlog.CrashLogService;
import com.aevi.crashlog.CrashLogServiceProvider;
import com.aevi.helpers.ServiceState;
import com.aevi.helpers.services.AeviServiceConnectionCallback;

import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.util.JSONReportBuilder;

/**
 * This ACRA report sender transforms the information gathered during
 * a crash to a JSON string, connects to the Crash Log Service using the given provider
 * and sends the crash log data as a string to the back end system.
 */
public class CrashLogServiceSender implements ReportSender {

    private static final String TAG = CrashLogServiceSender.class.getSimpleName();

    private CrashLogService service;

    public CrashLogServiceSender(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("serviceProvider must not be null");
        }

        if (TerminalConfiguration.isTerminalServicesInstalled(context) == false) {
            Log.e(TAG, "Terminal Services are not installed.");
            return;
        }

        if (TerminalConfiguration.getServiceStatus(CrashLogService.class, context) != ServiceState.AVAILABLE) {
            Log.e(TAG, "Crash Log Services are not available.");
            return;
        }

        CrashLogServiceProvider serviceProvider = new CrashLogServiceProvider(context);
        // Obtains a reference to the Crash Log Service
        serviceProvider.connect(new AeviServiceConnectionCallback<CrashLogService>() {

            @Override
            public void onConnect(CrashLogService service) {
                CrashLogServiceSender.this.service = service;
            }
        });
    }

    @Override
    public void send(final CrashReportData report) {
        Log.i(TAG, "Preparing to send crash data to the Crash Log Service");
        if (service != null) {
            sendImpl(report);
        }
    }

    private void sendImpl(CrashReportData report) {
        try {
            Log.i(TAG, "Sending crash data to the Crash Log Service");
            service.send(report.toJSON().toString());
        } catch (JSONReportBuilder.JSONReportException e) {
            Log.e(TAG, "Failed creating JSON report out of crash log data.", e);
        }
    }
}
