/*
 * This sample code is a preliminary draft for illustrative purposes only and not subject to any license granted by Wincor Nixdorf.
 * The sample code is provided "as is" and Wincor Nixdorf assumes no responsibility for errors or omissions of any kind out of the
 * use of such code by any third party. 
 */
package com.aevi.simpleexample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.aevi.helpers.services.AeviServiceConnectionCallback;
import com.aevi.systembar.SystemBarService;
import com.aevi.systembar.SystemBarServiceProvider;

public class SystemBarActivity extends Activity {

    private SystemBarServiceProvider serviceProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Prevents the Navigation Buttons from briefly appearing if a second Activity is started
        savedInstanceState = SystemBarService.disableRestore(savedInstanceState);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_systembar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        serviceProvider = new SystemBarServiceProvider(this);
    }

    @Override
    protected void onPause() {
        // close all services and remove listeners
        serviceProvider.close();
        serviceProvider = null;
        super.onPause();
    }

    private void changeHomeButtonVisibility(final boolean visible) {
        serviceProvider.connect(new AeviServiceConnectionCallback<SystemBarService>() {
            @Override
            public void onConnect(SystemBarService service) {
                service.setHomeButtonVisibility(visible);
            }
        });
    }

    private void changeBackButtonVisibility(final boolean visible) {
        serviceProvider.connect(new AeviServiceConnectionCallback<SystemBarService>() {
            @Override
            public void onConnect(SystemBarService service) {
                service.setBackButtonVisibility(visible);
            }
        });
    }

    private void changeRecentAppsButtonVisibility(final boolean visible) {
        serviceProvider.connect(new AeviServiceConnectionCallback<SystemBarService>() {
            @Override
            public void onConnect(SystemBarService service) {
                service.setRecentAppsButtonVisibility(visible);
            }
        });
    }

    public void showHomeButton(View view) {
        changeHomeButtonVisibility(true);
    }

    public void hideHomeButton(View view) {
        changeHomeButtonVisibility(false);
    }

    public void showBackButton(View view) {
        changeBackButtonVisibility(true);
    }

    public void hideBackButton(View view) {
        changeBackButtonVisibility(false);
    }

    public void showRecentAppsButton(View view) {
        changeRecentAppsButtonVisibility(true);
    }

    public void hideRecentAppsButton(View view) {
        changeRecentAppsButtonVisibility(false);
    }

}
