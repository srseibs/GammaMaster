package com.sailinghawklabs.gammamaster;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.IOException;
import java.io.InputStream;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

// public class MainActivity extends AppCompatActivity implements com.sailinghawklabs.gammamaster.GammaSolver.NotifyCallback, TextView.OnEditorActionListener, View.OnTouchListener {

 public class MainActivity extends AppCompatActivity implements com.sailinghawklabs.gammamaster.GammaSolver.NotifyCallback, TextView.OnEditorActionListener{
    private static final String TAG = MainActivity.class.getName();

    GammaSolver gammaSolver1;
    GammaSolver gammaSolver2;
    MismatchSolver mismatchSolver;
    AdView mainAd;
    AdView helpAd;

    EditText et_z0;
    private static final String KEY_PRESET_GAMMA1 = "PRESET_GAMMA1";
    private static final String KEY_PRESET_GAMMA2 = "PRESET_GAMMA2";
    private static final String KEY_PRESET_Z0 = "PRESET_Z0";
    private static final String KEY_GAMMA1 = "GAMMA1";
    private static final String KEY_GAMMA2 = "GAMMA2";

//    @Override
//    public void onBackPressed() {
//        Log.d(TAG, "onBackPressed: entered");
//        View main = (View) findViewById(R.id.et_vswr_1);
//        main.requestFocus();
//        super.onBackPressed();
//    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: entered");

//        View container = findViewById(R.id.main_container);
//        container.setOnTouchListener(this);

        mainAd = (AdView) findViewById(R.id.adView);

        // lookup all the Views -------------------------------------------------------------
        et_z0 = (EditText) findViewById(R.id.et_z0);
        et_z0.setOnEditorActionListener(this);
        et_z0.setImeOptions(IME_ACTION_DONE);
        mismatchSolver = new MismatchSolver();
        mismatchSolver.setView_MAG_PLUS(findViewById(R.id.tv_mismag_p));
        mismatchSolver.setView_MAG_MINUS(findViewById(R.id.tv_mismag_n));
        mismatchSolver.setView_PHASE(findViewById(R.id.tv_misphas));
        Log.d(TAG, "onCreate: z0: " + et_z0.getText().toString() + ", savedInstanceState = " + savedInstanceState);

        gammaSolver1 = new GammaSolver(this, this);
        gammaSolver1.setView_Z0(findViewById(R.id.et_z0));
        gammaSolver1.setView_RL(findViewById(R.id.et_rl_1));
        gammaSolver1.setView_GAMMA(findViewById(R.id.et_gamma_1));
        gammaSolver1.setView_VSWR(findViewById(R.id.et_vswr_1));
        gammaSolver1.setView_RLOADGT(findViewById(R.id.et_rload_lg_1));
        gammaSolver1.setView_RLOADLT(findViewById(R.id.et_rload_sm_1));
        gammaSolver1.setView_MISMATCHLOSS(findViewById(R.id.et_misloss_1));

        gammaSolver2 = new GammaSolver(this, this);
        gammaSolver2.setView_Z0(findViewById(R.id.et_z0));
        gammaSolver2.setView_RL(findViewById(R.id.et_rl_2));
        gammaSolver2.setView_GAMMA(findViewById(R.id.et_gamma_2));
        gammaSolver2.setView_VSWR(findViewById(R.id.et_vswr_2));
        gammaSolver2.setView_RLOADGT(findViewById(R.id.et_rload_lg_2));
        gammaSolver2.setView_RLOADLT(findViewById(R.id.et_rload_sm_2));
        gammaSolver2.setView_MISMATCHLOSS(findViewById(R.id.et_misloss_2));
        // -------------------------------------------------------------------------------

        if (savedInstanceState == null) {
            preset();
        }

        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mainAd.loadAd(adRequest);
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstganceState: z0: " + et_z0.getText().toString());

        outState.putDouble(KEY_GAMMA1, gammaSolver1.getGamma());
        outState.putDouble(KEY_GAMMA2, gammaSolver2.getGamma());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState: z0: " + et_z0.getText().toString());
        super.onRestoreInstanceState(savedInstanceState);
        updateZ0(et_z0.getText().toString());
        gammaSolver1.setGamma(savedInstanceState.getDouble(KEY_GAMMA1));
        gammaSolver2.setGamma(savedInstanceState.getDouble(KEY_GAMMA2));
        reportChange();

    }

    void preset()
    {
        // preset to the Shared Preferences (or the String resources as default)
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String z0 = preferences.getString(KEY_PRESET_Z0, getString(R.string.default_z0));
        String gamma1 = preferences.getString(KEY_PRESET_GAMMA1, getString(R.string.default_gamma1));
        String gamma2 = preferences.getString(KEY_PRESET_GAMMA2, getString(R.string.default_gamma2));

        updateZ0(z0);
        gammaSolver1.setGamma(Double.parseDouble(gamma1));
        gammaSolver2.setGamma(Double.parseDouble(gamma2));
        reportChange();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our visualizer_menu layout to this menu */
        inflater.inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public void reportChange() {
        gammaSolver1.updateViews();
        gammaSolver2.updateViews();
        mismatchSolver.updateViews(gammaSolver1.getGamma(), gammaSolver2.getGamma());
    }

    void updateZ0(String z0_string) {
        et_z0.setText(z0_string);
        double double_z0 = Double.parseDouble(z0_string);
        gammaSolver1.set_z0(double_z0);
        gammaSolver2.set_z0(double_z0);
    }

    // this handles the Z0 edittext changes
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if(actionId == IME_ACTION_DONE) {
            String input = v.getText().toString();
            Double value = Double.parseDouble(input);
            hideSoftKeyboard(this);
            InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            gammaSolver1.set_z0(value);
            gammaSolver2.set_z0(value);
            reportChange();

            return true;
        }
        return false;
    }


    public static void hideSoftKeyboard(Activity activity) {
//        InputMethodManager inputMethodManager =
//                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
//        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        Log.d(TAG, "onTouch: entered");
//        hideSoftKeyboard(this);
//        return false;
//    }

    void saveSharedPreferences(double z0, double gamma1, double gamma2) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PRESET_Z0, Double.toString(z0));
        editor.putString(KEY_PRESET_GAMMA1, Double.toString(gamma1));
        editor.putString(KEY_PRESET_GAMMA2, Double.toString(gamma2));
        editor.apply();
    }

    public void menuHandler(MenuItem item) {
        if (item.getItemId() == R.id.action_preset) {
            preset();

        } else if (item.getItemId() == R.id.action_save) {
            saveSharedPreferences(gammaSolver1.getZ0(), gammaSolver1.getGamma(), gammaSolver2.getGamma());
            Toast toast = Toast.makeText(this, "Preset-values stored!", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
            toast.show();


        } else if (item.getItemId() == R.id.action_revert) {
            updateZ0(getString(R.string.default_z0));
            gammaSolver1.setGamma(Double.parseDouble(getString(R.string.default_gamma1)));
            gammaSolver2.setGamma(Double.parseDouble(getString(R.string.default_gamma2)));
            reportChange();

        } else if (item.getItemId() == R.id.action_help) {
            showHelp();
        }

        else if (item.getItemId() == R.id.action_about) {
            showAbout();
       }
    }

    private String readAssetFile(String fileName) {
        InputStream is;
        int size = 0;
        String str = "";
        try {
            is = getAssets().open(fileName);
            size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            str = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return str;
    }

    private void showAbout() {
        final boolean SHOW_AD_IN_ABOUT = false;

        View view = View.inflate(this, R.layout.help_dialog, null);
        WebView webView = (WebView) view.findViewById(R.id.tv_htmlText);

        String str = readAssetFile("about_html.html");

        String replaced = str.replace("__BVN__", BuildConfig.VERSION_NAME);
        str = replaced.replace("__BVC__", Integer.toString(BuildConfig.VERSION_CODE));
        webView.loadDataWithBaseURL("file:///android_asset/", str, "text/html; charset=utf-8", "utf-9", null);

        final AdView helpAd = (AdView) view.findViewById(R.id.adView_help);

        if (SHOW_AD_IN_ABOUT) {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();
            helpAd.loadAd(adRequest);
        } else {
            helpAd.setVisibility(View.GONE);
        }

        // fill in the title bar
        View titleView = View.inflate(this, R.layout.dialog_title, null);
        ImageView back = (ImageView) titleView.findViewById(R.id.iv_back);
        TextView title = (TextView) titleView.findViewById(R.id.tv_title);
        title.setText("About");


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCustomTitle(titleView);
        builder.setView(view);
        final Dialog dialog = builder.create();

        // attach back button
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private void showHelp() {
        View view = View.inflate(this, R.layout.help_dialog, null);

        WebView webView = (WebView) view.findViewById(R.id.tv_htmlText);
        final AdView helpAd = (AdView) view.findViewById(R.id.adView_help);
        String str = readAssetFile("help_html.html");
        webView.loadDataWithBaseURL("file:///android_asset/", str, "text/html; charset=utf-8", "utf-9", null);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        helpAd.loadAd(adRequest);

        // fill in the title bar
        View titleView = View.inflate(this, R.layout.dialog_title, null);
        ImageView back = (ImageView) titleView.findViewById(R.id.iv_back);
        TextView title = (TextView) titleView.findViewById(R.id.tv_title);
        title.setText("Help");


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCustomTitle(titleView);
        builder.setView(view);
        final Dialog dialog = builder.create();

        // attach back button
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
