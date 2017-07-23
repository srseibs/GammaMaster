package com.sailinghawklabs.gammamaster;

import android.view.View;
import android.widget.TextView;

/**
 * Created by Mike on 7/15/2017.
 */

public class MismatchSolver {
    private TextView tv_mismatchErrorMagPlus;
    private TextView tv_mismatchErrorMagMinus;
    private TextView tv_mismatchErrorPhase;

    public void setView_MAG_PLUS(View v) {
        tv_mismatchErrorMagPlus = (TextView) v;
    }

    public void setView_MAG_MINUS(View v) {
        tv_mismatchErrorMagMinus = (TextView) v;
    }

    public void setView_PHASE(View v) {
        tv_mismatchErrorPhase = (TextView) v;
    }

    public void updateViews(double gamma1, double gamma2) {
        double mismatchMag_plus = 20.0 * Math.log10(1 + gamma1 * gamma2);
        double mismatchMag_minus = 20.0 * Math.log10(1 - gamma1 * gamma2);
        double mismatchPhase_deg = gamma1 * gamma2 * 180.0 / Math.PI;

        tv_mismatchErrorMagPlus.setText(String.format("%+.2f", mismatchMag_plus));
        tv_mismatchErrorMagMinus.setText(String.format("%+.2f", mismatchMag_minus));
        tv_mismatchErrorPhase.setText(String.format("%.2f", mismatchPhase_deg));
    }
}
