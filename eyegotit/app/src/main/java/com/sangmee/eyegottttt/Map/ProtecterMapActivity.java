package com.sangmee.eyegottttt.Map;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Align;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.LocationButtonView;
import com.sangmee.eyegottttt.Login.ListViewAdapterSwipt;
import com.sangmee.eyegottttt.Login.LoginActivity;
import com.sangmee.eyegottttt.R;
import com.sangmee.eyegottttt.SplashActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProtecterMapActivity extends AppCompatActivity
        implements OnMapReadyCallback ,NavigationView.OnNavigationItemSelectedListener {


    //?????? ??????
    // NaverMap API 3.0
    private MapView mapView;
    private LocationButtonView locationButtonView;

    double longitude;
    double latitude;

    NaverMap naverMap;
    ArrayList<Marker> arrayList1=new ArrayList<>();
    Marker marker=new Marker();

    // FusedLocationSource (Google)
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;

    private DatabaseReference databaseReference;
    Intent intent;
    String user_id;
    public static final String CHANNEL_ID="notificationChannel";

    //mqtt ??????

    String topic_value;
    MqttAndroidClient client;

    //notification bar
    ListViewAdapterSwipt adapter;
    ListView listView;
    ImageButton menu_btn;
    DrawerLayout drawer;

    String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(R.style.noactionbar);
        setContentView(R.layout.activity_protecter_map);
        setTitle("");

        intent=getIntent();
        user_id=intent.getStringExtra("id");
        menu_btn=findViewById(R.id.menuButton);
        menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(GravityCompat.END);
            }
        });
        //??? ??????
        mapView = findViewById(R.id.main_map_view);
        mapView.onCreate(savedInstanceState);
        naverMapBasicSettings();
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(ProtecterMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ProtecterMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

        //mqtt ??????!!!!!!!!!!!!!!!!!!!!!!

        databaseReference = FirebaseDatabase.getInstance().getReference();
        Query recentPostsQuery = databaseReference.child(user_id).child("topic");
        recentPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                topic_value=dataSnapshot.getValue().toString();
                Log.d("sangminTopic", topic_value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(ProtecterMapActivity.this, "tcp://broker.hivemq.com:1883", clientId);

        //connect?????? ??????
        try {
            IMqttToken token = client.connect(getMqttConnectionOption());
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) { //????????? ????????? ??????
                    Log.v("SEYUN_TAG", "connection1");
                    try {
                        client.subscribe(topic_value, 0);
                    } catch(MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) { //????????? ????????? ??????
                    Toast.makeText(ProtecterMapActivity.this, "????????? ?????????????????????...(3)", Toast.LENGTH_SHORT).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        client.setCallback(new MqttCallback() { //?????????????????? ??????
            @Override
            public void connectionLost(Throwable throwable) {
                Toast.makeText(ProtecterMapActivity.this, "????????? ???????????????...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                if (topic.equals(topic_value)) { //topic?????? ????????????
                    String msg = new String(message.getPayload());
                    String word1 = msg.split("####")[0];
                    String lati = msg.split("####")[1];
                    String longi = msg.split("####")[2];
                    String user= msg.split("####")[3];
                    Log.d("sangminLocation", longi+"  "+lati);
                    Toast.makeText(ProtecterMapActivity.this, word1, Toast.LENGTH_SHORT).show();

                    double d_longitude = Double.parseDouble(longi);
                    double d_latitude = Double.parseDouble(lati);
                    marker.setPosition((new LatLng(d_latitude, d_longitude)));
                    marker.setCaptionText(user+" ??????");
                    marker.setCaptionTextSize(16);
                    marker.setCaptionColor(Color.BLACK);
                    marker.setCaptionAlign(Align.Top);
                    marker.setIconTintColor(Color.GRAY);
                    marker.setMap(naverMap);
                    CameraPosition cameraPosition = new CameraPosition(new LatLng(d_latitude, d_longitude), 17);
                    naverMap.setCameraPosition(cameraPosition);
                    //????????????


                    //createNotification();

                    address=getAddress(ProtecterMapActivity.this,d_latitude,d_longitude);


                    if(word1.equals("???????????? ?????????????????????.")){
                        startService();
                    }
                    else if(word1.equals("?????? ????????????!!!")){
                        startService2();
                    }


                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });

        listView=findViewById(R.id.list_view_inside_nav);
        adapter= new ListViewAdapterSwipt();
        listView.setAdapter(adapter);
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.recruitment), "??? ??????");
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.exit), "????????????");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                switch (position){
                    case 0 :
                        break;
                    case 1 :
                        logout_dialog();
                        break;
                }
            }
        }) ;


        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        ////////////

    }

    public String getAddress(Context mContext, double lat, double lng) {
        String nowAddress ="?????? ????????? ?????? ??? ??? ????????????.";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> address;
        try {
            if (geocoder != null) {
                //????????? ??????????????? ????????? ?????? ????????? ?????? ?????? ?????????
                //???????????? ?????? ??????????????? ????????? ????????????????????? ??????????????? ???????????? ?????? ???????????? ??????
                address = geocoder.getFromLocation(lat, lng, 1);

                if (address != null && address.size() > 0) {
                    // ?????? ????????????
                    String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                    nowAddress  = currentLocationAddress;
                }
            }
        } catch (IOException e) {
            nowAddress = "????????? ????????? ??? ????????????.";
            System.out.println("????????? ????????? ??? ????????????.");
            e.printStackTrace();
        }
        return nowAddress;
    }

    ///mqtt!!!!!!!!!!!
    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setWill("aaa", "I am going offline".getBytes(), 1, true);
        return mqttConnectOptions;
    }

    public boolean isApplicationSentToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                //createNotification();
                return true;
            }
        }

        return false;
    }
    public void startService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
        serviceIntent.putExtra("address",address);

        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void startService2() {
        Intent serviceIntent = new Intent(this, ForegroundService2.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");

        ContextCompat.startForegroundService(this, serviceIntent);
    }

    //?????????
    private void createNotification() {
        Log.v("SEYUN_TAG", "??????");

        //????????????
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        Bitmap LargeIconNoti = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        Intent intent = new Intent(this, SplashActivity.class); //????????? ????????? ??????????????? ????????????.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ProtecterMapActivity.this, CHANNEL_ID)
                .setSmallIcon(R.drawable.background)
                .setLargeIcon(LargeIconNoti) //?????????????????? ??? ??? ???????????????.
                .setContentTitle("??????")
                .setContentText("???????????? ?????? ???????????????!!!")
                .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE) //????????? ????????? ?????????.
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);


        notificationManager.notify(0, builder.build());
    }

    //??? ??????
    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //????????? ???????????? ???????????? ???????????? ????????????.
            //?????? Location ????????? ???????????? ?????? ?????? ????????? ????????? ??????.
            longitude = location.getLongitude(); //??????
            latitude = location.getLatitude();   //??????

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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.END);
        return true;
    }

    void logout_dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.MyAlertDialogStyle);
        //tilte ?????? xml

        /*TextView title = new TextView(this);
        title.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        title.setPadding(40,30,0,30);
        title.setLayoutParams(lp);
        title.setText("??????");
        title.setGravity(Gravity.LEFT);
        title.setBackgroundColor(Color.rgb(139,195,74));
        builder.setCustomTitle(title);*/
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.normal_dialog, null);
        final TextView location_edit=view.findViewById(R.id.delete_text);
        location_edit.setTextColor(Color.GRAY);
        location_edit.setText("???????????? ???????????????????");
        builder.setView(view);

        //????????????
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sharedPreferences = getSharedPreferences("sFile", MODE_PRIVATE);

                //????????? ???????????? editor??? ???????????? ?????? ??????????????????.
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("us_id", ""); // key, value??? ???????????? ???????????? ??????
                editor.putString("us_pw", "");
                //?????? ??????
                editor.commit();
                Intent intent=new Intent(ProtecterMapActivity.this, LoginActivity.class);
                ActivityCompat.finishAffinity(ProtecterMapActivity.this);
                startActivity(intent);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }
}
