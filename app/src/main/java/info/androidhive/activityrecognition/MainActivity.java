package info.androidhive.activityrecognition;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {
    CheckBox check1,check2,check3,check4;
    Button buttonSubmit;
    String imei;
    LocationManager locationManager;
    private static final int REQUEST_CODE = 101;
    //private double locationLatitude,locationLongitude;
    String locationText = "";
    String locationLatitude = "";
    String locationLongitude = "";

    private int mInterval = 3000; // 3 seconds by default, can be changed later
    private Handler mHandler;


    private String TAG = MainActivity.class.getSimpleName();
    BroadcastReceiver broadcastReceiver;

    private TextView txtActivity, txtConfidence;
    private ImageView imgActivity;
    private Button btnStartTrcking, btnStopTracking, btnSurvay;

    private DatabaseReference myRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("IMEI",""+"hi");
        TelephonyManager telMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Log.e("IMEI",""+"hi2");
        if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE);
            Log.e("IMEI",""+"hi3");
            return;
        }
        Log.e("IMEI",""+"hi4");
        imei = telMgr.getDeviceId();
        Log.e("IMEI",""+imei);

        txtActivity = findViewById(R.id.txt_activity);
        txtConfidence = findViewById(R.id.txt_confidence);
        imgActivity = findViewById(R.id.img_activity);
        btnStartTrcking = findViewById(R.id.btn_start_tracking);
        btnStopTracking = findViewById(R.id.btn_stop_tracking);
           btnSurvay=findViewById(R.id.survay);
        myRef = FirebaseDatabase.getInstance().getReference("location").child(imei);

        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            public void run() {
                mHandler = new Handler();
                startRepeatingTask();
            }
        }, 5000);   //5 seconds
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

        }
        btnStartTrcking.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
               startTracking();
//                Intent intent1 = new Intent(MainActivity.this, OfflineGPS.class);
//                intent1.putExtra("Id", 7);
//                MainActivity.this.startActivity(intent1);
                            }
        });

        btnStopTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTracking();
            }
        });
        btnSurvay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(MainActivity.this, WebViewActivity.class);
                intent1.putExtra("IMEI", imei);
                MainActivity.this.startActivity(intent1);
            }
        });


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    handleUserActivity(type, confidence);
                }
            }
        };

        startTracking();
        addListenerOnButtonClick();
    }
        @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void handleUserActivity(int type, int confidence) {
        String label = getString(R.string.activity_unknown);
        int icon = R.drawable.ic_still;

        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                label = getString(R.string.activity_in_vehicle);
                icon = R.drawable.ic_driving;
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                label = getString(R.string.activity_on_bicycle);
                icon = R.drawable.ic_on_bicycle;
                break;
            }
            case DetectedActivity.ON_FOOT: {
                label = getString(R.string.activity_on_foot);
                icon = R.drawable.ic_walking;
                break;
            }
            case DetectedActivity.RUNNING: {
                label = getString(R.string.activity_running);
                icon = R.drawable.ic_running;
                break;
            }
            case DetectedActivity.STILL: {
                label = getString(R.string.activity_still);
                break;
            }
            case DetectedActivity.TILTING: {
                label = getString(R.string.activity_tilting);
                icon = R.drawable.ic_tilting;
                break;
            }
            case DetectedActivity.WALKING: {
                label = getString(R.string.activity_walking);
                icon = R.drawable.ic_walking;
                break;
            }
            case DetectedActivity.UNKNOWN: {
                label = getString(R.string.activity_unknown);
                break;
            }
        }

        Log.e(TAG, "User activity: " + label + ", Confidence: " + confidence);

        if (confidence > Constants.CONFIDENCE) {
            txtActivity.setText(label);
            txtConfidence.setText("Confidence: " + confidence);
            imgActivity.setImageResource(icon);
            if(locationLatitude != null && locationLongitude != null){
                info.androidhive.activityrecognition.Location location = new info.androidhive.activityrecognition.Location(locationLatitude,locationLongitude,label);
                myRef.push().setValue(location).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            Toast.makeText(MainActivity.this, "Location is updated", Toast.LENGTH_SHORT).show();
                        }else {

                            Toast.makeText(MainActivity.this, "Location is not updated", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
    @Override
    public void onLocationChanged(Location location) {

        locationText = location.getLatitude() + "," + location.getLongitude();
        locationLatitude = location.getLatitude() + "";
        locationLongitude = location.getLongitude() + "";
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(MainActivity.this, "Please Enable GPS", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {

            final EditText yourlat = (EditText) findViewById(R.id.yourLat);
            final EditText yourlong = (EditText) findViewById(R.id.yourLong);

            try {
                getLocation(); //this function can change value of mInterval.

                if (locationText.toString() == "") {
                    Toast.makeText(getApplicationContext(), "Trying to retrieve coordinates.", Toast.LENGTH_LONG).show();
                }
                else {

                    yourlat.setText(locationLatitude.toString());
                    yourlong.setText(locationLongitude.toString());
                }
            } finally {

                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };
    void startRepeatingTask() {
        mStatusChecker.run();
    }


    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    private void startTracking() {
        Intent intent = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        startService(intent);
    }
    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, (LocationListener) this);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    private void stopTracking() {
        Intent intent = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        stopService(intent);
    }
    //reverse geocoding
//    private void LocationAddress() throws IOException {
//        Log.d("add12345", "Value: " + locationLatitude);
//        Geocoder geocoder;
//        List<Address> addresses;
//        geocoder = new Geocoder(this, Locale.getDefault());
//
//        addresses = geocoder.getFromLocation(locationLatitude, locationLongitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
//
//        String address = addresses.get(0).getAddressLine(0);// If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//
//        String city = addresses.get(0).getLocality();
//        String state = addresses.get(0).getAdminArea();
//        String country = addresses.get(0).getCountryName();
//        String postalCode = addresses.get(0).getPostalCode();
//        String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
//
//       // editTextAddress.setText(address);
//    }
public void addListenerOnButtonClick(){
    //Getting instance of CheckBoxes and Button from the activty_main.xml file
    check1=(CheckBox)findViewById(R.id.checkBox);
    check2=(CheckBox)findViewById(R.id.checkBox2);
    check3=(CheckBox)findViewById(R.id.checkBox3);
    check4=(CheckBox)findViewById(R.id.checkBox4);
    buttonSubmit=(Button)findViewById(R.id.button);

    //Applying the Listener on the Button click
    buttonSubmit.setOnClickListener(new View.OnClickListener(){

        @Override
        public void onClick(View view) {

            StringBuilder result=new StringBuilder();
            result.append("Selected options:");
            if(check1.isChecked()){
                result.append("\ndifficult to sleep");

            }
            if(check2.isChecked()){
                result.append("\nEarly morning awakening");

            }
            if(check3.isChecked()){
                result.append("\nAwakening time to time");

            }
            if(check4.isChecked()){
                result.append("\nnone of above");

            }

            //Displaying the message on the toast
            Toast.makeText(getApplicationContext(), result.toString(), Toast.LENGTH_LONG).show();
        }

    });
}
}
