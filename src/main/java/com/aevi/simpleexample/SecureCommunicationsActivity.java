/*
 * This sample code is a preliminary draft for illustrative purposes only and not subject to any license granted by Wincor Nixdorf.
 * The sample code is provided "as is" and Wincor Nixdorf assumes no responsibility for errors or omissions of any kind out of the
 * use of such code by any third party.
 */
package com.aevi.simpleexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.aevi.configuration.TerminalConfiguration;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SecureCommunicationsActivity extends Activity {

    private static final String TAG = SecureCommunicationsActivity.class.getSimpleName();

    // this URL only works on the Aevi device. See the sample documentation for an explanation.
    // URL used on the Aevi Device (See: com.aevi.helpers.UriBuilder)
    private static final String GITHUB_API_URL = "https://api.github.com";

    private static final String GITHUB_SEARCH_REPOSITORIES_URL = GITHUB_API_URL + "/search/repositories?sort=stars&order=desc&per_page=20";

    private List<String> consoleLines = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_https_example);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, consoleLines);
        ListView console = (ListView) findViewById(R.id.consoleView);
        console.setAdapter(adapter);

        setupSearchTextEnterListener();

        progressBarVisible(false);
    }

    private void displayConsoleText(String... messages) {
        progressBarVisible(false);
        Collections.addAll(consoleLines, messages);
        adapter.notifyDataSetChanged();
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

    private void progressBarVisible(boolean visible) {
        findViewById(R.id.progressBar).setVisibility((visible) ? View.VISIBLE : View.INVISIBLE);
    }

    private void setupSearchTextEnterListener() {
        EditText searchText = (EditText) findViewById(R.id.searchText);
        searchText.setImeActionLabel("Search", KeyEvent.KEYCODE_ENTER);

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == keyEvent.ACTION_DOWN) {
                    searchProjectsClick(textView);
                    return true;
                }
                return false;
            }
        });
    }

    public void searchProjectsClick(View view) {
        clearConsoleLines();
        String searchText = ((EditText) findViewById(R.id.searchText)).getText().toString();
        searchText = searchText.trim();
        if (searchText.length() >= 2) {
            progressBarVisible(true);
            new AsyncReadHttps().execute(searchText);
        }
    }

    private void clearConsoleLines() {
        consoleLines.clear();
        adapter.notifyDataSetChanged();
    }

    private class AsyncReadHttps extends AsyncTask<String, String, String> {

        protected String doInBackground(String... projectNames) {
            readHttpData(GITHUB_SEARCH_REPOSITORIES_URL + "&q=" + projectNames[0]);
            return "";
        }

        void readHttpData(String url) {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(new URI(url));
                HttpResponse response = client.execute(request);
                InputStream is = response.getEntity().getContent();
                decodeTheJsonResponse(is);
                is.close();
            } catch (IOException e) {
                displayErrorText("Network problems", e);
            } catch (URISyntaxException e) {
                displayErrorText("invalid URL", e);
            }
        }

        private void decodeTheJsonResponse(InputStream is) {
            String jsonString = inputStreamToString(is);
            try {

                JSONObject topLevel = new JSONObject(jsonString);
                JSONArray itemsList = topLevel.getJSONArray("items");
                int length = itemsList.length();

                if (length == 0) {
                    displayTextResponse("No Results found");
                    return;
                }

                for (int i = 0; i < length; i++) {
                    JSONObject item = itemsList.getJSONObject(i);
                    displayTextResponse(item.getString("name"));
                }

            } catch (JSONException e) {
                displayErrorText("Not valid json", e);
            }
        }

        public String inputStreamToString(InputStream stream) {
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            String result = "";
            while (true) {
                String line;
                try {
                    line = in.readLine();
                } catch (IOException e) {
                    break;
                }
                if (line == null) {
                    break;
                }
                result += line;
            }
            return result;
        }

        private void displayTextResponse(String message) {
            publishProgress(message);
        }

        private void displayErrorText(String message, Exception e) {
            displayTextResponse("ERROR - " + message + ": " + e.getMessage());
        }

        protected void onProgressUpdate(String... messages) {
            displayConsoleText(messages);
            progressBarVisible(false);
        }
    }
}
