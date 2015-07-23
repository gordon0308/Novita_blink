/*
 * This sample code is a preliminary draft for illustrative purposes only and not subject to any license granted by Wincor Nixdorf.
 * The sample code is provided "as is" and Wincor Nixdorf assumes no responsibility for errors or omissions of any kind out of the
 * use of such code by any third party.
 */
package com.aevi.simpleexample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.aevi.helpers.services.AeviServiceConnectionCallback;
import com.aevi.helpers.services.AeviServiceException;
import com.aevi.printing.PrintService;
import com.aevi.printing.PrintServiceProvider;
import com.aevi.printing.model.PrintPayload;

public class PrintPreviewActivity extends Activity {

    private static final String TAG = PrintPreviewActivity.class.getSimpleName();

    private PrintServiceProvider serviceProvider;

    private PrintPayload payload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_preview);
        payload = getIntent().getParcelableExtra("payload");
    }

    @Override
    protected void onResume() {
        super.onResume();
        serviceProvider = new PrintServiceProvider(this);
        preview();
    }

    @Override
    protected void onPause() {
        serviceProvider.close();
        serviceProvider = null;
        super.onPause();
    }

    private void preview() {
        try {
            // send the request to the print service
            serviceProvider.connect(new AeviServiceConnectionCallback<PrintService>() {
                @Override
                public void onConnect(PrintService service) {
                    ImageView printPreview = (ImageView) findViewById(R.id.printPreviewImage);
                    Bitmap preview = service.preview(payload, service.getDefaultPrinterSettings());
                    printPreview.setImageBitmap(preview);
                }
            });

        } catch (AeviServiceException e) {
            Log.e(TAG, "Received exception when querying available printers : ", e);
            Toast.makeText(PrintPreviewActivity.this, "Received exception when printing : " + e, Toast.LENGTH_LONG).show();
        }
    }


}