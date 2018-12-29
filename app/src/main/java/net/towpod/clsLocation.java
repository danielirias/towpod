package net.towpod;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import static com.google.android.gms.internal.zzahn.runOnUiThread;


/**
 * Created by WebMaster on 3/2/17.
 */

public class clsLocation
{
    private Context MyContext;
    LocationManager locationManager;

    private boolean NetworkOn;
    String Latitud;
    String Longitud;

    double longitudeBest, latitudeBest;
    double longitudeGPS, latitudeGPS;
    double longitudeNetwork, latitudeNetwork;

    public String strLatGPS;
    public String strLonGPS;

    public String strLatNET;
    public String strLonNET;

    public String strLatBest;
    public String strLonBest;

    public clsLocation(Context MyContext) {
        this.MyContext = MyContext;
    }

    public void getLocation()
    {
        GetNetworkUpdates();
    }

    private boolean checkLocation()
    {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert()
    {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MyContext);
        dialog.setTitle("Servicios de ubicación")
                .setMessage("Su ubicación está desactivada.\nPor favor active su ubicación.")
                .setPositiveButton("Configurar de ubicación", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        MyContext.startActivity(myIntent);
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
        locationManager = (LocationManager) MyContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void GetNetworkUpdates()
    {
        if (!checkLocation())
            return;
        locationManager.removeUpdates(locationListenerNetwork);
        if (ActivityCompat.checkSelfPermission(MyContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MyContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
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

            Latitud = String.valueOf(location.getLatitude());
            Longitud = String.valueOf(location.getLongitude());

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    strLatNET = String.valueOf(latitudeNetwork);
                    strLonNET = String.valueOf(longitudeNetwork);

                    Latitud = strLatNET;
                    Longitud = strLonNET;

                    Toast toast = Toast.makeText(MyContext, Latitud+"\n"+Longitud, Toast.LENGTH_SHORT);
                    toast.show();

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

}
