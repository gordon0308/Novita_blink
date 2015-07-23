/*
 * This sample code is a preliminary draft for illustrative purposes only and not subject to any license granted by Wincor Nixdorf.
 * The sample code is provided "as is" and Wincor Nixdorf assumes no responsibility for errors or omissions of any kind out of the
 * use of such code by any third party. 
 */
package com.aevi.simpleexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.*;
import android.widget.ImageView;
import android.media.MediaPlayer;

import com.aevi.payment.AccountType;
import com.aevi.payment.CardEntryType;
import com.aevi.payment.CardType;
import com.aevi.payment.CompletionRequest;
import com.aevi.payment.DepositRequest;
import com.aevi.payment.MoToPaymentRequest;
import com.aevi.payment.MoToRefundRequest;
import com.aevi.payment.MoToRequestType;
import com.aevi.payment.PaymentAppConfiguration;
import com.aevi.payment.PaymentRequest;
import com.aevi.payment.PreAuthorisationRequest;
import com.aevi.payment.RefundRequest;
import com.aevi.payment.ReversalRequest;
import com.aevi.payment.TokenRequest;
import com.aevi.payment.TransactionReferences;
import com.aevi.payment.TransactionReferenceCode;
import com.aevi.payment.TransactionResult;
import com.aevi.payment.TransactionStatus;
import com.aevi.payment.TransactionType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Payment Activity demonstrates various payment request and their configuration options.
 */
public class PaymentActivity extends Activity {
    MediaPlayer player = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
        final Button btn = (Button) findViewById(R.id.menu_button_printing);
        btn.startAnimation(animation);
        player = MediaPlayer.create(PaymentActivity.this, R.raw.welcome);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                player.start();
                startActivity(new Intent(PaymentActivity.this, MagStripeActivity.class));

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

//    public void blink(View view){
//        ImageView image = (ImageView)findViewById(R.id.imageView);
//        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
//        image.startAnimation(animation1);
//    }


}
 