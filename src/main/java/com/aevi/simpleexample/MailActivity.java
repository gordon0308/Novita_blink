/*
 * This sample code is a preliminary draft for illustrative purposes only and not subject to any license granted by Wincor Nixdorf.
 * The sample code is provided "as is" and Wincor Nixdorf assumes no responsibility for errors or omissions of any kind out of the
 * use of such code by any third party. 
 */
package com.aevi.simpleexample;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.aevi.helpers.services.AeviServiceConnectionCallback;
import com.aevi.mail.MailAuthenticationException;
import com.aevi.mail.MailConfigurationException;
import com.aevi.mail.MailMessage;
import com.aevi.mail.MailMessageAttachment;
import com.aevi.mail.MailSendException;
import com.aevi.mail.MailService;
import com.aevi.mail.MailServiceProvider;
import com.aevi.mail.MailSslConnectionException;

import java.io.IOException;
import java.io.InputStream;

public class MailActivity extends Activity {

    private static final String TAG = MailActivity.class.getSimpleName();

    private MailServiceProvider serviceProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail);
        hideError();
    }

    @Override
    protected void onResume() {
        super.onResume();
        serviceProvider = new MailServiceProvider(this);
    }

    @Override
    protected void onPause() {
        // close all services and remove listeners
        serviceProvider.close();
        serviceProvider = null;
        super.onPause();
    }

    private void showError(String message) {
        TextView errorField = (TextView) findViewById(R.id.errorText);
        errorField.setVisibility(View.VISIBLE);
        errorField.setText(message);
    }

    private void hideError() {
        findViewById(R.id.errorText).setVisibility(View.INVISIBLE);
    }

    public void sendPlainMailButton(View view) {
        hideError();

        try {
            sendMail(createPlainMessage());
        } catch (IllegalArgumentException e) {
            showError("Illegal email address");
        }
    }

    public void sendHtmlMailButton(View view) {
        hideError();

        try {
            sendMail(createHtmlMessage());
        } catch (IllegalArgumentException e) {
            showError("Illegal email address");
        }
    }

    public void sendMailWithAttachmentButton(View view) throws IOException {
        hideError();

        try {
            sendMail(createMessageWithAttachment());
        } catch (IllegalArgumentException e) {
            showError("Illegal email address");
        }
    }

    private void sendMail(final MailMessage mailMessage) {
        // To determine if the email service is configured see TerminalConfiguration.isMailApiUsable()
        serviceProvider.connect(new AeviServiceConnectionCallback<MailService>() {
            @Override
            public void onConnect(MailService service) {
                new Mailer(service).execute(mailMessage);
            }
        });
    }

    private MailMessage createPlainMessage() {
        return new MailMessage()
                .addTo(getValue(R.id.emailTo).trim())
                .setSubject("Plain test mail")
                .setPlainContent("This is a plain text mail message.\n\nGreetings\n\nThe Aevi Team");
    }

    private MailMessage createHtmlMessage() {
        return new MailMessage()
                .addTo(getValue(R.id.emailTo).trim())
                .setSubject("Html test mail")
                .setHtmlContent("<html><body><h1>Example</h1><p>This is a html mail message.</p><p>Greetings</p><p>The Aevi Team</p></body></html>");
    }

    private MailMessage createMessageWithAttachment() throws IOException {
        return new MailMessage()
                .addTo(getValue(R.id.emailTo).trim())
                .setSubject("Attachment test mail")
                .setHtmlContent("<html>" +
                        "<body>" +
                        "<h1>Example</h1>" +
                        "<p>This is a html mail message with some attachments.</p>" +
                        "<p>Greetings</p>" +
                        "<p>The Aevi Team</p>" +
                        "</body>" +
                        "</html>")
                .addAttachment(createAttachment("wincor_icon.png", R.drawable.wincor_icon, "image/png"))
                .addAttachment(createAttachment("aevi_icon.png", R.raw.aevi_icon, "image/png"));
    }

    private MailMessageAttachment createAttachment(String name, int id, String type) throws IOException {
        return new MailMessageAttachment(name, readResource(id), type);
    }

    private byte[] readResource(int id) throws IOException {
        InputStream inputStream = getResources().openRawResource(id);
        byte[] result = new byte[inputStream.available()];
        inputStream.read(result);
        return result;
    }

    private String getValue(int id) {
        EditText editText = (EditText) findViewById(id);
        return editText.getText().toString();
    }


    private class Mailer extends AsyncTask<MailMessage, String, Void> {

        private final MailService service;
        private ProgressDialog dialog = new ProgressDialog(MailActivity.this);

        private Mailer(MailService service) {
            this.service = service;
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }


        @Override
        protected Void doInBackground(final MailMessage... messages) {

            try {
                for (MailMessage message : messages) {
                    service.send(message);
                }
            } catch (IllegalArgumentException e) {
                publishProgress("Illegal email address ");
                Log.e(TAG, "IllegalArgumentException " + e.getMessage());
            } catch (MailSslConnectionException e) {
                publishProgress("Failed to create a SSL connection to the mail server");
            } catch (MailAuthenticationException e) {
                publishProgress("Authentication to the mailserver failed");
            } catch (MailConfigurationException e) {
                publishProgress("Cannot send email - email service not configured");
            } catch (MailSendException e) {
                publishProgress("Sending the message failed");
            }

            return null;
        }

        protected void onProgressUpdate(String... messages) {
            for (String message : messages) {
                showError(message);
            }
        }

        @Override
        protected void onPostExecute(final Void success) {

            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            super.onPostExecute(success);
        }
    }

}
