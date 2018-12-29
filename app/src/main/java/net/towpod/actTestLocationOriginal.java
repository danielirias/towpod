package net.towpod;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class actTestLocationOriginal extends AppCompatActivity {

    LocationManager locationManager;
    double longitudeBest, latitudeBest;
    double longitudeGPS, latitudeGPS;
    double longitudeNetwork, latitudeNetwork;
    TextView longitudeValueBest, latitudeValueBest;
    TextView longitudeValueGPS, latitudeValueGPS;
    TextView longitudeValueNetwork, latitudeValueNetwork;

    public String Proveedor;
    public String strLatGPS, strLonGPS;
    public String strLatNET, strLonNET;
    public String strLatBest, strLonBest;
    String Latitud, Longitud;
    String Direccion, Ciudad, CodigoPostal, ZonaDestacada, SubArea, Estado, CodigoEstado, Pais,  CodigoPais;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_test_location);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        longitudeValueBest = (TextView) findViewById(R.id.longitudeValueBest);
        latitudeValueBest = (TextView) findViewById(R.id.latitudeValueBest);
        longitudeValueGPS = (TextView) findViewById(R.id.longitudeValueGPS);
        latitudeValueGPS = (TextView) findViewById(R.id.latitudeValueGPS);
        longitudeValueNetwork = (TextView) findViewById(R.id.longitudeValueNetwork);
        latitudeValueNetwork = (TextView) findViewById(R.id.latitudeValueNetwork);

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        GetGPSUpdates();
        GetNetworkUpdates();
        GetBestUpdates();

    }

    //==============================================================================================
    //INICIO DE LOCALIZACION========================================================================
    //==============================================================================================
    private boolean checkLocation()
    {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

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

    private boolean isLocationEnabled()
    {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void GetGPSUpdates()
    {
        if (!checkLocation())
            return;
            locationManager.removeUpdates(locationListenerGPS);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {

            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 1, locationListenerGPS);
    }

    public void GetNetworkUpdates()
    {
        if (!checkLocation())
            return;
            locationManager.removeUpdates(locationListenerNetwork);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {

            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 1, locationListenerNetwork);
    }

    public void GetBestUpdates()
    {
        if (!checkLocation())
            return;

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
            }
            locationManager.removeUpdates(locationListenerBest);

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            Proveedor = locationManager.getBestProvider(criteria, true);
            if (Proveedor != null)
            {
                locationManager.requestLocationUpdates(Proveedor, 3000, 1, locationListenerBest);
                //Toast.makeText(this, "USANDO: " + Proveedor, Toast.LENGTH_LONG).show();
            }

    }

    private final LocationListener locationListenerNetwork = new LocationListener()
    {
        public void onLocationChanged(Location location)
        {
            longitudeNetwork = location.getLongitude();
            latitudeNetwork = location.getLatitude();

            Latitud = String.valueOf(location.getLatitude());
            Longitud = String.valueOf(location.getLongitude());

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    longitudeValueNetwork.setText(longitudeNetwork + "");
                    latitudeValueNetwork.setText(latitudeNetwork + "");

                    strLatNET = String.valueOf(latitudeNetwork);
                    strLonNET = String.valueOf(longitudeNetwork);

                    SetMyBestLocation();

                    //Toast.makeText(actTestLocation.this, "NETWORK updated", Toast.LENGTH_SHORT).show();
                }
            });
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

    private final LocationListener locationListenerGPS = new LocationListener()
    {
        public void onLocationChanged(Location location)
        {
            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();

            Latitud = String.valueOf(location.getLatitude());
            Longitud = String.valueOf(location.getLongitude());

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    longitudeValueGPS.setText(longitudeGPS + "");
                    latitudeValueGPS.setText(latitudeGPS + "");

                    strLatGPS = String.valueOf(latitudeBest);
                    strLonGPS = String.valueOf(longitudeBest);

                    SetMyBestLocation();

                    //Toast.makeText(actTestLocation.this, "GPS updated", Toast.LENGTH_SHORT).show();
                }
            });
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

    private final LocationListener locationListenerBest = new LocationListener()
    {
        public void onLocationChanged(Location location)
        {
            latitudeBest = location.getLatitude();
            longitudeBest = location.getLongitude();

            Latitud = String.valueOf(location.getLatitude());
            Longitud = String.valueOf(location.getLongitude());

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    longitudeValueBest.setText(longitudeBest + "");
                    latitudeValueBest.setText(latitudeBest + "");

                    strLatBest = String.valueOf(latitudeBest);
                    strLonBest = String.valueOf(longitudeBest);

                    SetMyBestLocation();

                    //Toast.makeText(actTestLocation.this, Proveedor +" actualizado", Toast.LENGTH_SHORT).show();
                }
            });
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

    public void SetMyBestLocation()
    {
        if (Latitud.toString().substring(0,1).equals("0"))
        {
            Latitud = strLatNET;
            Longitud = strLonNET;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try
        {
            addresses = geocoder.getFromLocation(Double.parseDouble(Latitud), Double.parseDouble(Longitud), 1);
        }
        catch (IOException e1)
        {
            Log.e("LocationSampleActivity", "IO Exception in getFromLocation()");
            e1.printStackTrace();
        }
        // If the reverse geocode returned an address
        if (addresses != null && addresses.size() > 0)
        {
            // Get the first address
            Address address = addresses.get(0);
            Direccion = address.getAddressLine(0);
            Ciudad = address.getLocality();
            Estado = address.getAdminArea();
            CodigoPais = address.getCountryCode();
            Pais = address.getCountryName();
        }

        TextView addressText = (TextView) findViewById(R.id.addressText);
        addressText.setText(Latitud + "\n" + Longitud + "\n" + Ciudad + "\n" + Pais);
        //Toast.makeText(this, Latitud + "\n" + Longitud + "\n" + Pais, Toast.LENGTH_SHORT).show();
    }

    //==============================================================================================
    //FIN DE LOCALIZACION===========================================================================
    //==============================================================================================



}
