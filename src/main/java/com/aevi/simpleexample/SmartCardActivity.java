/*
 * This sample code is a preliminary draft for illustrative purposes only and not subject to any license granted by Wincor Nixdorf.
 * The sample code is provided "as is" and Wincor Nixdorf assumes no responsibility for errors or omissions of any kind out of the
 * use of such code by any third party.
 */
package com.aevi.simpleexample;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.aevi.configuration.TerminalConfiguration;
import com.aevi.helpers.HexUtils;
import com.aevi.helpers.StringUtils;
import com.aevi.helpers.services.AeviServiceConnectionCallback;
import com.aevi.smartcard.SmartCardService;
import com.aevi.smartcard.SmartCardServiceProvider;

import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardNotPresentException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class SmartCardActivity extends Activity {

    private static final int SW_NO_ERROR = 0x9000;

    private static final String TAG = SmartCardActivity.class.getSimpleName();
    private List<String> consoleLines = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private SmartCardServiceProvider serviceProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_smart_card);
        ListView console = (ListView) findViewById(R.id.consoleView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, consoleLines);
        console.setAdapter(adapter);
        checkStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        serviceProvider = new SmartCardServiceProvider(this);
    }

    @Override
    protected void onPause() {
        serviceProvider.close();
        serviceProvider = null;
        super.onPause();
    }

    public void clearClick(View view) {
        clearConsoleLines();
    }

    private void clearConsoleLines() {
        consoleLines.clear();
        adapter.notifyDataSetChanged();
    }

    public void readCardClick(View view) {

        serviceProvider.connect(
                new AeviServiceConnectionCallback<SmartCardService>() {
                    @Override
                    public void onConnect(final SmartCardService service) {
                        getCardInfo(service);
                    }
                }
        );
    }

    private void getCardInfo(SmartCardService service) {
        clearConsoleLines();

        String[] adpus = getResources().getStringArray(R.array.adpu_command_list);
        new ReadSmartCard(service).execute(adpus);
    }


    private class ReadSmartCard extends AsyncTask<String, String, String> {

        private SmartCardService service;

        ReadSmartCard(SmartCardService service) {
            this.service = service;
        }

        protected String doInBackground(String... apdus) {
            try {
                Card card = service.connect();
                byte[] atrBytes = card.getATR().getBytes();
                publishProgress("ATR: " + HexUtils.bytesToHexStr(atrBytes));
                CardChannel basicChannel = card.getBasicChannel();
                for (String apdu : apdus) {
                    ResponseAPDU response = basicChannel.transmit(createCommandAPDU(apdu));
                    publishProgress("ADPU: " + apdu);
                    if (response.getSW() != SW_NO_ERROR) {
                        publishProgress(String.format("FAILED RESPONSE SW1:%02X SW2:%02X", response.getSW1(), response.getSW2()));
                        break;
                    }

                    displayTextResponse(response.getBytes());
                }
                basicChannel.close();
                card.disconnect(false);
            } catch (CardNotPresentException e) {
                publishProgress("No card in the reader");
            } catch (CardException e) {
                displayErrorText("Card Exception", e);
            } catch (Exception e) {
                displayErrorText("Unexpected exception", e);
            }
            return "";

        }

        private CommandAPDU createCommandAPDU(String command) {
            return new CommandAPDU(HexUtils.hexStrToBytes(command));
        }

        private void displayTextResponse(byte[] response) {
            String readableResponse = HexUtils.extractedStrings(response);
            Log.d(TAG, "received :" + readableResponse);
            if (StringUtils.isNotBlank(readableResponse)) {
                publishProgress(readableResponse);
            }
        }

        private void displayErrorText(String message, Exception e) {
            Log.e(TAG, message, e);
            publishProgress("ERROR : " + message + ", " + e.getMessage());
        }

        protected void onProgressUpdate(String... messages) {
            for (String message : messages) {
                consoleLines.add(message);
            }
            adapter.notifyDataSetChanged();
        }

    }

    private void checkStatus() {
        TerminalConfiguration terminalConfiguration = TerminalConfiguration.getTerminalConfiguration(this);
        ButtonUtil.updateButton((Button) findViewById(R.id.readCardButton), terminalConfiguration.isSmartCardApiUsable());
    }

}
