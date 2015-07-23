package com.aevi.simpleexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.aevi.helpers.services.AeviServiceConnectionCallback;
import com.aevi.helpers.services.AeviServiceException;
import com.aevi.preferences.SharedPreferencesService;
import com.aevi.preferences.SharedPreferencesServiceProvider;
import com.aevi.simpleexample.SharedPreferenceItem.SharedPreferencesType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SharedPreferencesEditActivity extends Activity {
    private static final String TAG = SharedPreferencesEditActivity.class.getSimpleName();

    public static final String SHARED_ITEM_KEY = "SharedItemKey";
    public static final String SHARED_ITEM_TYPE = "SharedItemType";


    private SharedPreferencesServiceProvider serviceProvider;

    private ArrayAdapter<String> adapter;
    private List<String> strings = new ArrayList<String>();
    private String sharedItemKey;
    private boolean finishAfterCommitChanges;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_preferences_edit);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            sharedItemKey = bundle.getString(SHARED_ITEM_KEY);
            SharedPreferencesType type = (SharedPreferencesType) bundle.getSerializable(SHARED_ITEM_TYPE);
            updateView(type);
        }

        setTypeChangedListener();

        setupStringSetListView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        serviceProvider = new SharedPreferencesServiceProvider(this);
        updateSharedPreferenceFields();
    }

    @Override
    protected void onPause() {
        serviceProvider.close();
        serviceProvider = null;
        super.onPause();
    }


    private void setupStringSetListView() {
        ListView listView = (ListView) findViewById(R.id.preference_value_string_set_list);
        adapter = new CustomArrayAdapter(this, R.layout.shared_preferences_string_item, strings);
        listView.setAdapter(adapter);
    }

    private void setTypeChangedListener() {
        Spinner spinner = (Spinner) findViewById(R.id.shared_preference_spinner);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                updateView(SharedPreferencesType.values()[position]);
                clearErrors();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void updateView(SharedPreferencesType type) {
        if (sharedItemKey != null) {
            EditText keyEditText = (EditText) findViewById(R.id.preference_key_editText);
            keyEditText.setText(sharedItemKey);
            keyEditText.setEnabled(false);
            findViewById(R.id.shared_preference_spinner).setEnabled(false);
            findViewById(R.id.edit_container).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.shared_preference_edit_title)).setText(getString(R.string.preference_edit_title));
        } else {
            findViewById(R.id.readOnly_container).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.shared_preference_edit_title)).setText(getString(R.string.preference_create_title));
        }
        findViewById(R.id.preference_value_editText).setVisibility(View.GONE);
        findViewById(R.id.preference_add_button).setVisibility(View.GONE);
        findViewById(R.id.preference_value_string_set_list).setVisibility(View.GONE);
        switch (type) {
            case STRING_SET:
                findViewById(R.id.preference_add_button).setVisibility(View.VISIBLE);
                findViewById(R.id.preference_value_string_set_list).setVisibility(View.VISIBLE);
                break;
            default:
                findViewById(R.id.preference_value_editText).setVisibility(View.VISIBLE);
                break;
        }
    }


    private void updateSharedPreferenceFields() {
        if (sharedItemKey == null) {
            return;
        }

        Log.i(TAG, "Retrieving all shared preference keys");
        serviceProvider.connect(new AeviServiceConnectionCallback<SharedPreferencesService>() {
            @Override
            public void onConnect(SharedPreferencesService service) {
                try {

                    SharedPreferences sharedPreferences = service.getSharedPreferences();
                    Object value = sharedPreferences.getAll().get(sharedItemKey);
                    SharedPreferenceItem item = new SharedPreferenceItem(sharedItemKey, value);


                    ((TextView) findViewById(R.id.preference_key_editText)).setText(item.getKey());
                    ((Spinner) findViewById(R.id.shared_preference_spinner)).setSelection(item.getType().ordinal());
                    ((TextView) findViewById(R.id.preference_key_readOnly)).setText(item.getKey());
                    ((TextView) findViewById(R.id.preference_type_readOnly)).setText(item.getTypeString(SharedPreferencesEditActivity.this));
                    if (item.getType() == SharedPreferencesType.STRING_SET) {
                        strings.clear();
                        Set<String> stringSet = (Set<String>) item.getValue();
                        for (String s : stringSet) {
                            strings.add(s);
                        }
                        adapter.notifyDataSetChanged();

                    } else {

                        ((TextView) findViewById(R.id.preference_value_editText)).setText(item.getValueToString());
                    }

                } catch (RemoteException e) {
                    Log.e(TAG, "Remote Exception while getting all shared preference keys", e);
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    public void onAddStringButtonClick(View view) {
        showStringDialog(-1);
    }


    private void showStringDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle((position >= 0) ? getString(R.string.preference_existingString) : getString(R.string.preference_newString));

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        if (position >= 0) {
            input.setText(strings.get(position));
        }
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(getString(R.string.text_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString().trim();
                for (String s : strings) {
                    // Ignore duplicates
                    if (value.equals(s)) {
                        return;
                    }
                }

                if (position >= 0) {
                    strings.set(position, value);
                } else {
                    strings.add(value);
                }
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    public void onUpdateButtonClick(View view) {
        Object value;

        String key = ((EditText) findViewById(R.id.preference_key_editText)).getText().toString().trim();
        clearErrors();

        if (key.isEmpty()) {
            ((TextView) findViewById(R.id.key_error_text)).setText(getString(R.string.preference_noKeyEntered));
            findViewById(R.id.preference_key_editText).requestFocus();
            return;
        }

        SharedPreferencesType type = SharedPreferencesType.values()[((Spinner) findViewById(R.id.shared_preference_spinner)).getSelectedItemPosition()];

        if (type == SharedPreferencesType.STRING_SET) {

            value = strings;

        } else {

            String stringValue = ((EditText) findViewById(R.id.preference_value_editText)).getText().toString();

            value = validateAndConvertValue(type, stringValue);
            if (value == null) {
                ((TextView) findViewById(R.id.value_error_text)).setText(getString(R.string.preference_invalid) + " " + SharedPreferenceItem.getTypeString(view.getContext(), type));
                findViewById(R.id.preference_value_editText).requestFocus();
                return;
            }
        }

        updateValues(key, type, value);
        finishAfterCommitChanges = true;
    }

    private void clearErrors() {
        ((TextView) findViewById(R.id.key_error_text)).setText("");
        ((TextView) findViewById(R.id.value_error_text)).setText("");
    }

    private Object validateAndConvertValue(SharedPreferencesType type, String stringValue) {
        try {
            switch (type) {
                case BOOLEAN:
                    String s = stringValue.trim();
                    if (s.equalsIgnoreCase("true")) {
                        return true;
                    }
                    if (s.equalsIgnoreCase("false")) {
                        return false;
                    }
                    return null;

                case STRING:
                    return stringValue;

                case LONG:
                    return Long.parseLong(stringValue);

                case INTEGER:
                    return Integer.parseInt(stringValue);

                case FLOAT:
                    return Float.parseFloat(stringValue);

                case STRING_SET:
                    break;
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }


    void updateValues(final String keyText, final SharedPreferencesType type, final Object value) {
        serviceProvider.connect(new AeviServiceConnectionCallback<SharedPreferencesService>() {

            @Override
            public void onConnect(SharedPreferencesService service) {
                try {

                    SharedPreferences.Editor newFieldEditor = service.getSharedPreferences().edit();
                    switch (type) {
                        case BOOLEAN:
                            newFieldEditor.putBoolean(keyText, (Boolean) value);
                            break;
                        case STRING:
                            newFieldEditor.putString(keyText, (String) value);
                            break;
                        case LONG:
                            newFieldEditor.putLong(keyText, (Long) value);
                            break;
                        case INTEGER:
                            newFieldEditor.putInt(keyText, (Integer) value);
                            break;
                        case FLOAT:
                            newFieldEditor.putFloat(keyText, (Float) value);
                            break;
                        case STRING_SET:
                            Log.i(TAG, "Inserting set into preferences");
                            newFieldEditor.putStringSet(keyText, new HashSet<String>(strings));
                            break;
                    }
                    newFieldEditor.commit();
                } catch (RemoteException e) {
                    Log.e(TAG, "Remote exception occurred when trying to add a field to the shared preferences", e);
                    throw new AeviServiceException("Remote exception occurred when trying to add a field to the shared preferences", e);
                }

                if (finishAfterCommitChanges) {
                    finish();
                } else {
                    updateSharedPreferenceFields();
                }
            }
        });

    }

    class CustomArrayAdapter extends ArrayAdapter<String> {
        private final Context context;

        public CustomArrayAdapter(Context context, int textViewResourceId, List<String> strings) {
            super(context, textViewResourceId, strings);
            this.context = context;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final String string = getItem(position);

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.shared_preferences_string_item, null);
                setOnClickListeners(convertView);
            }

            ((TextView) convertView.findViewById(R.id.string_item_key_id)).setText(string);

            setClickListenersTags(convertView, position);

            return convertView;
        }


        protected void setOnClickListeners(View view) {

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (Integer) view.getTag();
                    showStringDialog(position);

                }
            });

            view.findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (Integer) view.getTag();
                    remove(getItem(position));
                    notifyDataSetChanged();
                }
            });
        }


        private void setClickListenersTags(View convertView, int position) {
            convertView.setTag(position);
            convertView.findViewById(R.id.deleteButton).setTag(position);
        }
    }


}
