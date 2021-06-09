package com.fallsafe.epilepsycare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fallsafe.epilepsycare.Constants.Constants;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String actionIntent = intent.getAction();
        assert actionIntent != null;
        if(actionIntent.equals(Constants.FALL_CONFIRMATION_ACTION_CANCEL)){
            MyService.isFallTaskCancelled = true;
        }

    }
}
