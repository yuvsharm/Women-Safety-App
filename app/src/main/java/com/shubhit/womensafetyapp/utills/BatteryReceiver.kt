package com.shubhit.womensafetyapp.utills

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager

class BatteryReceiver(private val context: Context,
                      private val onLowBattery: () -> Unit // Lambda function to handle low battery alert
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        if (level <= 7 && !Preferences.isLowBatteryAlertSent)  {
            onLowBattery() // Trigger the lambda function
            Preferences.isLowBatteryAlertSent=true
        }
    }
}