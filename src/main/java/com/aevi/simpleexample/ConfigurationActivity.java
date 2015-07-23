/*
 * This sample code is a preliminary draft for illustrative purposes only and not subject to any license granted by Wincor Nixdorf.
 * The sample code is provided "as is" and Wincor Nixdorf assumes no responsibility for errors or omissions of any kind out of the
 * use of such code by any third party. 
 */
package com.aevi.simpleexample;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.aevi.configuration.TerminalConfiguration;
import com.aevi.helpers.SdkLibrary;
import com.aevi.payment.MerchantInfo;
import com.aevi.payment.PaymentAppConfiguration;
import com.aevi.payment.TransactionType;

import java.util.Currency;

/**
 * This activity shows the configuration options of the installed payment application.
 * This includes information such as vendor, vendor version, API version, and supported currencies.
 */
public class ConfigurationActivity extends BasePaymentConfigActivity {

    private static final String TAG = ConfigurationActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout
        setContentView(R.layout.activity_configuration);
    }

    @Override
    void onPaymentConfigurationResults(PaymentAppConfiguration paymentConfiguration) {
        if (paymentConfiguration == null) {
            Log.e(TAG, "Failed to obtain PaymentAppConfiguration");
            setResult(Activity.RESULT_CANCELED, null);
            finish();
            return;
        }

        // Get the payment application configuration information.
        TerminalConfiguration terminalConfiguration = getTerminalConfiguration();
        if (terminalConfiguration == null) {
            Log.e(TAG, "Failed to obtain TerminalConfiguration");
            setResult(Activity.RESULT_CANCELED, null);
            finish();
            return;
        }

        // Payment application vendor
        setText(R.id.paymentApiVendor, paymentConfiguration.getVendor());

        // Payment application version
        setText(R.id.paymentApiVersion, paymentConfiguration.getVersion());

        // Payment application state
        setText(R.id.paymentApiAppState, paymentConfiguration.getApplicationState().toString());

        // Integrated mode
        setText(R.id.paymentApiIntegratedMode, paymentConfiguration.isIntegratedMode() ? getString(R.string.text_on) : getString(R.string.text_off));

        // Supported currencies
        setText(R.id.paymentApiCurrencies, buildCurrenciesString(paymentConfiguration.getSupportedCurrencies()));

        // Default currency
        Currency defaultCurrency = paymentConfiguration.getDefaultCurrency();
        setText(R.id.paymentApiDefaultCurrency, defaultCurrency != null ? buildCurrencyString(defaultCurrency) : "");

        // Supported merchants
        setText(R.id.paymentApiMerchants, buildMerchantsString(paymentConfiguration.getMerchants()));

        // Supported transaction types
        setText(R.id.paymentApiTransactionTypes, buildTransactionTypesString(paymentConfiguration.getSupportedTransactionTypes()));

        // Supported transaction types
        setText(R.id.paymentApiCombinedReceipts, "" + paymentConfiguration.canPrintCombinedReceipt());

        // Supported transaction types
        setText(R.id.accessibilityModeSupported, "" + paymentConfiguration.hasAccessibilityMode());

        // Terminal Configuration
        setText(R.id.terminalApiVersion, terminalConfiguration.getVersion());

        setText(R.id.terminalApiId, terminalConfiguration.getTerminalId());

        setText(R.id.terminalApiIsSimulator, "" + terminalConfiguration.isSimulator());

        setText(R.id.terminalApiIsAeviDevice, "" + terminalConfiguration.isAeviDevice());

        // Client Version
        try {
            String versionNumber = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            setText(R.id.clientAppVersion, "" + versionNumber);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed reading the version number from the Android manifest file");
        }

        // SDK Version
        setText(R.id.clientApiVersion, "" + SdkLibrary.API_VERSION);

    }

    private void setText(int id, String text) {
        ((TextView) findViewById(id)).setText(text);
    }

    private TerminalConfiguration getTerminalConfiguration() {
        // Get the payment application configuration information.
        TerminalConfiguration configuration = null;
        try {
            configuration = TerminalConfiguration.getTerminalConfiguration(this);
        } catch (Exception e) {
            Log.e(TAG, "Encountered exception while trying to obtain the terminal configuration ", e);
        }
        return configuration;
    }

    private String buildMerchantsString(MerchantInfo[] merchants) {
        if (merchants.length == 0) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder(merchants[0].getId() + ": " + merchants[0].getName());
            for (int i = 1; i < merchants.length; i++) {
                builder.append("\n");
                builder.append(merchants[i].getId() + ": " + merchants[i].getName());
            }
            return builder.toString();
        }
    }

    private String buildTransactionTypesString(TransactionType[] transactionTypes) {
        if (transactionTypes.length == 0) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder(transactionTypes[0].toString());
            for (int i = 1; i < transactionTypes.length; i++) {
                builder.append("\n");
                builder.append(transactionTypes[i]);
            }
            return builder.toString();
        }
    }

    // Build a comma-separated list of currencies. For each currency, the ISO-4217 code and symbol is shown.
    private String buildCurrenciesString(Currency[] currencies) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < currencies.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(buildCurrencyString(currencies[i]));
        }

        return builder.toString();
    }

    // build a human readable currency name from the given currency
    private String buildCurrencyString(Currency currency) {
        return String.format(" (%s)", currency.getSymbol());
    }

    // Event handler for the 'Ok' button.
    public void onOkClick(View view) {
        close();
    }

    private void close() {
        setResult(Activity.RESULT_OK, null);
        // Return to the previous Activity.
        finish();
    }

    @Override
    public void onBackPressed() {
        close();
    }
}
