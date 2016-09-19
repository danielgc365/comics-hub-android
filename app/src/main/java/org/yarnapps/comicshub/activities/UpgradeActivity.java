package org.yarnapps.comicshub.activities;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.yarnapps.comicshub.R;

import java.math.BigDecimal;

public class UpgradeActivity extends BaseAppActivity {

    private static PayPalConfiguration config = new PayPalConfiguration()

            // Start with mock environment.  When ready, switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId("AXbi-k0GuQrsdFj0jq12eGXEtGdZnN7RW4No1EiXLBaP6-xwY346uJ5B_YRhMJH4qWTWFTJLhOzS7_5P");

    private Button mUpgradeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);

        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    public void onBuyPressed(View pressed){
        PayPalPayment payment = new PayPalPayment(new BigDecimal("2.99"), "USD", "Comics Hub Full Version",
                PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, 0);

    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        if(resultCode == Activity.RESULT_OK){
            PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if(confirm != null){
                try{
                    Log.i("UpgradeActivity", confirm.toJSONObject().toString(4));

                    //TODO verify payment

                    //Below Preference to change to paid version
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor sharedEditor = sharedPref.edit();
                    sharedEditor.putBoolean("PaidBooleanKey", true);
                    sharedEditor.commit();

                    //Do a dialog to thank user for support
                    AlertDialog.Builder builder = new AlertDialog.Builder(UpgradeActivity.this);
                    builder.setMessage("You just upgraded to the full version of Comics Hub! Thank you for the support we hope you enjoy the app. For support or any questions, visits us on twitter @comics_hub")
                            .setTitle("Thank You!");

                    builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(UpgradeActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                } catch(JSONException e){
                    Log.e("UpgradeActivity", "and extremely unlikely failure ocurred: ", e);
                }
            }
        }

        else if(resultCode == Activity.RESULT_CANCELED){
            Log.i("UpgradeActivity", "The user cancelled the transaction.");
            Toast.makeText(this, "Payment was cancelled by the user", Toast.LENGTH_SHORT).show();
        }

        else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID){
            Log.i("UpgradeActivity", "An invalid Payment or paypal configuration was submitted.");
            Toast.makeText(this, "Payment could not be processed please try again", Toast.LENGTH_SHORT).show();
        }
    }



}
