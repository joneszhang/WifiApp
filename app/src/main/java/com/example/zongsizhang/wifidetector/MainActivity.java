package com.example.zongsizhang.wifidetector;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button b_start = null;
    private Button b_stop = null;
    private Button b_record = null;
    private TextView t_time = null;
    private ListView list_record = null;

    private WifiManager wifiManager = null;

    public int MONITOR_OFF = 0;
    public int MONITOR_ON = 1;
    private int monitor_state = 0;

    private long initial_time = 0;
    private AsyncTask ongotask = null;

    public DBManager dbManager = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.initView();

        wifiManager= (WifiManager) getSystemService(WIFI_SERVICE); //init wifimanager
        dbManager = new DBManager(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //in case that activity end before asynctask
        ongotask.cancel(true);
        dbManager.closeDB();
    }

    public void initView(){
        b_start = (Button)this.findViewById(R.id.but_start);
        b_stop = (Button)this.findViewById(R.id.but_stop);
        b_record = (Button)this.findViewById(R.id.but_record);
        t_time = (TextView)this.findViewById(R.id.t_time);
        list_record = (ListView)this.findViewById(R.id.list_record);
        t_time.setText("");

        b_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (monitor_state == MONITOR_ON) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Already on Monitor", Toast.LENGTH_SHORT);
                    toast.show();
                }
                if (isWifiEnable()) {
                    initial_time = System.currentTimeMillis();
                    WifiDetectTask task_wifi = new WifiDetectTask();
                    ongotask = task_wifi;
                    monitor_state = MONITOR_ON;
                    t_time.setText("Monitor on");
                    list_record.setVisibility(View.INVISIBLE);
                    dbManager.clearWifiData();
                    task_wifi.execute();
                } else {
                    t_time.setText("Wifi is not enable yet, please check your wifi status");
                }
            }
        });

        b_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (monitor_state == MONITOR_OFF) return;
                monitor_state = MONITOR_OFF;
                t_time.setText("Disabling the Monitor");
            }
        });

        b_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (monitor_state == MONITOR_ON) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please stop monitor first", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                } else if (monitor_state == MONITOR_OFF) {
                    t_time.setText("reading records");
                    ReadWifiTask readtask = new ReadWifiTask();
                    ongotask = readtask;
                    readtask.execute();
                }

            }
        });


    }

    private class WifiDetectTask extends AsyncTask<String, Integer, String> {
        private long offset = 30;

        @Override
        protected String doInBackground(String... params) {
            recordWifi();
            for(int i = 0;i < offset; ++i){
                if(monitor_state == MONITOR_OFF) return null;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            super.onPreExecute();
            long curtime = System.currentTimeMillis();
            if(monitor_state == MONITOR_ON && curtime - initial_time < 600000){
                WifiDetectTask newtask = new WifiDetectTask();
                ongotask = newtask;
                newtask.execute();
            }else{
                t_time.setText("monitor end");
                monitor_state = MONITOR_OFF;
            }
        }
    }

    private class ReadWifiTask extends AsyncTask<String, Integer, String[]> {
        @Override
        protected void onPostExecute(String[] data) {
            super.onPostExecute(data);
            if(data[0] == ""){
                t_time.setText("no record");
            }else{
                t_time.setText("reading completed");
                list_record.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, data));
                list_record.setVisibility(View.VISIBLE);
            }

        }

        @Override
        protected String[] doInBackground(String... params) {

            List<WifiData> dataset = dbManager.queryWifiRecords();
            if(dataset.size() == 0) return new String[]{""};
            String[] textdata = new String[dataset.size()];
            for(int i = 0;i < dataset.size(); ++i){
                textdata[i] = dataset.get(i).toText();
            }
            return textdata;
        }
    }

    public boolean isWifiEnable(){
        int wstate = wifiManager.getWifiState();
        if(wstate == WifiManager.WIFI_STATE_ENABLED) return true;
        else return false;
    }

    public void recordWifi(){
        int wifistate = wifiManager.getWifiState();
        //wifiManager.getConnectionInfo();
        List<ScanResult> scanResults= wifiManager.getScanResults();
        for(ScanResult result : scanResults){
            //System.out.println("ssid: "+ result.SSID + "|| signal level: " + result.level + "|| address" + result.BSSID);
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = new Date();
            String dt = dateFormat.format(date);
            dbManager.pushWifiData(dt, result.SSID, result.level, result.BSSID);
        }
    }


}
