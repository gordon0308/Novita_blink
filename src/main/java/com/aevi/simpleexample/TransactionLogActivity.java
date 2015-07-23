/*
 * This sample code is a preliminary draft for illustrative purposes only and not subject to any license granted by Wincor Nixdorf.
 * The sample code is provided "as is" and Wincor Nixdorf assumes no responsibility for errors or omissions of any kind out of the
 * use of such code by any third party. 
 */
package com.aevi.simpleexample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.aevi.configuration.TerminalConfiguration;
import com.aevi.helpers.services.AeviServiceConnectionCallback;
import com.aevi.transactionlog.TransactionLogService;
import com.aevi.transactionlog.TransactionLogServiceProvider;

public class TransactionLogActivity extends Activity {

    private static final String TAG = TransactionLogActivity.class.getSimpleName();

    private TransactionLogServiceProvider serviceProvider;

    private EditText transactionId;
    private EditText transactionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialise UI
        setContentView(R.layout.activity_transactionlog);
        transactionId = (EditText) findViewById(R.id.transactionId);
        transactionText = (EditText) findViewById(R.id.transactionText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        serviceProvider = new TransactionLogServiceProvider(this);
    }

    @Override
    protected void onPause() {
        serviceProvider.close();
        serviceProvider = null;
        super.onPause();
    }


    public void logTransactionClick(View view) {
        final String id = transactionId.getText().toString();
        final String text = transactionText.getText().toString();

        if (id.isEmpty() || text.isEmpty()) {
            return;
        }

        hideTheSoftKeyboard();

        if (!validateApiIsUsable()) {
            return;
        }

        serviceProvider.connect(new AeviServiceConnectionCallback<TransactionLogService>() {
            @Override
            public void onConnect(TransactionLogService service) {
                try {
                    service.log(id, text);
                    Toast toast = Toast.makeText(TransactionLogActivity.this, "Transaction log sent: " + id + ", " + text, Toast.LENGTH_LONG);
                    toast.show();
                } catch (RemoteException e) {
                    Log.e(TAG, "Error while trying to write to the transaction log", e);
                    Toast toast = Toast.makeText(TransactionLogActivity.this, "ERROR: Transaction log failed", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
    }

    private void hideTheSoftKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private boolean validateApiIsUsable() {
        TerminalConfiguration terminalConfiguration = TerminalConfiguration.getTerminalConfiguration(this);
        if (!terminalConfiguration.isTransactionLogApiUsable()) {
            Toast.makeText(this, "TransactionLog API does not work. Reason : " + terminalConfiguration.getTransactionLogApiStatus(), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}
