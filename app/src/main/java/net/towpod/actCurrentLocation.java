package net.towpod;

import android.*;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class actCurrentLocation extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    double longitudeBest, latitudeBest;
    double longitudeGPS, latitudeGPS;
    double longitudeNetwork, latitudeNetwork;
    TextView longitudeValueBest, latitudeValueBest;
    TextView longitudeValueGPS, latitudeValueGPS;
    TextView longitudeValueNetwork, latitudeValueNetwork;

    public Marker currentPositionMarker = null;

    Double currentLat, currentLong;

    public String strLocationProvider;

    public static String extraCurrentLat = "extra.lat";
    public static String extraCurrentLong = "extra.lon";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Ubicación actual");
        setContentView(R.layout.act_current_location);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Button btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(actCurrentLocation.this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        GetGPSUpdates();
        GetNetworkUpdates();
        GetBestUpdates();

        //Realizo el proceo en segundo plano y muestro el cuadro de espera
        ProgressDialog VentanaEspera = new ProgressDialog(this);
        VentanaEspera.setTitle("Estableciendo ubicación actual");
        VentanaEspera.setMessage("Espere un momento, por favor...");
        new actCurrentLocation.ProcesoSegundoPlano(VentanaEspera, this).execute();




    }


    @Override
    public void onMapReady(GoogleMap googleMap)
    {


        mMap = googleMap;


    }

    private class ProcesoSegundoPlano extends AsyncTask<Void, Void, Void>
    {

        ProgressDialog progress;
        actCurrentLocation act;

        public ProcesoSegundoPlano(ProgressDialog progress, actCurrentLocation act) {
            this.progress = progress;
            this.act = act;
        }

        public void onPreExecute() {
            progress.show();
            //aquí se puede colocar código a ejecutarse previo
            //a la operación
        }

        public void onPostExecute(Void unused) {
            //aquí se puede colocar código que
            //se ejecutará tras finalizar el proceso y la espera adicional de 3 segundos
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // acciones que se ejecutan tras los milisegundos
                    progress.dismiss();
                }
            }, 3000);
        }

        protected Void doInBackground(Void... params) {

            return null;
        }

    }

    public void setDestination(View view)
    {
        Intent actDestination = new Intent(actCurrentLocation.this, actDestination.class);
        actDestination.putExtra(extraCurrentLat, currentLat.toString());
        actDestination.putExtra(extraCurrentLong, currentLong.toString());
        startActivity(actDestination);
    }

    //==============================================================================================
    //INICIO DE LOCALIZACION========================================================================
    //==============================================================================================

    private void showAlert()
    {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Servicios de ubicación")
                .setMessage("Su ubicación está desactivada.\nPor favor active su ubicación.")
                .setPositiveButton("Configurar de ubicación", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    public void GetGPSUpdates()
    {
        if (!checkLocation())
            return;
        locationManager.removeUpdates(locationListenerGPS);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 1, locationListenerGPS);
    }

    private final LocationListener locationListenerGPS = new LocationListener()
    {
        public void onLocationChanged(Location location)
        {
            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();

            currentLat = latitudeGPS;
            currentLong = longitudeGPS;

            setMyCurrentLocation(currentLat, currentLong);

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };

    public void GetNetworkUpdates()
    {
        if (!checkLocation())
            return;
        locationManager.removeUpdates(locationListenerNetwork);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 1, locationListenerNetwork);
    }

    private final LocationListener locationListenerNetwork = new LocationListener()
    {
        public void onLocationChanged(Location location)
        {
            longitudeNetwork = location.getLongitude();
            latitudeNetwork = location.getLatitude();

            currentLat = latitudeNetwork;
            currentLong = longitudeNetwork;

            setMyCurrentLocation(currentLat, currentLong);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {

        }
        @Override
        public void onProviderDisabled(String s) {

        }
    };

    public void GetBestUpdates()
    {
        if (!checkLocation())
            return;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
        }
        locationManager.removeUpdates(locationListenerBest);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        strLocationProvider = locationManager.getBestProvider(criteria, true);
        if (strLocationProvider != null)
        {
            locationManager.requestLocationUpdates(strLocationProvider, 3000, 1, locationListenerBest);
            //Toast.makeText(this, "USANDO: " + strLocationProvider, Toast.LENGTH_LONG).show();
        }

    }

    private final LocationListener locationListenerBest = new LocationListener()
    {
        public void onLocationChanged(Location location)
        {
            latitudeBest = location.getLatitude();
            longitudeBest = location.getLongitude();

            currentLat = latitudeBest;
            currentLong = longitudeBest;

            setMyCurrentLocation(currentLat, currentLong);

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };

    private boolean checkLocation()
    {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private boolean isLocationEnabled()
    {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||  locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void setMyCurrentLocation(Double currentLat, Double currentLong)
    {
        LatLng Cordenadas = new LatLng(currentLat, currentLong);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(Cordenadas);
        markerOptions.title("Tu ubicación");
        markerOptions.snippet("--");
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

        if(currentPositionMarker == null)
        {
            currentPositionMarker = mMap.addMarker(markerOptions);
            //currentPositionMarker.setDraggable(false);
        }
        else
        {
            currentPositionMarker.setPosition(Cordenadas);
        }

        CameraPosition cameraPosition = new CameraPosition.Builder().target(Cordenadas).zoom(16.0f).build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        //mMap.addMarker(markerOptions);

        currentPositionMarker.showInfoWindow();

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Cordenadas, 18.0f));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);

        Button btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setEnabled(true);
    }
    //==============================================================================================
    //FIN DE LOCALIZACION===========================================================================
    //==============================================================================================

}
