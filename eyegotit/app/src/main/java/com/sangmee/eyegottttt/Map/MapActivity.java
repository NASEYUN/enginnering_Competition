package com.sangmee.eyegottttt.Map;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Align;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.LocationButtonView;
import com.sangmee.eyegottttt.FirstviewActivity;
import com.sangmee.eyegottttt.R;
import com.sangmee.eyegottttt.SpeakVoiceActivity;
import com.sangmee.eyegottttt.SplashActivity;
import com.sangmee.eyegottttt.TrafficLight.CameraActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, TextToSpeech.OnInitListener {

    public static final String CHANNEL_ID = "notificationChannel";
    private DatabaseReference databaseReference;
    Intent intent;
    Intent intentId;
    String s_location, user_id;
    ArrayList<Double> longitude_list = new ArrayList<>();
    ArrayList<Double> latitude_list = new ArrayList<>();
    ArrayList<LatLng> latlng_list = new ArrayList<>();
    String message[] = new String[20];
    boolean location_changed=false;

    Handler delayHandler = new Handler();

    Location[] saved_location;

    int[] intArray = {100, 100, 100, 100, 100, 100, 100, 100, 100, 100};
    int confirm_num = 0;
    int confirm = 0;

    String spot_str = "p";
    String message_str = "p";
    int index = 1000;
    int i_index;
    ArrayList<Integer> indexList = new ArrayList<>();

    //?????? ??????
    // NaverMap API 3.0
    private MapView mapView;
    private LocationButtonView locationButtonView;

    double longitude = 0;
    double latitude = 0;

    NaverMap naverMap;
    ArrayList<Marker> arrayList1 = new ArrayList<>();

    Marker marker_location = new Marker();
    // FusedLocationSource (Google)
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    ProtecterMapActivity2 protecterMapActivity2;

    //mqtt ??????
    private ImageButton alert_btn;
    String topicStr = "???????????? ?????????????????????.";
    String topicStr2="?????? ????????????!!!";
    String topic_value;
    MqttAndroidClient client;
    double d_longi;
    double d_lati;
    Context context = this;

    SpeakVoiceActivity voiceActivity;
    TextToSpeech tts;


    SharedPreferences.Editor editor;
    String lati;
    String longi;
    Marker marker=new Marker();

    SharedPreferences.OnSharedPreferenceChangeListener mPrefChangeListener=new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            //SharedPreferences tmsg = getSharedPreferences("tmsg", MODE_PRIVATE);
            //String lati_L = tmsg.getString("latitude", "");
            //String longi_L = tmsg.getString("longitude", "");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final int MOVE_HAND=350;//????????? ????????????
        final float[] sx = new float[1]; //????????????
        final float[] sy = new float[1];
        final float[] ssx = new float[1];
        final float[] ssy = new float[1];


        setTheme(R.style.noactionbar);
        setContentView(R.layout.activity_map);

        intent = getIntent();
        intentId = getIntent();
        s_location = intent.getStringExtra("s_location");
        user_id = intent.getStringExtra("id");


        //??? ??????
        mapView = findViewById(R.id.main_map_view);
        mapView.onCreate(savedInstanceState);
        naverMapBasicSettings();
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


            return;
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // ????????? ???????????????
                100, // ??????????????? ?????? ???????????? (miliSecond)
                1, // ??????????????? ?????? ???????????? (m)
                mLocationListener);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // ????????? ???????????????
                100, // ??????????????? ?????? ???????????? (miliSecond)
                1, // ??????????????? ?????? ???????????? (m)
                mLocationListener);
        // Toast.makeText(practice.this,(int)longtitude,Toast.LENGTH_SHORT).show();

        //  lm.removeUpdates(mLocationListener);
        //????????????

        tts = new TextToSpeech(MapActivity.this, MapActivity.this);
        voiceActivity = new SpeakVoiceActivity(MapActivity.this, tts);

        //mqtt ??????!!!!!!!!!!!!!!!!!!!!!!

        databaseReference = FirebaseDatabase.getInstance().getReference();
        Query recentPostsQuery = databaseReference.child(user_id).child("topic");
        recentPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                topic_value = dataSnapshot.getValue().toString();
                Log.d("sangminTopic", topic_value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(MapActivity.this, "tcp://broker.hivemq.com:1883", clientId);

        alert_btn = (ImageButton) findViewById(R.id.alert_btn);
        //connect?????? ??????
        try {
            IMqttToken token = client.connect(getMqttConnectionOption());
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) { //????????? ????????? ??????
                    Log.v("SEYUN_TAG", "connection1");
                    try {
                        client.subscribe(topic_value, 0); //topic??? ??????.
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) { //????????? ????????? ??????
                    Toast.makeText(MapActivity.this, "????????? ?????????????????????...(1)", Toast.LENGTH_SHORT).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        SharedPreferences tmsg = getSharedPreferences("tmsg", MODE_PRIVATE);
        tmsg.registerOnSharedPreferenceChangeListener(mPrefChangeListener);
        editor = tmsg.edit();

        alert_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent e) {
                if(e.getAction() == MotionEvent.ACTION_DOWN){
                    sx[0] = e.getRawX();
                    sy[0] = e.getRawY();
                }
                if(e.getAction() == MotionEvent.ACTION_MOVE){
                    ssx[0] = e.getRawX();
                    ssy[0] = e.getRawY();
                }
                else if(e.getAction() == MotionEvent.ACTION_UP){
                    float diffxx = sx[0] -e.getRawX();
                    float diffyy = sy[0] - e.getRawY();
                    if(Math.abs(diffxx)>Math.abs(diffyy)){
                        if(diffxx>MOVE_HAND) {
                            topicStr = topicStr + "####" + latitude + "####" + longitude + "####?????????####";
                            String msg = new String(topicStr);
                            String word1 = msg.split("####")[0];
                            lati = msg.split("####")[1];
                            longi = msg.split("####")[2];
                            String user= msg.split("####")[3];

                            Log.v("SEYUN_TAG", lati);
                            Log.v("SEYUN_TAG", longi);

                            int qos = 0;
                            try {
                                IMqttToken subToken = client.publish(topic_value, topicStr.getBytes(), qos, false);
                                subToken.setActionCallback(new IMqttActionListener() {
                                    @Override
                                    public void onSuccess(IMqttToken asyncActionToken) { //????????? ????????? ??????
                                        Log.v("SEYUN_TAG", "connection2");
                                        String text = "??????????????? ??????????????? ???????????????.";
                                        Toast.makeText(MapActivity.this, text, Toast.LENGTH_SHORT).show();

                                        //????????? ??????????????? ???????????? ???????????????.
                                        voiceActivity.text = text;
                                        voiceActivity.speekTTS(voiceActivity.text, tts);

                                        topicStr = "???????????? ?????????????????????.";


                                        /*if(location_changed==true){
                                            editor.putString("latitude_changed",lati);
                                            editor.putString("longtitude_chagned",longi);
                                        }*/
                                        //else if(location_changed==false) {
                                            editor.putString("latitude", lati);
                                            editor.putString("longitude", longi);
                                        //}
                                        //editor.apply();
                                        editor.commit();
                                        Log.v("SEYUN_TAG", "???????????????");

                                        //createNotification();

                                    }

                                    @Override
                                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) { //????????? ????????? ??????
                                        Toast.makeText(MapActivity.this, "????????? ?????????????????????...(2)", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (MqttException fe) {
                                fe.printStackTrace();
                            }
                        }
                        else if (diffxx<-MOVE_HAND) {
                            topicStr2 = topicStr2 + "####" + latitude + "####" + longitude + "####?????????####";
                            Log.v("SEYUN_TAG", topicStr2);
                            String msg = new String(topicStr2);
                            String word1 = msg.split("####")[0];
                            String lati = msg.split("####")[1];
                            String longi = msg.split("####")[2];
                            String user= msg.split("####")[3];
                            Log.v("SEYUN_TAG", lati);
                            Log.v("SEYUN_TAG", longi);

                            int qos = 0;
                            try {
                                IMqttToken subToken = client.publish(topic_value, topicStr2.getBytes(), qos, false);
                                subToken.setActionCallback(new IMqttActionListener() {
                                    @Override
                                    public void onSuccess(IMqttToken asyncActionToken) { //????????? ????????? ??????
                                        Log.v("SEYUN_TAG", "connection2");
                                        String text = "?????? ?????? ?????????????????????.";
                                        Toast.makeText(MapActivity.this, text, Toast.LENGTH_SHORT).show();

                                        //????????? ??????????????? ???????????? ???????????????.
                                        voiceActivity.text = text;
                                        voiceActivity.speekTTS(voiceActivity.text, tts);

                                        topicStr2 = "?????? ????????????!!!";

                                        SharedPreferences tmsg = getSharedPreferences("tmsg", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = tmsg.edit();
                                        editor.putString("latitude", lati);
                                        editor.putString("longitude", longi);
                                        editor.apply();
                                        //editor.commit();
                                        Log.v("SEYUN_TAG", "???????????????2");
                                    }

                                    @Override
                                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) { //????????? ????????? ??????
                                        Toast.makeText(MapActivity.this, "????????? ?????????????????????...(2)", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (MqttException fe) {
                                fe.printStackTrace();
                            }
                        }
                    }
                    else {
                        if (diffyy > MOVE_HAND){
                            Intent intent=new Intent(MapActivity.this, CameraActivity.class);
                            startActivity(intent);
                        }
                        else if (diffyy < -MOVE_HAND){
                            Intent intent=new Intent(MapActivity.this, FirstviewActivity.class);
                            intent.putExtra("id", user_id);
                            startActivity(intent);
                        }
                    }
                }
                return true;
            }
        });






        Query recentPostsQuery1 = databaseReference.child(user_id).child("location").child(s_location);
        recentPostsQuery1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String key = dataSnapshot.getKey();
                Log.d("sangminkey", key);
                i_index = 0;
                longitude_list.clear();
                latitude_list.clear();
                long n = dataSnapshot.getChildrenCount();
                Log.d("sangconfirm", "" + n);

                for (DataSnapshot messageData : dataSnapshot.getChildren()) {
                    String data_key = messageData.getKey();
                    Log.d("sangmin", data_key);

                    String sLongitude = messageData.child("sLongitude").getValue().toString();
                    String sLatitude = messageData.child("sLatitude").getValue().toString();
                    Log.d("sangmin", sLongitude);
                    Log.d("sangmin", sLatitude);
                    double d_longitude = Double.parseDouble(sLongitude);
                    double d_latitude = Double.parseDouble(sLatitude);
                    i_index++;
                    longitude_list.add(new Double(d_longitude));
                    latitude_list.add(new Double(d_latitude));
                    latlng_list.add(new LatLng(d_latitude, d_longitude));



                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setWill("aaa", "I am going offline".getBytes(), 1, true);
        return mqttConnectOptions;
    }
    //mqtt???!!!!!!!!!!

    //?????????
    private void createNotification() {
        Log.v("SEYUN_TAG", "??????");

        //????????????
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        Bitmap LargeIconNoti = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        Intent intent = new Intent(this, SplashActivity.class); //????????? ????????? ??????????????? ????????????.
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.background)
                .setLargeIcon(LargeIconNoti) //?????????????????? ??? ??? ???????????????.
                .setContentTitle("??????")
                .setContentText("???????????? ?????? ???????????????!!!")
                .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE) //????????? ????????? ?????????.
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);


        notificationManager.notify(0, builder.build());

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "????????????", NotificationManager.IMPORTANCE_DEFAULT));
        }*/
    }
    //??? ??????

    @Override
    public void onMapReady(@NonNull final NaverMap naverMap) {
        //naverMap.getUiSettings().setLocationButtonEnabled(true);
        this.naverMap = naverMap;
        //naverMap ??????
        /*
        LocationOverlay locationOverlay = naverMap.getLocationOverlay();
        Log.d("sangmin", locationOverlay.getPosition().toString());
        locationOverlay.setVisible(true);
        locationOverlay.setCircleRadius(100);
        locationOverlay.setCircleOutlineWidth(10);
        locationOverlay.setCircleOutlineColor(Color.BLACK);
        */
        locationButtonView.setMap(naverMap);

        // Location Change Listener??? ???????????? ?????? FusedLocationSource ??????
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        //longtitude = locationSource.getLastLocation().getLongitude();
        //latitude = locationSource.getLastLocation().getLatitude();




        saved_location = new Location[longitude_list.size()];
        for (int i = 0; i < longitude_list.size(); i++) {
            saved_location[i] = new Location("point" + i);
            saved_location[i].setLatitude(latitude_list.get(i));
            saved_location[i].setLongitude(longitude_list.get(i));

        }

        for(int i=0; i<longitude_list.size(); i++){
            Marker marker = new Marker();
            marker.setPosition((new LatLng(latitude_list.get(i), longitude_list.get(i))));

            if (i==0) {

                marker.setCaptionText(s_location + " ?????????");
                marker.setCaptionTextSize(16);
                marker.setCaptionColor(Color.BLUE);
                marker.setCaptionAlign(Align.Top);
                marker.setIconTintColor(Color.RED);
            } else if (i==(longitude_list.size()-1)) {

                marker.setCaptionText(s_location + " ?????????");
                marker.setCaptionTextSize(16);
                marker.setCaptionColor(Color.BLUE);
                marker.setCaptionAlign(Align.Top);
                marker.setIconTintColor(Color.BLUE);
            } else {
                marker.setCaptionText("??????"+i);
            }
            marker.setMap(naverMap);
        }


        //????????? ????????? ??????
        PathOverlay path = new PathOverlay();
        List<LatLng> coords = new ArrayList<>();
        for (int j = 0; j < latlng_list.size(); j++) {
            Collections.addAll(coords, latlng_list.get(j));
        }

        CameraPosition cameraPosition = new CameraPosition(latlng_list.get(0), 18);
        naverMap.setCameraPosition(cameraPosition);

        path.setCoords(coords);
        path.setMap(naverMap);


        for(int k = 0; k< saved_location.length; k++) {
            Query recentPostsQuery = databaseReference.child(user_id).child("location").child(s_location).child("??????"+ k).child("message");
            final int finalK = k;
            recentPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    message[finalK] = dataSnapshot.getValue().toString();
                    Log.d("sangminSpod", finalK + String.valueOf(message[finalK]));
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }


    }

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //????????? ???????????? ???????????? ???????????? ????????????.  eds
            //?????? Location ????????? ???????????? ?????? ?????? ????????? ????????? ??????.
            location_changed=true;
            longitude = location.getLongitude(); //??????
            latitude = location.getLatitude();   //??????

           /* String stringlong=Double.toString(longitude);
            String stringlat=Double.toString(latitude);

            editor.putString("latitude", stringlong);
            editor.putString("longitude", stringlat);
            editor.commit();*/

            Location trash_location = new Location("trash");
            trash_location.setLatitude(51.5072);
            trash_location.setLongitude(-0.1275);

            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(latitude, longitude)).animate(CameraAnimation.Easing);
            naverMap.moveCamera(cameraUpdate);

            for (int k = 0; k < saved_location.length; k++) {
                if (location.distanceTo(saved_location[k]) <= 10) {
                    index = k;
                    Log.d("sangminSpot", String.valueOf(index));
                    break;
                }
            }
            for(int i = 0;i<saved_location.length;i++){
                if(index == i){
                    if(index == 0){
                        if(message[1].equals("")){
                            voiceActivity.text="??????????????????. ?????? ???????????? ?????????.";
                            Toast.makeText(MapActivity.this,voiceActivity.text, Toast.LENGTH_SHORT).show();
                        }
                        else{
                            voiceActivity.text="??????????????????. ?????? ???????????? ?????? ?????????" + message[1] + "?????????.";
                            Toast.makeText(MapActivity.this,voiceActivity.text, Toast.LENGTH_SHORT).show();
                        }

                        saved_location[index] = trash_location;
                        index = 1000;
                    }else if(index == saved_location.length-1){
                        voiceActivity.text="??????????????????. ????????? ?????????????????????.";
                        Toast.makeText(MapActivity.this,voiceActivity.text , Toast.LENGTH_SHORT).show();
                        saved_location[index] = trash_location;
                        index = 1000;
                    }else {
                        if(message[index+1].equals("")){
                            voiceActivity.text="??????" + index +"?????????. ?????? ???????????? ?????????.";
                            Toast.makeText(MapActivity.this, voiceActivity.text, Toast.LENGTH_SHORT).show();
                        }
                        else{
                            voiceActivity.text="??????" + index +"?????????. ?????? ???????????? ?????? ????????? " + message[index+1] + "?????????.";
                            Toast.makeText(MapActivity.this, voiceActivity.text, Toast.LENGTH_SHORT).show();
                        }

                        saved_location[index] = trash_location;
                        index = 1000;
                    }
                    voiceActivity.speekTTS(voiceActivity.text,tts);
                }
            }
            /*
            switch(index) {
                case 0:

                    break;
                case 1:
                    Toast.makeText(MapActivity.this, "??????1", Toast.LENGTH_SHORT).show();
                    saved_location[index] = trash_location;
                    index = 100;
                    break;
                case 2:
                    Toast.makeText(MapActivity.this, "??????2", Toast.LENGTH_SHORT).show();
                    saved_location[index] = trash_location;
                    index = 100;
                    break;
                case 3:
                    Toast.makeText(MapActivity.this, "??????3", Toast.LENGTH_SHORT).show();
                    saved_location[index] = trash_location;
                    index = 100;
                    break;
                case 4:
                    Toast.makeText(MapActivity.this, "??????4", Toast.LENGTH_SHORT).show();
                    saved_location[index] = trash_location;
                    index = 100;
                    break;
            }
            */
        }

        public void onProviderDisabled(String provider) {

        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    };


    public void naverMapBasicSettings() {
        mapView.getMapAsync(this);

        //????????? ??????
        locationButtonView = findViewById(R.id.locationbuttonview);
        // ????????? ?????? ?????? source
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onInit(int status) {//TTS ????????? ?????? ??????
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.KOREA);
            if (result == TextToSpeech.LANG_MISSING_DATA) {
                Log.d("hyori", "no tts data");
            } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.d("hyori", "language wrong");
            } else {
                //mRecognizer.stopListening();
                voiceActivity.speekTTS(voiceActivity.text, tts);
            }
        } else {
            Log.d("hyori", "failed");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);

    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }


}