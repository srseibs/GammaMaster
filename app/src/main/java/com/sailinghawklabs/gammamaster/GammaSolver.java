package com.sailinghawklabs.gammamaster;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI;

/**
 * Created by Mike on 7/14/2017.
 */

public class GammaSolver implements TextView.OnEditorActionListener, View.OnFocusChangeListener {
    Context mContext;

    public interface NotifyCallback {
        void reportChange();
    }

    private NotifyCallback myListener;

    public GammaSolver(NotifyCallback listener, Context context) {
        this.myListener = listener;
        this.mContext = context;
    }

    private static final String TAG = GammaSolver.class.getName();
    private double z0;
    EditText et_z0;

    private double returnLoss_dB;
    EditText et_returnLoss;

    private double gamma;
    EditText et_gamma;

    private double vswr;
    EditText et_vswr;

    private double rLoad_gtZo;
    EditText et_rload_gt;

    private double rLoad_ltZo;
    EditText et_rload_lt;

    private double mismatchLoss_dB;
    EditText et_mismatchLoss;


    protected void setUpListener(EditText v) {
        Log.d(TAG, "setUpListener: entered " + v.getResources().getResourceName(v.getId()));
        v.setImeOptions(IME_ACTION_DONE | IME_FLAG_NO_EXTRACT_UI );
        v.setOnFocusChangeListener(this);
        v.setOnEditorActionListener(this);
        v.setSelectAllOnFocus(true);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Log.d(TAG, "onFocusChange: view = " + v.getResources().getResourceName(v.getId()) + ", bool = " + hasFocus );
        if (hasFocus == false) {
            enterValue((EditText)v);
            v.clearFocus();
            myListener.reportChange();
        } else {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void enterValue(EditText v)
    {
        Double value = null;
        String valueString = v.getText().toString();
        try {
            value = Double.parseDouble(valueString);
        } catch (NumberFormatException err) {
            Toast toast = Toast.makeText(v.getContext(), "Invalid number format found!", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
            toast.show();
        }

        Log.d(TAG, "enterValue: value = "+ String.valueOf(value) + ", valueString= " + valueString + ", id: " + v.getResources().getResourceName(v.getId()));

        if (value == null) {
            return;
        }

        int viewId = v.getId();
        if (et_gamma.getId() == viewId) {
            setGamma(value);

        } else if (et_returnLoss.getId() == viewId) {
            set_returnLoss(value);

        } else if (et_mismatchLoss.getId() == viewId) {
            setMismatchLoss_dB(value);

        } else if (et_rload_gt.getId() == viewId) {
            setrLoad_gtZo(value);

        } else if (et_rload_lt.getId() == viewId) {
            setrLoad_ltZo(value);

        } else if (et_vswr.getId() == viewId) {
            setVswr(value);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        Log.d(TAG, "onEditorAction: entered, actionId = " + actionId + ", KeyEvent = " + event);
        v.clearFocus();
        if(actionId == EditorInfo.IME_ACTION_DONE) {
            enterValue((EditText)v);

            InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            myListener.reportChange();

            return true; // consumed.
        }

        return false; // pass on to other listeners.
    }



    public void updateViews() {
        et_z0.setText(String.format("%.1f", z0));
        et_returnLoss.setText(String.format("%.2f", returnLoss_dB));
        et_gamma.setText(String.format("%.3f", gamma));
        et_vswr.setText(String.format("%.3f", vswr));
        et_rload_gt.setText(String.format("%.1f", rLoad_gtZo));
        et_rload_lt.setText(String.format("%.1f", rLoad_ltZo));
        et_mismatchLoss.setText(String.format("%.2f", mismatchLoss_dB));
    }

// update individual values and calculate the rest

    // *****************************  Gamma is the master -- it recalculates all the others
    // *****************************
    public void setGamma(double g) {
        gamma = g;
        rLoad_gtZo = z0 * (1 + gamma) / (1 - gamma);
        rLoad_ltZo = z0 * (1 - gamma) / (1 + gamma);
        vswr = (1.0 + gamma) / (1.0 - gamma);
        returnLoss_dB = -20.0 * Math.log10(gamma);
        mismatchLoss_dB = -10.0 * Math.log10(1 - gamma * gamma);
    }

    public void set_z0(double z0) {
        this.z0 = z0;
        setGamma(gamma);// force recalculate;
    }

    public void set_returnLoss(double rl) {
        gamma = Math.pow(10.0, -rl / 20);
        setGamma(gamma);// force recalculate;
    }

    public void setVswr(double vswr) {
       setGamma((vswr - 1.0)/(vswr + 1.0));
    }

    public void setrLoad_ltZo(double r) {
        setGamma( (z0 - r) / (r + z0) );
    }

    public void setrLoad_gtZo(double r) {
        setGamma( (r - z0) / (r + z0) );
    }

    public void setMismatchLoss_dB(double loss) {
        setGamma(  Math.sqrt(  (1 - Math.pow(10, -loss/10.0))));

        Log.d(TAG, "setMismatchLoss_dB: loss = " + loss + ", calculated: " + mismatchLoss_dB );

    }


    public double getReturnLoss_dB() {
        return returnLoss_dB;
    }

    public double getGamma() {
        return gamma;
    }

    public double getVswr() {
        return vswr;
    }

    public double getrLoad_ltZo() {
        return rLoad_ltZo;
    }

    public double getrLoad_gtZo() {
        return rLoad_gtZo;
    }

    public double getMismatchLoss_dB() {
        return mismatchLoss_dB;
    }

    public double getZ0() {
        return z0;
    }

// setup all the views that we maintain

    public void setView_Z0(View v) {
        this.et_z0 = (EditText) v;
        // no listener for Z0 since it needs to be shared between more than one class
    }

    public void setView_RL(View v) {
        this.et_returnLoss = (EditText) v;
        setUpListener(et_returnLoss);
    }

    public void setView_GAMMA(View v) {
        this.et_gamma = (EditText) v;
        setUpListener(et_gamma);
    }

    public void setView_VSWR(View v) {
        this.et_vswr = (EditText) v;
        setUpListener(et_vswr);
    }

    public void setView_RLOADGT(View v) {
        this.et_rload_gt = (EditText) v;
        setUpListener(et_rload_gt);
    }

    public void setView_RLOADLT(View v) {
        this.et_rload_lt = (EditText) v;
        setUpListener(et_rload_lt);
    }

    public void setView_MISMATCHLOSS(View v) {
        this.et_mismatchLoss = (EditText) v;
        setUpListener(et_mismatchLoss);
    }
}


