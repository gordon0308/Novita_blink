/*
 * This sample code is a preliminary draft for illustrative purposes only and not subject to any license granted by Wincor Nixdorf.
 * The sample code is provided "as is" and Wincor Nixdorf assumes no responsibility for errors or omissions of any kind out of the
 * use of such code by any third party. 
 */
package com.aevi.simpleexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aevi.authentication.AuthenticationRequest;
import com.aevi.authentication.AuthenticationResult;
import com.aevi.authentication.Role;
import com.aevi.configuration.TerminalConfiguration;

/**
 * This activity sends an authentication request to the Aevi platform
 * when the user is successfully authenticated, the intent results holds the user role.
 * <p/>
 * In case the user is un-successfully authenticated, the resulting role is set to {com.aevi.authentication.Role.NONE}
 */
public class AuthenticationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialise UI
        setContentView(R.layout.activity_authentication);
    }

    public void authenticationRequestClick(View view) {
        if (!validateApiIsUsable()) {
            return;
        }

        AuthenticationRequest authenticationRequest = null;

        // identify the button pressed
        switch (view.getId()) {
            case R.id.menu_request_button_any_authentication: // doStuff
                // send the AuthenticationRequest to the Aevi platform
                authenticationRequest = new AuthenticationRequest();
                break;
            case R.id.menu_request_button_manager_authentication: // doStuff
                authenticationRequest = new AuthenticationRequest(Role.MANAGER);
                break;
            case R.id.menu_request_button_technician_authentication: // doStuff
                authenticationRequest = new AuthenticationRequest(Role.TECHNICIAN);
                break;
        }

        startActivityForResult(authenticationRequest.createIntent(), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        TextView textView = (TextView) findViewById(R.id.authenticationResultTextView);
        if (resultCode == Activity.RESULT_CANCELED) {
            textView.setText("Authentication Request Cancelled");
        } else if (resultCode == Activity.RESULT_OK) {
            // read the authentication result
            AuthenticationResult authenticationResult = AuthenticationResult.fromIntent(data);
            textView.setText("Authentication Role: " + authenticationResult.getRole().toString());
        } else {
            textView.setText("Result Code: " + resultCode);
        }
    }


    private boolean validateApiIsUsable() {
        TerminalConfiguration terminalConfiguration = TerminalConfiguration.getTerminalConfiguration(this);

        if (!terminalConfiguration.isAuthenticationApiUsable()) {
            Toast.makeText(this, "Authentication API does not work. Reason : " + terminalConfiguration.getAuthenticationApiStatus(), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}
