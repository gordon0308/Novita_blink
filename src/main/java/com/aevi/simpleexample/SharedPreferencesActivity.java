/*
 * This sample code is a preliminary draft for illustrative purposes only and not subject to any license granted by Wincor Nixdorf.
 * The sample code is provided "as is" and Wincor Nixdorf assumes no responsibility for errors or omissions of any kind out of the
 * use of such code by any third party. 
 */
package com.aevi.simpleexample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aevi.configuration.TerminalConfiguration;
import com.aevi.helpers.services.AeviServiceConnectionCallback;
import com.aevi.preferences.SharedPreferencesService;
import com.aevi.preferences.SharedPreferencesServiceProvider;

import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SharedPreferencesActivity.class.getSimpleName();

    private SharedPreferencesServiceProvider serviceProvider;
    private SharedPreferences sharedPreferences;

    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_preferences);

    }

    public void startEditActivity(String key, SharedPreferenceItem.SharedPreferencesType type) {
        Intent intent = new Intent(this, SharedPreferencesEditActivity.class);
        intent.putExtra(SharedPreferencesEditActivity.SHARED_ITEM_KEY, key);
        intent.putExtra(SharedPreferencesEditActivity.SHARED_ITEM_TYPE, type);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        serviceProvider = new SharedPreferencesServiceProvider(this);
        updateKeyList();
    }

    @Override
    protected void onPause() {
        //Remove change listener from old shared preferences
        if (sharedPreferences != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }

        serviceProvider.close();
        serviceProvider = null;

        super.onPause();
    }

    /**
     * Handles the create button click by showing a new dialog
     *
     * @param view
     */
    public void createPreferenceClick(View view) {
        if (!validateApiIsUsable()) {
            return;
        }
        Intent intent = new Intent(this, SharedPreferencesEditActivity.class);
        startActivity(intent);
    }

    /**
     * Update the list of onscreen shared preference keys
     */
    private void updateKeyList() {

        Log.i(TAG, "Retrieving all shared preference keys");
        final SharedPreferencesActivity self = this;
        serviceProvider.connect(new AeviServiceConnectionCallback<SharedPreferencesService>() {
            @Override
            public void onConnect(SharedPreferencesService service) {
                try {
                    ListView preferenceKeyList = (ListView) findViewById(R.id.sharedPreferenceList);

                    //Remove change listener from old shared preferences
                    if (sharedPreferences != null) {
                        sharedPreferences.unregisterOnSharedPreferenceChangeListener(self);
                    }
                    sharedPreferences = service.getSharedPreferences();
                    sharedPreferences.registerOnSharedPreferenceChangeListener(self);

                    editor = sharedPreferences.edit();
                    preferenceKeyList.setAdapter(new SharedPreferenceAdapter(SharedPreferencesActivity.this, sharedPreferences));
                } catch (RemoteException e) {
                    Log.e(TAG, "Remote Exception while getting all shared preference keys", e);
                    throw new IllegalStateException(e);
                }
            }
        });
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Toast.makeText(getApplicationContext(), getString(R.string.preference_notification_received) + " " + key, Toast.LENGTH_SHORT).show();
    }

    private boolean validateApiIsUsable() {
        TerminalConfiguration terminalConfiguration = TerminalConfiguration.getTerminalConfiguration(this);
        if (!terminalConfiguration.isSharedPreferencesApiUsable()) {
            Toast.makeText(this, "SharedPreferences API does not work. Reason : " + terminalConfiguration.getSharedPreferencesApiStatus(), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    class SharedPreferenceAdapter extends ArrayAdapter<SharedPreferenceItem> {
        private final Context context;

        public SharedPreferenceAdapter(Context context, SharedPreferences sharedPreferences) {
            super(context, R.layout.shared_preferences_item, new ArrayList<SharedPreferenceItem>());
            this.context = context;

            List<String> keyList = new ArrayList<String>(sharedPreferences.getAll().keySet());

            for (String key : keyList) {
                add(new SharedPreferenceItem(key, sharedPreferences.getAll().get(key)));
            }
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final SharedPreferenceItem item = getItem(position);

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.shared_preferences_item, null);
                setOnClickListeners(convertView);
            }

            ((TextView) convertView.findViewById(R.id.shared_preference_key_id)).setText(item.getKey());
            ((TextView) convertView.findViewById(R.id.shared_preference_type_id)).setText(item.getTypeString(convertView.getContext()));

            ((TextView) convertView.findViewById(R.id.shared_preference_value_id)).setText(item.getValueToString());

            setClickListenersTags(convertView, position);

            return convertView;
        }


        protected void setOnClickListeners(View view) {

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferenceItem item = getItem((Integer) view.getTag());
                    startEditActivity(item.getKey(), item.getType());

                }
            });

            view.findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (Integer) view.getTag();
                    String key = getItem(position).getKey();
                    editor.remove(key).commit();
                    updateKeyList();
                }
            });
        }


        private void setClickListenersTags(View convertView, int position) {
            convertView.setTag(position);
            convertView.findViewById(R.id.deleteButton).setTag(position);
        }
    }

}
