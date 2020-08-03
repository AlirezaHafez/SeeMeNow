package ca.ualberta.hafez.seeme;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor pressureSensor;
    private TextView txt;
    private TextView sum;
    private int recordTime = 1;
    private File file;
    private StringBuilder dataBuffer;
    private StringBuilder lastEventsdataBuffer;
    private boolean record;
    private boolean lastEvent;
    private String lastRecord;
    private String lastTimeStamp;
    private Activity activity;
    private SizedStack lastEvents;
    private int testCounter;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txt = findViewById(R.id.txt);
        sum = findViewById(R.id.sum);
        activity = this;
        record = false;
        lastEvent = false;
        testCounter = 0;
        sum.setText(String.valueOf(testCounter));
        lastEvents = new SizedStack<String>(26);
        dataBuffer = new StringBuilder(8192);
        lastEventsdataBuffer = new StringBuilder(16384);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        pressureSensor.isAdditionalInfoSupported();
        sensorManager.registerListener(sensorEventListener, pressureSensor, 1);
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float[] values = sensorEvent.values;
            lastRecord = String.format("%.3f", values[0]);
            Long tsLong = sensorEvent.timestamp;
            int accuracy = sensorEvent.accuracy;
            lastTimeStamp = tsLong.toString();
            lastEvents.push(lastTimeStamp + ", " + lastRecord + ", " + String.valueOf(accuracy) + "\n");
            if (record) {
                if (lastEvent) {
                    lastEvent = false;
                    List<String> recs = new ArrayList<String>(lastEvents);
                    for (String rec : recs) {
                        dataBuffer.append(rec);
                    }
                } else {
                    dataBuffer.append(lastTimeStamp + ", " + lastRecord + ", " + String.valueOf(accuracy) + "\n");
                }
            }
            txt.setText(lastRecord);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };


    private void logData(File file, String data) {
        try {
            FileOutputStream fileinput = new FileOutputStream(file, true);
            PrintStream printstream = new PrintStream(fileinput);
            printstream.print(data);
            fileinput.close();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void record(View v) {
        findViewById(R.id.b1).setEnabled(false);
        findViewById(R.id.b2).setEnabled(false);
        findViewById(R.id.b3).setEnabled(false);
        findViewById(R.id.b4).setEnabled(false);
        findViewById(R.id.b5).setEnabled(false);
        findViewById(R.id.b6).setEnabled(false);
        findViewById(R.id.b7).setEnabled(false);
        findViewById(R.id.b8).setEnabled(false);
        findViewById(R.id.b9).setEnabled(false);
        testCounter++;
        sum.setText(String.valueOf(testCounter));
        record = true;
        lastEvent = true;
        dataBuffer.setLength(0);
        Long tsLong = System.currentTimeMillis() / 1000;
        String fName = v.getTag() + "_" + String.valueOf(recordTime) + "_" + tsLong.toString() + ".txt";
        verifyStoragePermissions(activity);
        File folder = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "see_me");
        folder.mkdirs();
        file = new File(folder.getPath() + File.separator + fName);
        new CountDownTimer(recordTime * 1000, 20) {
            public void onTick(long millisUntilFinished) {
//                counterView.setText(String.valueOf(millisUntilFinished / 1000.000));
            }

            public void onFinish() {
                record = false;
                lastEvent = false;
                logData(file, dataBuffer.toString());
                dataBuffer.setLength(0);
                findViewById(R.id.b1).setEnabled(true);
                findViewById(R.id.b2).setEnabled(true);
                findViewById(R.id.b3).setEnabled(true);
                findViewById(R.id.b4).setEnabled(true);
                findViewById(R.id.b5).setEnabled(true);
                findViewById(R.id.b6).setEnabled(true);
                findViewById(R.id.b7).setEnabled(true);
                findViewById(R.id.b8).setEnabled(true);
                findViewById(R.id.b9).setEnabled(true);
//                counterView.setText(String.valueOf(recordTime));
            }

        }.start();
    }
}
