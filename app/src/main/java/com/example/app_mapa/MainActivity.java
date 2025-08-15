package com.example.app_mapa;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.app_mapa.WebService.Asynchtask;
import com.example.app_mapa.WebService.WebService;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.slider.Slider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
implements OnMapReadyCallback , Asynchtask
{
    GoogleMap mapa;
    Double lat, lng;
    float radio;
    Circle circulo = null;
    Slider sliderRadio;
    EditText txtLatitud, txtLongitud;
    ArrayList<Marker> markers = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lat = -1.02313;  lng = -79.459561;       radio = 1;
        txtLatitud = findViewById(R.id.txt_latitud);
        txtLongitud = findViewById(R.id.txt_longitud);
        sliderRadio = findViewById(R.id.sliderRadio);
        sliderRadio.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                radio = slider.getValue();
                updateInterfaz();
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {

            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
    mapa = googleMap;

    //Configurar mapa
        mapa.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mapa.getUiSettings().setZoomControlsEnabled(true);
        CameraUpdate camUpd1 =
        CameraUpdateFactory
        .newLatLngZoom(new LatLng(-1.02299, -79.45979), 15);
        mapa.moveCamera(camUpd1);

        mapa.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                LatLng center = mapa.getCameraPosition().target;
                lat = center.latitude;
                lng =center.longitude;
                updateInterfaz();

            }
        });

    }
    private void updateInterfaz(){
        txtLatitud.setText(String.format("%.4f", lat));
        txtLongitud.setText(String.format("%.4f", lng));

        if(circulo!=null){  circulo.remove();  circulo = null; }

        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(lat,lng))
                .radius(radio * 100)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(50, 150, 50, 50));

        circulo = mapa.addCircle(circleOptions);

        Map<String, String> datos = new HashMap<String, String>();
        WebService ws= new WebService(
                "https://turismo.quevedoenlinea.gob.ec/lugar_turistico/json_getlistadoMapa?lat=-1.0229&lng=-79.4597&radio=1"
                        + lat +   "&lng=" + lng +"&radio=" + (radio/10.0)  ,datos,

                MainActivity.this, MainActivity.this);
        ws.execute("GET");


    }

    @Override
    public void processFinish(String result) throws JSONException {
        for (Marker marker : markers) marker.remove();
        markers.clear();

        JSONObject JSONobj= new JSONObject(result);
        JSONArray jsonLista = JSONobj.getJSONArray("data");
        for(int i=0; i< jsonLista.length(); i++){
            JSONObject lugar= jsonLista.getJSONObject(i);
            markers.add(mapa.addMarker(
                    new MarkerOptions().position(
                            new LatLng(lugar.getDouble("lat"), lugar.getDouble("lng"))
                    ).title(lugar.get("nombre").toString())));

        }

    }

}