package net.towpod;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;

public class actMyAccountHistoryDetailDriver extends AppCompatActivity implements OnMapReadyCallback {

    public String prmIDRow;
    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;
    private GoogleMap mMap;
    public String strProviderLat;
    public String strProviderLon;

    public String strUserLat;
    public String strUserLon;

    public String strDestinyLat;
    public String strDestinyLon;

    public String strCarID;
    public String strDate;
    public Integer intIdProveedor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Detalle de la soilictud");

        setContentView(R.layout.act_my_account_history_detail_driver);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        prmIDRow = intent.getStringExtra(actMyAccountHistory.EXTRA_ID_HISTORY);

        //Realizo el proceo en segundo plano y muestro el cuadro de espera
        ProgressDialog VentanaEspera = new ProgressDialog(this);
        VentanaEspera.setTitle("Obteniendo información");
        VentanaEspera.setMessage("Espere un momento, por favor...");
        new actMyAccountHistoryDetailDriver.CargaDeDatosEnSegundoPlano(VentanaEspera, this).execute();


    }

    protected void onStart()
    {
        super.onStart();


    }

    private class CargaDeDatosEnSegundoPlano extends AsyncTask<Void, Void, Void>
    {

        ProgressDialog progress;
        actMyAccountHistoryDetailDriver act;

        public CargaDeDatosEnSegundoPlano(ProgressDialog progress, actMyAccountHistoryDetailDriver act) {
            this.progress = progress;
            this.act = act;
        }

        public void onPreExecute() {
            progress.show();
            //aquí se puede colocar código a ejecutarse previo
            //a la operación
        }

        public void onPostExecute(Void unused) {
            //progress.dismiss();
            //aquí se puede colocar código que
            //se ejecutará tras finalizar el proceso y la espera adicional de 3 segundos
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // acciones que se ejecutan tras los milisegundos
                    progress.dismiss();
                }
            }, 1500);
        }

        protected Void doInBackground(Void... params) {
            //realizar la operación aquí
            CargarDatos();
            return null;
        }

    }

    public void CargarDatos()
    {

        //==================================================================================================
        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this);

        //URL del detalle del proveedor
        String url = "http://app.towpod.net/ws_get_history_detail.php?idr=" + prmIDRow;
        url = url.replaceAll(" ", "%20");

        JsonArrayRequest request = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try
                        {

                            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                            mapFragment.getMapAsync(actMyAccountHistoryDetailDriver.this);

                            TextView lblFecha = (TextView) findViewById(R.id.lblFecha);
                            lblFecha.setText(response.getJSONObject(0).getString("request_date")+" - "+response.getJSONObject(0).getString("request_time"));

                            TextView lblDescripcion = (TextView) findViewById(R.id.lblDescripcion);
                            lblDescripcion.setText(response.getJSONObject(0).getString("customer_comment"));

                            TextView lblProviderName = (TextView) findViewById(R.id.lblProviderName);
                            lblProviderName.setText(response.getJSONObject(0).getString("provider_name"));

                            RatingBar Estrellas = (RatingBar) findViewById(R.id.ratingBar);
                            Estrellas.setRating(response.getJSONObject(0).getInt("provider_rate"));

                            TextView lblTotalDistance = (TextView) findViewById(R.id.lblTotalDistance);
                            lblTotalDistance.setText(response.getJSONObject(0).getString("total_distance")+" km");

                            DecimalFormat Formato = new DecimalFormat("#,###.00");
                            String precioFormateado = Formato.format(Double.parseDouble(response.getJSONObject(0).getString("service_value")));

                            TextView lblPrecio = (TextView) findViewById(R.id.lblPrecio);
                            lblPrecio.setText(response.getJSONObject(0).getString("country_currency") +" "+ String.valueOf(precioFormateado));


                            strProviderLat = response.getJSONObject(0).getString("provider_lat");
                            strProviderLon = response.getJSONObject(0).getString("provider_lon");

                            strUserLat = response.getJSONObject(0).getString("customer_lat");
                            strUserLon = response.getJSONObject(0).getString("customer_lon");

                            strDestinyLat = response.getJSONObject(0).getString("destination_lat");
                            strDestinyLon = response.getJSONObject(0).getString("destination_lon");

                            strDate = response.getJSONObject(0).getString("request_date");

                            TextView lblDriverName = (TextView) findViewById(R.id.lblDriverName);
                            lblDriverName.setText(response.getJSONObject(0).getString("provider_firstname")+" "+response.getJSONObject(0).getString("provider_lastname"));

                            TextView lblNIF = (TextView) findViewById(R.id.lblNIF);
                            lblNIF.setText(response.getJSONObject(0).getString("provider_nif"));

                            TextView lblFullAddress = (TextView) findViewById(R.id.lblFullAddress);
                            lblFullAddress.setText(response.getJSONObject(0).getString("provider_city")+" "+response.getJSONObject(0).getString("provider_state")+" "+response.getJSONObject(0).getString("provider_country"));

                            TextView lblMarca = (TextView) findViewById(R.id.lblMarca);
                            lblMarca.setText(response.getJSONObject(0).getString("provider_car_brand"));
                            TextView lblModelo = (TextView) findViewById(R.id.lblModelo);
                            lblModelo.setText(response.getJSONObject(0).getString("provider_car_model"));
                            TextView lblYear = (TextView) findViewById(R.id.lblYear);
                            lblYear.setText(response.getJSONObject(0).getString("provider_car_year"));
                            TextView lblColor = (TextView) findViewById(R.id.lblColor);
                            lblColor.setText(response.getJSONObject(0).getString("provider_car_color"));
                            TextView lblPlaca = (TextView) findViewById(R.id.lblPlaca);
                            lblPlaca.setText(response.getJSONObject(0).getString("provider_car_id"));

                            strCarID = response.getJSONObject(0).getString("provider_car_id");





                            //Para cargar el código QR desde el servidor:
                            ImageView imgQRCode = (ImageView) findViewById(R.id.imgQRCode);
                            new actMyAccountHistoryDetailDriver.AsyncTaskLoadImage(imgQRCode).execute("http://app.towpod.net/qr-code-assistance/QR_"+response.getJSONObject(0).getString("confirmation_code")+"_"+response.getJSONObject(0).getString("customer_id")+".jpg");

                            TextView lblStatus = (TextView) findViewById(R.id.lblStatus);
                            if(response.getJSONObject(0).getInt("verification_status")==0)
                            {
                                lblStatus.setText("Este código QR debe ser escaneado por el proveedor de asistencia vial.");
                            }
                            else
                            {
                                lblStatus.setText("El código QR ya fue verificado.");
                                intIdProveedor = response.getJSONObject(0).getInt("provider_id");
                            }


                            //Para cargar  la foto del conductor
                            ImageView imgDriver = (ImageView) findViewById(R.id.imgDriver);
                            new actMyAccountHistoryDetailDriver.AsyncTaskLoadDriverPicture(imgDriver).execute("http://app.towpod.net/driver/"+response.getJSONObject(0).getString("provider_no_id")+".jpg");





                        } catch (JSONException e) {
                            Toast popUpError = Toast.makeText(actMyAccountHistoryDetailDriver.this, e.getMessage(), Toast.LENGTH_LONG);
                            popUpError.show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Si recibimos un error, mostraremos la causa
                        Toast popUpError = Toast.makeText(actMyAccountHistoryDetailDriver.this, error.getMessage(), Toast.LENGTH_LONG);
                        popUpError.show();
                    }
                }
        );

        //Le ponemos un tag que servirá para identificarla si la queremos cancelar
        request.setTag(JSON_REQUEST);
        //Al ponerle la política de reintento, le hacemos casting, ya que el metodo recibe un objeto de la clase padre
        request = (JsonArrayRequest) setRetryPolicy(request);
        //Iniciamos la petición añadiéndola a la cola
        requestQueue.add(request);

        //==================================================================================================
    }

    public void onMapReady(GoogleMap googleMap)
    {

        mMap = googleMap;

        //=============== MARCADOR USUARIO ====================
        // Add a marker and move the camera
        LatLng pointUser = new LatLng(Double.parseDouble(strUserLat), Double.parseDouble(strUserLon));

        MarkerOptions markerOptionsUser = new MarkerOptions();
        markerOptionsUser.position(pointUser);
        markerOptionsUser.title("Tu ubicación");
        //markerOptions.snippet(strDate);
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(R.drawable.taxilocationmarker));
        BitmapDrawable iconMarker = (BitmapDrawable) getResources().getDrawable(R.drawable.locationmarkeruser);
        Bitmap bitmapMarker = iconMarker.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(bitmapMarker, 130, 130, false);
        markerOptionsUser.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.taxilocationmarker));
        Marker locationMarkerUser = mMap.addMarker(markerOptionsUser);
        locationMarkerUser.showInfoWindow();
        locationMarkerUser.setDraggable(false);
        //===================================================


        //=============== MARCADOR DESTINO ====================
        // Add a marker and move the camera
        LatLng pointDestiny = new LatLng(Double.parseDouble(strDestinyLat), Double.parseDouble(strDestinyLon));

        MarkerOptions markerOptionsDestiny = new MarkerOptions();
        markerOptionsDestiny.position(pointDestiny);
        markerOptionsDestiny.title("Destino");
        //markerOptions.snippet(strDate);
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(R.drawable.taxilocationmarker));
        BitmapDrawable iconMarkerDestiny = (BitmapDrawable) getResources().getDrawable(R.drawable.locationmarkerhome);
        Bitmap bitmapMarkerDestiny = iconMarkerDestiny.getBitmap();
        Bitmap smallMarkerDestiny = Bitmap.createScaledBitmap(bitmapMarkerDestiny, 130, 130, false);
        markerOptionsDestiny.icon(BitmapDescriptorFactory.fromBitmap(smallMarkerDestiny));
        //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.taxilocationmarker));
        Marker locationMarkerDestiny = mMap.addMarker(markerOptionsDestiny);
        locationMarkerDestiny.showInfoWindow();
        locationMarkerDestiny.setDraggable(false);
        //===================================================


        //=============== MARCADOR TAXI ====================
        // Add a marker and move the camera
        LatLng Proveedor = new LatLng(Double.parseDouble(strProviderLat), Double.parseDouble(strProviderLon));

        MarkerOptions markerOptionsTaxi = new MarkerOptions();
        markerOptionsTaxi.position(Proveedor);
        markerOptionsTaxi.title("ID: "+strCarID);
        //markerOptions.snippet(strDate);
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(R.drawable.taxilocationmarker));
        BitmapDrawable iconMarkerTaxi = (BitmapDrawable) getResources().getDrawable(R.drawable.locationmarkertaxi);
        Bitmap bitmapMarkerTaxi = iconMarkerTaxi.getBitmap();
        Bitmap smallMarkerTaxi = Bitmap.createScaledBitmap(bitmapMarkerTaxi, 130, 130, false);
        markerOptionsTaxi.icon(BitmapDescriptorFactory.fromBitmap(smallMarkerTaxi));
        //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.taxilocationmarker));
        Marker locationMarkerTaxi = mMap.addMarker(markerOptionsTaxi);
        locationMarkerTaxi.showInfoWindow();
        locationMarkerTaxi.setDraggable(false);
        //===================================================

        mMap.moveCamera(CameraUpdateFactory.newLatLng(Proveedor));
        //Move the camera to the user's location and zoom in!
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Proveedor, 15.0f));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }

    public class AsyncTaskLoadImage  extends AsyncTask<String, String, Bitmap> {
        private final static String TAG = "AsyncTaskLoadImage";
        private ImageView imageView;
        public AsyncTaskLoadImage(ImageView imageView) {
            this.imageView = imageView;
        }
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            try {
                URL url = new URL(params[0]);
                bitmap = BitmapFactory.decodeStream((InputStream) url.getContent());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            return bitmap;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }

    public class AsyncTaskLoadDriverPicture  extends AsyncTask<String, String, Bitmap> {
        private final static String TAG = "AsyncTaskLoadImage";
        private ImageView imageView;
        public AsyncTaskLoadDriverPicture(ImageView imageView) {
            this.imageView = imageView;
        }
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            try {
                URL url = new URL(params[0]);
                bitmap = BitmapFactory.decodeStream((InputStream) url.getContent());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            return bitmap;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }

    public Request setRetryPolicy(Request request)
    {
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }
}
