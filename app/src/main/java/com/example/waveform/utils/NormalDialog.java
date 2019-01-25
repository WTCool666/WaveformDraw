package com.example.waveform.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

public class NormalDialog extends DialogFragment {

    private final String SHOW_TAG="NormalDialog";
    private String title=null;
    private String message=null;
    private DialogInterface.OnClickListener click=null;
    private boolean isNeedCancel=true;
    public void show(FragmentManager fm, String title, String message, DialogInterface.OnClickListener click){
        this.title=title;
        this.message=message;
        this.click=click;
        show(fm, SHOW_TAG);
    }

    public void setNeedCancel(boolean isNeedCancel){
        this.isNeedCancel=isNeedCancel;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        if (title!=null)
        builder.setTitle(this.title);
        if (message!=null)
        builder.setMessage(message);
        if (this.click!=null){
            builder.setPositiveButton(android.R.string.ok, click);
            if (isNeedCancel){
                builder.setNegativeButton(android.R.string.cancel, click);
            }
        }
        return builder.create();
    }
}
