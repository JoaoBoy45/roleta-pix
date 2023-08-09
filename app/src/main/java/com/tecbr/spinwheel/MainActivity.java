package com.tecbr.spinwheel;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    TextView textView2;
    TextView pontos;
    Button share;

    ImageView resgatar, gift, sound;
    ImageView girar;
    String id = null;
    boolean girando = false;
    String[] sectors = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    private MaxInterstitialAd interstitialAd;
    private int retryAttempt;
    private MaxAdListener maxAdListener;

    private boolean exibirAd = false;
    Context context = this;
    MediaPlayer player;

    public String SHARED_PREFS = "sharedPrefs";
    public String KEY = "pontos";

    SharedPreferences sharedPreferences;

    ProgressDialog progressDialog;

    private Intent intent;

    private AppLovinSdk sdk;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();



        imageView = (ImageView) findViewById(R.id.imageView);
        textView2 = (TextView) findViewById(R.id.textView2);

        resgatar = (ImageView) findViewById(R.id.troca) ;
        share = (Button) findViewById(R.id.share);

        gift = (ImageView) findViewById(R.id.gift) ;

        sound = (ImageView) findViewById(R.id.sound) ;

        /**/

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name");
                    String shareMessage= "\nAplicativo da Roleta\n\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "choose one"));
                } catch(Exception e) {
                    //e.toString();
                }
            }
        });

        girar = (ImageView) findViewById(R.id.girar);

        pontos = (TextView) findViewById(R.id.textView5);

        resgatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, resgatar.class);
                Bundle b = new Bundle();
                b.putString("pontos", pontos.getText().toString()); //Your id
                i.putExtras(b); //Put your id to your next Intent
                startActivity(i);
            }
        });


        girar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!girando){
                    girando = true;
                    if(loadSound() == 0){
                        startSom();
                    }

                    obter_spin();
                }

            }
        });

        Collections.reverse(Arrays.asList(sectors));

        applovin_load();
        sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        pontos.setText(loadData());

        progressDialog = new ProgressDialog(this);

        setImagesom();

        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(loadSound() == 0){
                    stopSom();
                    saveSound(1);
                    setImagesom();
                }else{
                    saveSound(0);
                    setImagesom();

                }
            }
        });
    }

    void setImagesom() {
        if (loadSound() == 0) {
            Drawable res = getResources().getDrawable(R.drawable.no_muted);
            sound.setImageDrawable(res);
        } else {
            Drawable res = getResources().getDrawable(R.drawable.muted);
            sound.setImageDrawable(res);
        }

    }

    public void showDialogAnuncio() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_reward);

        Button dialogButton = (Button) dialog.findViewById(R.id.aceitar);

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                startActivity(intent);
            }
        });

        dialog.show();
    }

    void showLoading(){
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    void closeLoading(){
        progressDialog.dismiss();
    }

    public void saveData(int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY, value);
        editor.apply();
    }

    public int loadSound() {
        int number = sharedPreferences.getInt("som", 0);
        return number;
    }

    public void saveSound(int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("som", value);
        editor.apply();
    }

    public void saveDataOld(int value) {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putInt(KEY, value);
        editor.apply();
    }
    public String loadData() {
        int number = sharedPreferences.getInt(KEY, 0);
        String refector = "" + number;
        return refector;
    }

    public void SaveAdBet() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("ad", 1);
        editor.apply();
    }



    public String loadDataOld() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        int number = prefs.getInt(KEY, 0);
        String refector = "" + number;
        return refector;
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

        sdk = AppLovinSdk.getInstance( context );

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
                ToastCustom(error.getMessage());
                // Interstitial ad failed to load
                // AppLovin recommends that you retry with exponentially higher delays up to a maximum delay (in this case 64 seconds)

                retryAttempt++;
                long delayMillis = TimeUnit.SECONDS.toMillis( (long) Math.pow( 2, Math.min( 6, retryAttempt ) ) );

                new Handler().postDelayed( new Runnable()
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
                // Interstitial ad is hidden. Pre-load the next ad
                interstitialAd.loadAd();
                girando = false;
            }
        };

        interstitialAd = new MaxInterstitialAd( "421e38975d7cded7", this );
        interstitialAd.setListener( maxAdListener );

        // Load the first ad
        interstitialAd.loadAd();
    }

    public void CalculatePoint(int degree){
        // 360 degrees || 21 segments || 17.14285714285714 each Segment

        int initialPoint = 0;
        int endPoint = 36;
        int i = 0;
        String res = null;

        do{

            if(degree > initialPoint && degree < endPoint){
                res = sectors[i];
            }
            initialPoint += 36;
            endPoint += 36;
            i++;
        }while (res == null);

        ToastCustom("Parabéns ganhou " + res + " pontos");

        saveData(Integer.parseInt(loadData()) + Integer.parseInt(res));

        pontos.setText(loadData());

        girando = false;

        if(exibirAd){
            if(interstitialAd.isReady()){
                this.exibirAd = false;

                interstitialAd.showAd();
            }
        }else{
            this.exibirAd = true;
        }




    }

    private void setID(){
        id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    void startSom(){
        if(player == null){
            player = MediaPlayer.create(this, R.raw.som_girando);
        }

        player.start();

    }

    void stopSom(){
        if(player != null){
            player.release();
            player = null;
        }
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

    private void obter_spin(){
        final int min = 0;
        final int max = 360;
        final int random = new Random().nextInt((max - min) + 1) + min;

        startSpin(random);
    }

    private void giro_completo(){


    }

    private void startSpin(int rand){
        Random random = new Random();
        //final int degree = random.nextInt(360);

        final int degree = rand;

        //Toast.makeText(this, "rand: " + degree, Toast.LENGTH_LONG).show();

        RotateAnimation rotateAnimation = new RotateAnimation(0,degree+720,
                RotateAnimation.RELATIVE_TO_SELF,0.5f,
                RotateAnimation.RELATIVE_TO_SELF,0.5f);

        rotateAnimation.setDuration(5000);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setInterpolator(new DecelerateInterpolator());


        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                CalculatePoint(degree);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        imageView.startAnimation(rotateAnimation);
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        ToastCustom("Pressione novamente para sair");

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopSom();
    }
}