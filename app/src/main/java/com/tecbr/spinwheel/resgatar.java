package com.tecbr.spinwheel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class resgatar extends AppCompatActivity {

    ImageView pi1, pi2;

    Button v;
    Context context = this;

    TextView p;

    private MaxInterstitialAd interstitialAd;
    private int retryAttempt;
    private MaxAdListener maxAdListener;

    public String SHARED_PREFS = "sharedPrefs";
    public String KEY = "pontos";
    SharedPreferences sharedPreferences;
    int valorDescontar = 0;

    private String nome = "";

    private String email = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        pi1 = (ImageView) findViewById(R.id.imageView3422);

        pi1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(infoColetada()){
                    if(Integer.parseInt(p.getText().toString()) > 499){ //499
                        valorDescontar = Integer.parseInt(p.getText().toString()) - 500; //5000
                        saveData(valorDescontar);
                        p.setText(loadData());
                        requestRes(nome, email);
                    }else{
                        ToastCustom("Pontos insuficientes");
                    }
                }else{
                    showDialog();
                }

            }
        });

        pi2 = (ImageView) findViewById(R.id.imageView342);

        pi2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(infoColetada()){
                    if(Integer.parseInt(p.getText().toString()) > 999){ //999
                        valorDescontar = Integer.parseInt(p.getText().toString()) - 1000; //10000
                        saveData(valorDescontar);
                        p.setText(loadData());
                        requestRes(nome, email);
                    }else{
                        ToastCustom("Pontos insuficientes");
                    }

                }else{
                    showDialog();
                }

            }
        });

        Bundle b = getIntent().getExtras();

        sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        p = findViewById(R.id.textView5);
        p.setText(loadData());

        ImageView img_voltar = findViewById(R.id.left_icon);

        img_voltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(interstitialAd.isReady()){
                    interstitialAd.showAd();
                }
                onBackPressed();
            }
        });

        applovin_load();

        if(!infoColetada()){
            showDialog();
        }

    }

    public boolean infoColetada(){
        nome = sharedPreferences.getString("nome", "");
        email = sharedPreferences.getString("email", "");
        if(nome.isEmpty()){
            return false;
        }else{
            return true;
        }
    }

    public void saveData(int value) {
        //ToastCustom("" + value);
        sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY, value);
        editor.apply();
    }

    public void saveEmail(int value) {
        //ToastCustom("" + value);
        sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY, value);
        editor.apply();
    }

    public String loadData() {
        int number = sharedPreferences.getInt(KEY, 0);
        String refector = "" + number;
        return refector;
    }

    public void showDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_resgate);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        EditText editNome = (EditText) dialog.findViewById(R.id.nome);
        EditText editID = (EditText) dialog.findViewById(R.id.id);

        /*TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
        text.setText(msg);*/

        Button dialogButton = (Button) dialog.findViewById(R.id.request_btn);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editID.getText().toString().isEmpty()){
                    ToastCustom("Digite sua chave");
                }else{
                    sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("email", editID.getText().toString());
                    editor.putString("nome", editNome.getText().toString());
                    editor.apply();
                    dialog.dismiss();
                    /*saveData(valorDescontar);
                    dialog.dismiss();
                    p.setText(loadData());
                    requestRes(editNome.getText().toString(), editID.getText().toString());*/
                }
            }
        });

        dialog.show();

    }

    public void ToastCustom(String message){
        ViewGroup view = findViewById(R.id.toast_layout_root);
        View v = getLayoutInflater().inflate(R.layout.toast, view);

        TextView mensage = v.findViewById(R.id.text);
        mensage.setText(message);

        Toast toast = new Toast(this);
        toast.setView(v);
        toast.setDuration(Toast.LENGTH_SHORT);

        toast.show();
    }

    public void applovin_load(){
        // Make sure to set the mediation provider value to "max" to ensure proper functionality
        AppLovinSdk.getInstance( this ).setMediationProvider( "max" );
        AppLovinSdk.initializeSdk( this, new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration configuration)
            {
                // AppLovin SDK is initialized, start loading ads
            }
        } );

        maxAdListener = new MaxAdListener() {
            // MAX Ad Listener
            @Override
            public void onAdLoaded(final MaxAd maxAd)
            {
                // Interstitial ad is ready to be shown. interstitialAd.isReady() will now return 'true'

                // Reset retry attempt
                retryAttempt = 0;
            }

            @Override
            public void onAdLoadFailed(final String adUnitId, final MaxError error)
            {
                //ToastCustom(error.getMessage());
                // Interstitial ad failed to load
                // AppLovin recommends that you retry with exponentially higher delays up to a maximum delay (in this case 64 seconds)

                retryAttempt++;
                long delayMillis = TimeUnit.SECONDS.toMillis( (long) Math.pow( 2, Math.min( 6, retryAttempt ) ) );

                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        interstitialAd.loadAd();
                    }
                }, delayMillis );
            }

            @Override
            public void onAdDisplayFailed(final MaxAd maxAd, final MaxError error)
            {
                // Interstitial ad failed to display. AppLovin recommends that you load the next ad.
                interstitialAd.loadAd();
            }

            @Override
            public void onAdDisplayed(final MaxAd maxAd) {

            }

            @Override
            public void onAdClicked(final MaxAd maxAd) {}

            @Override
            public void onAdHidden(final MaxAd maxAd)
            {
            }
        };

        interstitialAd = new MaxInterstitialAd( "421e38975d7cded7", this );
        interstitialAd.setListener( maxAdListener );

        // Load the first ad
        interstitialAd.loadAd();
    }

    public void requestRes(String nome, String email){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://roletapixsorteios.000webhostapp.com/api-resgate.php?nome=" + nome + "&email=" + email;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ToastCustom(response);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
        };
        queue.add(stringRequest);
    }
}