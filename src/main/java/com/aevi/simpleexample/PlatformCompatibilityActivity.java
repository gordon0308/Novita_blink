/*
 * This sample code is a preliminary draft for illustrative purposes only and not subject to any license granted by Wincor Nixdorf.
 * The sample code is provided "as is" and Wincor Nixdorf assumes no responsibility for errors or omissions of any kind out of the
 * use of such code by any third party. 
 */
package com.aevi.simpleexample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.aevi.configuration.TerminalConfiguration;
import com.aevi.helpers.ApiStatus;
import com.aevi.payment.PaymentAppConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This activity shows the platform compatibility and availability of the various elements of the AeviApi.
 * This includes information such as vendor, vendor version, API version, and supported currencies.
 */
public class PlatformCompatibilityActivity extends BasePaymentConfigActivity {

    private static final String TAG = PlatformCompatibilityActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout
        setContentView(R.layout.activity_compatibility);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Get the payment application configuration information.
        SampleApplication sampleApplication = (SampleApplication) getApplication();

        TerminalConfiguration terminalConfiguration = TerminalConfiguration.getTerminalConfiguration(this);
        if (terminalConfiguration != null) {
            GridView table = (GridView) findViewById(R.id.terminalTable);
            List<ViewDataElement> data = createTerminalConfigurationViewData(terminalConfiguration);
            table.setAdapter(new ViewDataAdapter(data));
        }

        if (terminalConfiguration == null) {
            setResult(Activity.RESULT_CANCELED, null);
            finish();
        }
    }

    @Override
    void onPaymentConfigurationResults(PaymentAppConfiguration paymentAppConfiguration) {
        if (paymentAppConfiguration != null) {
            GridView table = (GridView) findViewById(R.id.paymentAppTable);
            List<ViewDataElement> data = createPaymentApplicationViewData(paymentAppConfiguration);
            table.setAdapter(new ViewDataAdapter(data));
        }
    }

    private List<ViewDataElement> createTerminalConfigurationViewData(TerminalConfiguration terminalConfiguration) {
        List<ViewDataElement> result = new ArrayList<ViewDataElement>();
        result.add(new ViewDataElement("Printing", terminalConfiguration.isPrintApiUsable(), terminalConfiguration.getPrintApiStatus()));
        result.add(new ViewDataElement("Smart card API", terminalConfiguration.isSmartCardApiUsable(), terminalConfiguration.getSmartCardApiStatus()));
        result.add(new ViewDataElement("Magstripe card API", terminalConfiguration.isMagStripeApiUsable(), terminalConfiguration.getMagStripeApiStatus()));
        result.add(new ViewDataElement("LED API", terminalConfiguration.isLedApiUsable(), terminalConfiguration.getLedApiStatus()));
        result.add(new ViewDataElement("Authentication", terminalConfiguration.isAuthenticationApiUsable(), terminalConfiguration.getAuthenticationApiStatus()));
        result.add(new ViewDataElement("Email Api", terminalConfiguration.isMailApiUsable(), terminalConfiguration.getMailApiStatus()));
        result.add(new ViewDataElement("Crash log", terminalConfiguration.isCrashLogApiUsable(), terminalConfiguration.getCrashLogApiStatus()));
        result.add(new ViewDataElement("Transaction Log", terminalConfiguration.isTransactionLogApiUsable(), terminalConfiguration.getTransactionLogApiStatus()));
        result.add(new ViewDataElement("Shared Preferences", terminalConfiguration.isSharedPreferencesApiUsable(), terminalConfiguration.getSharedPreferencesApiStatus()));
        return result;
    }

    private List<ViewDataElement> createPaymentApplicationViewData(PaymentAppConfiguration paymentAppConfiguration) {
        List<ViewDataElement> result = new ArrayList<ViewDataElement>();
        result.add(new ViewDataElement("Payment Request", paymentAppConfiguration.isPaymentApiUsable(), paymentAppConfiguration.getPaymentApiStatus()));
        result.add(new ViewDataElement("Refund Request", paymentAppConfiguration.isRefundRequestApiUsable(), paymentAppConfiguration.getRefundRequestApiStatus()));
        result.add(new ViewDataElement("Deposit Request", paymentAppConfiguration.isDepositApiUsable(), paymentAppConfiguration.getDepositApiStatus()));
        result.add(new ViewDataElement("Moto Request", paymentAppConfiguration.isMoToRequestApiUsable(), paymentAppConfiguration.getMotoRequestApiStatus()));
        result.add(new ViewDataElement("Moto Refund Request", paymentAppConfiguration.isMoToRefundRequestApiUsable(), paymentAppConfiguration.getMotoRefundRequestApiStatus()));
        result.add(new ViewDataElement("Reversal", paymentAppConfiguration.isReversalApiUsable(), paymentAppConfiguration.getReversalApiStatus()));
        result.add(new ViewDataElement("Pre-auth / Completion", paymentAppConfiguration.isPreAuthorisationApiUsable(), paymentAppConfiguration.getPreAuthorisationApiStatus()));
        return result;
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

    static class ViewDataElement {
        private final String name;
        private final boolean compatible;
        private final Set<ApiStatus> statuses;


        ViewDataElement(String name, boolean compatible, Set<ApiStatus> statuses) {
            this.name = name;
            this.compatible = compatible;
            this.statuses = statuses;
        }

        public String getName() {
            return name;
        }

        public boolean isCompatible() {
            return compatible;
        }

        public Set<ApiStatus> getStatuses() {
            return statuses;
        }
    }

    static class ViewDataAdapter extends BaseAdapter {

        private List<ViewDataElement> viewDataElements;

        ViewDataAdapter(List<ViewDataElement> viewDataElements) {
            this.viewDataElements = viewDataElements;
        }

        @Override
        public int getCount() {
            return viewDataElements.size() * 3;
        }

        @Override
        public Object getItem(int position) {
            return viewDataElements.get(position / 3);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int row = position / 3;
            int column = position % 3;
            ViewDataElement element = viewDataElements.get(row);
            return getView(column, element, parent.getContext());
        }

        private View getView(int column, ViewDataElement element, Context context) {
            switch (column) {
                case 0:
                    TextView textView = new TextView(context);
                    textView.setText(element.getName());
                    return textView;
                case 1:
                    return okImage(element.isCompatible(), context);
                case 2:
                    TextView statusView = new TextView(context);
                    statusView.setText(toString(element.getStatuses()));
                    return statusView;
                default:
                    throw new IllegalArgumentException("Column number is invalid " + column);
            }
        }

        private String toString(Set<ApiStatus> statuses) {
            StringBuilder result = new StringBuilder();
            for (ApiStatus status : statuses) {
                result.append(status + " ");
            }

            return result.toString();
        }

        private ImageView okImage(boolean ok, Context context) {
            ImageView imageView = new ImageView(context);
            if (ok) {
                imageView.setImageResource(R.drawable.okicon);
            } else {
                imageView.setImageResource(R.drawable.nokicon);
            }
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            return imageView;
        }

    }

}
