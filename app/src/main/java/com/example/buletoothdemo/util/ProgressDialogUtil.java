package com.example.buletoothdemo.util;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by aiyang on 2017/12/20.
 */

public class ProgressDialogUtil {
    public static ProgressDialog mProgressDialog;

    public static void show(Context context,String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.setMessage(message);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    public static void cancel(){
        if (mProgressDialog!=null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }
}
