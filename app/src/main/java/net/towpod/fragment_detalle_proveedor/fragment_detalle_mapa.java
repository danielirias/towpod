package net.towpod.fragment_detalle_proveedor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import net.towpod.R;

public class fragment_detalle_mapa extends Fragment implements OnMapReadyCallback
{
    private GoogleMap mMap;
    public double LatProveedor;
    public double LonProveedor;
    public String strNombreProveedor;

    public fragment_detalle_mapa() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View MyView = inflater.inflate(R.layout.fragment_detalle_mapa, container, false);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.mapaProveedor);
        mapFragment.getMapAsync(this);
        return MyView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatProveedor = getArguments().getDouble("prmLatProveedor");
        LonProveedor = getArguments().getDouble("prmLonProveedor");

        // Add a marker and move the camera
        LatLng Proveedor = new LatLng(LatProveedor, LonProveedor);
        mMap.addMarker(
                new MarkerOptions()
                        .position(Proveedor)
                        .title(getArguments().getString("prmNombreProveedor"))
                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.message24))
                        //.snippet("Segunda línea")
        );

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(clsProveedor));
        //Move the camera to the user's location and zoom in!
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Proveedor, 15.0f));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

    }
}