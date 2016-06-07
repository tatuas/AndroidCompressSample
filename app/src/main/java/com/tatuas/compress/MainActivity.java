package com.tatuas.compress;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import icepick.Icepick;
import icepick.State;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private TextView console;
    private Button execute;

    @State
    ArrayList<String> logs;
    @State
    boolean processing;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final ArrayList<String> received = intent.getStringArrayListExtra(CompressService.EXTRA_LOGS);
            if (received != null) {
                logs.addAll(received);
                exhaustLogs();
            }

            processing = false;
            progressDialog.dismiss();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            processing = false;
            logs = new ArrayList<>();
        } else {
            Icepick.restoreInstanceState(this, savedInstanceState);
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        console = (TextView) findViewById(R.id.log);
        execute = (Button) findViewById(R.id.execute);
        execute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processing = true;
                progressDialog.show();
                startService(CompressService.createIntent(getApplicationContext()));
            }
        });

        LocalBroadcastManager
                .getInstance(getApplicationContext())
                .registerReceiver(receiver, new IntentFilter(CompressService.NAME_INTENT_FILTER));

        if (processing) {
            progressDialog.show();
        }

        exhaustLogs();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager
                .getInstance(getApplicationContext())
                .unregisterReceiver(receiver);

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    private void exhaustLogs() {
        console.setText(TextUtils.join("\n", logs));
    }
}
