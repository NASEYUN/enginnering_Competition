package com.sangmee.eyegottttt.Map;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.LocationButtonView;
import com.sangmee.eyegottttt.DatabaseActivity;
import com.sangmee.eyegottttt.FirstviewActivity;
import com.sangmee.eyegottttt.Login.User;
import com.sangmee.eyegottttt.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private Button start_btn;
    private Button point_btn;
    private Button end_btn;

    Intent intent, intentId;
    String user_id;

    private DatabaseReference databaseReference;
    private Spinner spinner;

    String location;
    String spot;
    String message;
    String topic;
    int num;
    boolean start_bool=true;
    boolean point_bool=true;
    boolean end_bool=true;
    int index;

    ArrayList<String> child_name;

    ArrayAdapter<String> adapter;
    ArrayList<String> potList= new ArrayList<>();

    //?????? ??????
    // NaverMap API 3.0
    private MapView mapView;
    private LocationButtonView locationButtonView;

    double longitude;
    double latitude;

    NaverMap naverMap;
    ArrayList<Marker> arrayList1=new ArrayList<>();

    // FusedLocationSource (Google)
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;

    //????????? ????????? ?????? ?????? ??????
    double startLongitude;
    double startLatitude;
    String sLongitude;
    String sLatitude;
    Marker[] save_maker=new Marker[50];

    ImageButton listButton;

    int checking=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.noactionbar);
        setContentView(R.layout.activity_main);

        //??? ??????
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        naverMapBasicSettings();
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

        intentId=getIntent();
        user_id=intentId.getStringExtra("id");

        child_name=new ArrayList<>();

        start_btn = (Button) findViewById(R.id.start_btn);
        start_btn.setBackgroundColor(Color.rgb(255, 214, 63));
        start_btn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                start_dialog();

            }
        });

        point_btn = (Button) findViewById(R.id.point_btn);
        point_btn.setBackgroundColor(Color.LTGRAY);
        point_btn.setEnabled(false);
        point_bool=false;

        point_btn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                point_dialog();
            }
        });

        end_btn=(Button)findViewById(R.id.end_btn);
        end_btn.setEnabled(false);
        end_btn.setBackgroundColor(Color.LTGRAY);
        end_bool=false;
        end_btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                /*point_btn.setBackgroundColor(Color.LTGRAY);
                view.setBackgroundColor(Color.rgb(255, 214, 63));*/
                end_dialog();
            }
        });

        if (savedInstanceState != null) {
            start_bool = savedInstanceState.getBoolean("start_bool");
            point_bool = savedInstanceState.getBoolean("point_bool");
            end_bool = savedInstanceState.getBoolean("end_bool");
            start_btn.setEnabled(start_bool);
            point_btn.setEnabled(point_bool);
            end_btn.setEnabled(end_bool);

            num = savedInstanceState.getInt("num");
            location=savedInstanceState.getString("location");
            potList=savedInstanceState.getStringArrayList("potList");
            child_name=savedInstanceState.getStringArrayList("child_name");
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();
        Query recentPostsQuery = databaseReference.child(user_id).child("location");
        recentPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                child_name.clear();

                for (DataSnapshot messageData : dataSnapshot.getChildren()) {
                    String name=messageData.getKey();
                    Log.d("sangminKey", name);
                    child_name.add(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    //???????????? ?????? ?????????
    @Override
    public void onBackPressed() {
        if(start_bool==false){
            confirm_dialog();

        }
        else {
            super.onBackPressed();
        }
    }

    //?????? ?????? ????????? ????????? ???????????????
    void start_dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.MyAlertDialogStyle);

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.start_dialog, null);
        builder.setView(view);
        final EditText location_edit=view.findViewById(R.id.location_text);

        num=0;



        //focus ????????? (??? ??????)
        location_edit.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b)
                    view.setBackgroundResource(R.drawable.primary_border);
                else
                    view.setBackgroundResource(R.drawable.gray_border);
            }
        });


        //????????????
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //?????? ????????? ??????????????? ??????

                checking=100;

                location=location_edit.getText().toString();
                databaseReference = FirebaseDatabase.getInstance().getReference();

                //????????? ???????????? ?????? ????????? ????????? ??????
                boolean bl=false;
                for(int i=0; i<child_name.size(); i++){
                    String save_name=child_name.get(i);
                    if(save_name.equals(location))
                        bl=true;
                }
                //????????? ?????? ??????????????? ???
                if(bl){
                    normal_dialog();
                }
                //??? ???????????????
                if(!bl&&!location.equals("")){
                    //spot message ??????

                    databaseReference.child(user_id).child(location);


                    spot= "??????"+num;
                    message="";

                    Marker marker=new Marker();
                    marker.setPosition(new LatLng(latitude,longitude));

                    //?????? ?????? ??????
                    startLongitude=marker.getPosition().longitude; //??????
                    startLatitude=marker.getPosition().latitude;  //??????
                    sLongitude = Double.toString(startLongitude);
                    sLatitude= Double.toString(startLatitude);

                    marker.setCaptionText(location+" ?????????");
                    marker.setCaptionTextSize(16);
                    marker.setCaptionColor(Color.BLUE);
                    marker.setCaptionAlign(Align.Top);
                    marker.setIconTintColor(Color.RED);
                    marker.setMap(naverMap);

                    postFirebaseDatabase(true);
                    point_btn.setEnabled(true);
                    point_btn.setBackgroundColor(Color.rgb(255, 214, 63));
                    point_bool=true;
                    end_btn.setEnabled(true);
                    end_btn.setBackgroundColor(Color.rgb(255, 214, 63));
                    end_bool=true;
                    start_btn.setEnabled(false);
                    start_btn.setBackgroundColor(Color.LTGRAY);
                    start_bool=false;
                }
                //??????????????? ?????? ????????? ???
                if(location.equals("")){
                    empty_dialog();
                }

            }
        });

        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.show();
        //AlertDialog alert=builder.create();
        //alert.show();
        //Button cancelbutton=alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        //Button okaybutton=alert.getButton(DialogInterface.BUTTON_POSITIVE);
        //cancelbutton.setBackgroundColor(Color.WHITE);

    }


    //PIN ?????? ????????? ????????? ???????????????
    void point_dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.MyAlertDialogStyle);
        //tilte ?????? xml
       /* TextView title = new TextView(this);
        title.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        title.setPadding(40,30,0,30);
        title.setLayoutParams(lp);
        title.setText(location+" ????????????");
        title.setGravity(Gravity.LEFT);
        title.setBackgroundColor(Color.rgb(139,195,74));
        builder.setCustomTitle(title);*/
        builder.setCancelable(false);

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.point_dialog, null);
        builder.setView(view);



       final EditText answer_edit=view.findViewById(R.id.message_text);
       //final Button cancelbutton=view.findViewById(R.id.cancelbutton);
       //final Button okaybutton=view.findViewById(R.id.okaybutton);

       //focus ????????? (??? ??????)
        answer_edit.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b)
                    view.setBackgroundResource(R.drawable.primary_border);
                else
                    view.setBackgroundResource(R.drawable.gray_border);
            }
        });

        //spinner ??????
        num=num+1;
        potList.add("??????"+num);
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, potList);
        spinner=(Spinner)view.findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setSelection(num-1);


        //????????????
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //firebase??? spot message ?????? ?????? ??????
                spot= spinner.getSelectedItem().toString();
                message=answer_edit.getText().toString();


                Marker marker=new Marker();
                marker.setPosition(new LatLng(latitude,longitude));
                //?????? ?????? ??????
                startLongitude=marker.getPosition().longitude; //??????
                startLatitude=marker.getPosition().latitude;  //??????

                sLongitude = Double.toString(startLongitude);
                sLatitude= Double.toString(startLatitude);

                postFirebaseDatabase(true);


                save_maker[num-1]=marker;
                arrayList1.add(save_maker[num-1]);

                if(!(spinner.getSelectedItem().toString().equals("??????"+num))){


                    String position_num=spinner.getSelectedItem().toString();
                    position_num = position_num.replaceAll("[^0-9]", "");
                    int p_num=Integer.parseInt(position_num);
                    Log.d("sangmin", ""+p_num);

                    arrayList1.get(p_num-1).setMap(null);
                    arrayList1.get(p_num-1).setPosition(new LatLng(latitude,longitude));
                    arrayList1.get(p_num-1).setCaptionText("??????"+p_num);
                    arrayList1.get(p_num-1).setMap(naverMap);
                    potList.remove(num-1);
                    num=num-1;

                }
                else{
                    marker.setCaptionText("??????"+num);
                    marker.setMap(naverMap);
                }

            }
        });

        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                potList.remove(num-1);
                num=num-1;
            }
        });

        builder.show();

    }

    //?????? ?????? ????????? ????????? ???????????????
    void end_dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.MyAlertDialogStyle);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.end_dialog, null);
        builder.setView(view);

        final EditText ans_edit=view.findViewById(R.id.msg_text);
        //focus ????????? (??? ??????)
        ans_edit.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b)
                    view.setBackgroundResource(R.drawable.primary_border);
                else
                    view.setBackgroundResource(R.drawable.gray_border);
            }
        });


        //????????????
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                spot= "??????"+(num+1);
                message=ans_edit.getText().toString();
                Marker marker=new Marker();
                marker.setPosition(new LatLng(latitude,longitude));
                //?????? ?????? ??????
                startLongitude=marker.getPosition().longitude; //??????
                startLatitude=marker.getPosition().latitude;  //??????
                sLongitude = Double.toString(startLongitude);
                sLatitude= Double.toString(startLatitude);

                marker.setCaptionText(location+" ?????????");
                marker.setCaptionTextSize(16);
                marker.setCaptionColor(Color.BLUE);
                marker.setCaptionAlign(Align.Top);
                marker.setIconTintColor(Color.BLUE);
                marker.setMap(naverMap);


                postFirebaseDatabase(true);
                point_btn.setEnabled(false);
                point_btn.setBackgroundColor(Color.LTGRAY);
                point_bool=false;
                end_btn.setEnabled(false);
                end_btn.setBackgroundColor(Color.LTGRAY);
                end_bool=false;
                start_btn.setEnabled(true);
                start_btn.setBackgroundColor(Color.rgb(255, 214, 63));
                start_bool=true;
                potList.clear();

                done_dialog();
            }
        });

        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

    //?????? ???????????????
        void normal_dialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.MyAlertDialogStyle);

            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.normal_dialog, null);
            final TextView location_edit=view.findViewById(R.id.delete_text);
            location_edit.setText("?????? ????????? ????????? ???????????????.");
            builder.setView(view);

            //????????????
            builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    start_dialog();
                }
            });

        builder.show();
    }

    //?????? ???????????????
    void empty_dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.MyAlertDialogStyle);

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.normal_dialog, null);
        final TextView location_edit=view.findViewById(R.id.delete_text);
        location_edit.setText("?????? ????????? ??????????????????.");
        builder.setView(view);



        //????????????
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                start_dialog();
            }
        });
        builder.show();
    }

    void done_dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.MyAlertDialogStyle);

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.normal_dialog, null);
        final TextView location_edit=view.findViewById(R.id.delete_text);
        location_edit.setText(location+"????????? ?????????????????????.");
        builder.setView(view);



        //????????????
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                intent = new Intent(MainActivity.this, FirstviewActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("id", user_id);
                startActivity(intent);
            }
        });
        builder.show();
    }

    //???????????? ?????? ?????????????????? ?????? ????????? ???????????????
    void confirm_dialog() {
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
        location_edit.setText("???????????? ?????? ?????? ????????? ??????????????? ???????????? ?????? ???????????????.\n\n?????????????????????????\n");
        builder.setView(view);

        //????????????
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                point_btn.setEnabled(false);
                point_btn.setBackgroundColor(Color.LTGRAY);
                point_bool=false;
                end_btn.setEnabled(false);
                end_btn.setBackgroundColor(Color.LTGRAY);
                end_bool=false;
                start_btn.setEnabled(true);
                start_btn.setBackgroundColor(Color.rgb(255, 214, 63));
                start_bool=true;

                databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.child(user_id).child("location").child(location).removeValue();

                intent = new Intent(MainActivity.this, FirstviewActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("id", user_id);
                startActivity(intent);

            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }


    //????????????????????? ???????????? ??????
    public void postFirebaseDatabase(boolean add){
        databaseReference = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> childUpdates = new HashMap<>();
        Map<String, Object> postValues = null;
        if(add){
            User post = new User(spot, message, sLongitude, sLatitude);
            postValues = post.toMap();
        }
        //database ?????? ->pint_list:child , spot: title, postValues :?????? ???

        childUpdates.put("/"+user_id+"/location/"+location+"/" + spot, postValues);
        databaseReference.updateChildren(childUpdates);
    }

    //??? ??????
    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //????????? ???????????? ???????????? ???????????? ????????????.
            //?????? Location ????????? ???????????? ?????? ?????? ????????? ????????? ??????.
            longitude = location.getLongitude(); //??????
            latitude = location.getLatitude();   //??????

            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(latitude, longitude)).animate(CameraAnimation.Linear);
            naverMap.moveCamera(cameraUpdate);

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
        this.naverMap=naverMap;
        locationButtonView.setMap(naverMap);

        // ????????? ?????? ?????? source
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        // Location Change Listener??? ???????????? ?????? FusedLocationSource ??????
        naverMap.setLocationSource(locationSource);
        CameraPosition cameraPosition = new CameraPosition(new LatLng(37.51623152475618, 127.08423459151206), 18);
        naverMap.setCameraPosition(cameraPosition);

        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        LocationOverlay locationOverlay = naverMap.getLocationOverlay();
        locationOverlay.setVisible(true);
        locationOverlay.setPosition(new LatLng(latitude,longitude));
        Log.i("hyori","afdsfsd"+latitude);
        //locationSource.activate(listener);


        //locationOverlay.setPosition(new LatLng(locationSource.getLastLocation().getLatitude(),locationSource.getLastLocation().getLongitude()));

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

        start_bool = start_btn.isEnabled();
        point_bool = point_btn.isEnabled();
        end_bool = end_btn.isEnabled();
        outState.putBoolean("start_bool", start_bool);
        outState.putBoolean("point_bool", point_bool);
        outState.putBoolean("end_bool", end_bool);

        outState.putInt("num", num);
        outState.putString("location", location);
        outState.putStringArrayList("potList", potList);
        outState.putStringArrayList("child_name", child_name);
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

    //??????
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                if(start_bool==false){
                    confirm_dialog();
                    break;
                }
                else {
                    intent = new Intent(this, DatabaseActivity.class);
                    intent.putExtra("id", user_id);
                    startActivity(intent);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }
*/
}

