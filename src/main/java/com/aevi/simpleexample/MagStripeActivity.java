/*
 * This sample code is a preliminary draft for illustrative purposes only and not subject to any license granted by Wincor Nixdorf.
 * The sample code is provided "as is" and Wincor Nixdorf assumes no responsibility for errors or omissions of any kind out of the
 * use of such code by any third party.
 */
package com.aevi.simpleexample;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.aevi.configuration.TerminalConfiguration;
import com.aevi.helpers.services.AeviServiceConnectionCallback;
import com.aevi.magstripe.MagStripeError;
import com.aevi.magstripe.MagStripeService;
import com.aevi.magstripe.MagStripeServiceProvider;
import com.aevi.magstripe.MagStripeTrack;
import com.aevi.magstripe.MagStripeTracks;
import com.aevi.magstripe.OnSwipeCallback;

import java.util.ArrayList;
import java.util.List;

public class MagStripeActivity extends Activity {

    private static final String TAG = MagStripeActivity.class.getSimpleName();
    private List<String> consoleLines = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private MagStripeServiceProvider serviceProvider;
    private MagStripeService magStripeService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_mag_stripe);
        ListView console = (ListView) findViewById(R.id.consoleView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, consoleLines);
        console.setAdapter(adapter);
        checkStatus();
        displayText("Press 'Read Card' to activate reader");
        final Button btn = (Button) findViewById(R.id.test_load);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MagStripeActivity.this, LoadingNdisActivity.class));

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        serviceProvider = new MagStripeServiceProvider(this);
        serviceProvider.connect(
                new AeviServiceConnectionCallback<MagStripeService>() {
                    @Override
                    public void onConnect(final MagStripeService service) {
                        magStripeService = service;
                        findViewById(R.id.magstripe_read_card).setEnabled(true);
                    }
                }
        );
    }

    @Override
    protected void onPause() {
        cancelSwipe();
        serviceProvider.close();
        serviceProvider = null;
        magStripeService = null;
        super.onPause();
    }

    private void clearConsoleLines() {
        consoleLines.clear();
        adapter.notifyDataSetChanged();
    }

    public void readCardClick(View view) {
        if (magStripeService != null) {
            findViewById(R.id.magstripe_read_card).setEnabled(false);
            getCardInfo(magStripeService);
        }
    }

    public void cancelCardClick(View view) {
        cancelSwipe();
    }

    private void cancelSwipe() {
        serviceProvider.connect(
                new AeviServiceConnectionCallback<MagStripeService>() {
                    @Override
                    public void onConnect(MagStripeService service) {
                        try {
                            service.cancel();
                        } catch (Exception e) {
                            Log.e(TAG, "Unexpected exception while canceling a card swipe request", e);
                            displayText("Unexpected exception" + e.getMessage());
                        }
                    }
                }
        );
    }

    private void getCardInfo(MagStripeService service) {
        clearConsoleLines();
        try {
            displayText("Please swipe the card...");
            service.swipe(new OnSwipeCallback() {
                @Override
                public void onSuccess(MagStripeTracks result) {
                    findViewById(R.id.magstripe_read_card).setEnabled(true);
                    clearConsoleLines();
                    displayTextResponse("1", result.getTrack1());
                    displayTextResponse("2", result.getTrack2());
                    displayTextResponse("3", result.getTrack3());
                    //medicare card

                    //NDIS card
                }

                @Override
                public void onFailure(int errorCode) {
                    findViewById(R.id.magstripe_read_card).setEnabled(true);
                    clearConsoleLines();

                    displayText("ERROR: " + parseError(errorCode));
                }
            });

        } catch (Exception e) {
            clearConsoleLines();
            Log.e(TAG, "Unexpected exception while reading card info", e);
            displayText("Unexpected exception" + e.getMessage());
        }
    }

    private String parseError(int errorCode) {
        switch (errorCode) {
            case MagStripeError.CANNOT_CLAIM_INTERFACE:
                return "Not authorised to read the USB device";
            case MagStripeError.MAG_STRIPE_HAS_NO_DATA:
                return "Swipe cancelled";
            case MagStripeError.MAG_STRIPE_FORBIDDEN:
                return "Reading a payment card is not allowed";
            case MagStripeError.MAG_STRIPE_READER_NOT_FOUND:
                return "No mag swipe reader device can be detected";
            default:
                return "Unexpected error";
        }
    }

    private void displayTextResponse(String label, MagStripeTrack response) {

        if (response == null || !response.isValid()) {
            consoleLines.add("Track " + label + " Decode Status: False");

        } else {

            consoleLines.add("Track " + label + " Data Length: " + response.getData().length + " bytes");
            consoleLines.add("Track " + label + " Data: " + new String(response.getData()));
        }

        adapter.notifyDataSetChanged();
    }

    private void displayText(String message) {
        consoleLines.add(message);
        adapter.notifyDataSetChanged();
    }

    private void checkStatus() {
        TerminalConfiguration terminalConfiguration = TerminalConfiguration.getTerminalConfiguration(this);
        ButtonUtil.updateButton((Button) findViewById(R.id.magstripe_read_card), terminalConfiguration.isMagStripeApiUsable());
        ButtonUtil.updateButton((Button) findViewById(R.id.magstripe_cancel), terminalConfiguration.isMagStripeApiUsable());
    }

}
