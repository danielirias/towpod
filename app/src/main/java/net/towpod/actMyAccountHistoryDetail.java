package net.towpod;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;

public class actMyAccountHistoryDetail extends AppCompatActivity  implements OnMapReadyCallback {

    public String prmIDRow;
    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;
    private GoogleMap mMap;
    public String strLat;
    public String strLon;
    public String strDate;
    public Integer intIdProveedor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_my_account_history_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        prmIDRow = intent.getStringExtra(actMyAccountHistory.EXTRA_ID_HISTORY);
        setTitle(intent.getStringExtra(actListaProveedores.EXTRA_NOMBRE_PROVEEDOR));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(intIdProveedor == 0)
                {
                    Snackbar.make(view, "Debes confirmar que tu asistencia ha sido recibida para poder realizar comentarios.", Snackbar.LENGTH_LONG)
                            .setAction("OK", null).show();
                }
                else
                {
                    AddComment();
                }

            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Detalle de asistencia recibida");

        //Realizo el proceo en segundo plano y muestro el cuadro de espera
        ProgressDialog VentanaEspera = new ProgressDialog(this);
        VentanaEspera.setTitle("Obteniendo información");
        VentanaEspera.setMessage("Espere un momento, por favor...");
        new actMyAccountHistoryDetail.ProcesoSegundoPlano(VentanaEspera, this).execute();

    }

    protected void onStart()
    {
        super.onStart();


        //PresentarInfoProveedor();
    }

    private class ProcesoSegundoPlano extends AsyncTask<Void, Void, Void>
    {

        ProgressDialog progress;
        actMyAccountHistoryDetail act;

        public ProcesoSegundoPlano(ProgressDialog progress, actMyAccountHistoryDetail act) {
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
                            TextView lblFecha = (TextView) findViewById(R.id.lblFecha);
                            lblFecha.setText(response.getJSONObject(0).getString("request_date")+" - "+response.getJSONObject(0).getString("request_time"));

                            TextView lblCategoria = (TextView) findViewById(R.id.lblCategoria);
                            lblCategoria.setText(response.getJSONObject(0).getString("assistance_name"));

                            TextView lblDescripcion = (TextView) findViewById(R.id.lblDescripcion);
                            lblDescripcion.setText(response.getJSONObject(0).getString("customer_comment"));

                            TextView lblMarca = (TextView) findViewById(R.id.lblMarca);
                            lblMarca.setText(response.getJSONObject(0).getString("car_brand"));
                            TextView lblModelo = (TextView) findViewById(R.id.lblModelo);
                            lblModelo.setText(response.getJSONObject(0).getString("car_model"));
                            TextView lblYear = (TextView) findViewById(R.id.lblYear);
                            lblYear.setText(response.getJSONObject(0).getString("car_year"));
                            TextView lblColor = (TextView) findViewById(R.id.lblColor);
                            lblColor.setText(response.getJSONObject(0).getString("car_color"));
                            TextView lblPlaca = (TextView) findViewById(R.id.lblPlaca);
                            lblPlaca.setText(response.getJSONObject(0).getString("car_id"));

                            TextView lblNoPoliza = (TextView) findViewById(R.id.lblNoPoliza);
                            lblNoPoliza.setText(response.getJSONObject(0).getString("policy_number"));

                            DecimalFormat Formato = new DecimalFormat("#,###.00");
                            String precioFormateado = Formato.format(Double.parseDouble(response.getJSONObject(0).getString("service_value")));

                            TextView lblPrecio = (TextView) findViewById(R.id.lblPrecio);
                            lblPrecio.setText(response.getJSONObject(0).getString("country_currency") +" "+ String.valueOf(precioFormateado));

                            strLat = response.getJSONObject(0).getString("customer_lat");
                            strLon = response.getJSONObject(0).getString("customer_lon");
                            strDate = response.getJSONObject(0).getString("request_date");

                            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                            mapFragment.getMapAsync(actMyAccountHistoryDetail.this);

                            intIdProveedor = response.getJSONObject(0).getInt("provider_id");

                            TextView lblNombreProveedor = (TextView) findViewById(R.id.lblNombreProveedor);
                            if(response.getJSONObject(0).getString("provider_name").toString().trim().equals("null"))
                            {
                                lblNombreProveedor.setText("Esperando confirmación de asistencia recibida");
                            }
                            else
                            {
                                lblNombreProveedor.setText(response.getJSONObject(0).getString("provider_name"));
                            }

                            //Para cargar el código QR desde el servidor:
                            ImageView imgQRCode = (ImageView) findViewById(R.id.imgQRCode);
                            new AsyncTaskLoadImage(imgQRCode).execute("http://app.towpod.net/qr-code-assistance/QR_"+response.getJSONObject(0).getString("confirmation_code")+"_"+response.getJSONObject(0).getString("customer_id")+".jpg");

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





                        } catch (JSONException e) {
                            Toast popUpError = Toast.makeText(actMyAccountHistoryDetail.this, e.getMessage(), Toast.LENGTH_LONG);
                            popUpError.show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Si recibimos un error, mostraremos la causa
                        Toast popUpError = Toast.makeText(actMyAccountHistoryDetail.this, error.getMessage(), Toast.LENGTH_LONG);
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

    public void onMapReady(GoogleMap googleMap)
    {

        mMap = googleMap;

        // Add a marker and move the camera
        LatLng Proveedor = new LatLng(Double.parseDouble(strLat), Double.parseDouble(strLon));


        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(Proveedor);
        markerOptions.title("Ubicación registrada");
        //markerOptions.snippet(strDate);

        Marker locationMarker = mMap.addMarker(markerOptions);

        locationMarker.showInfoWindow();

        locationMarker.setDraggable(false);

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

    public void AddComment()
    {


        AlertDialog.Builder builder = new AlertDialog.Builder(actMyAccountHistoryDetail.this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_add_comment, null));

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Nada para hacer aquí
            }
        });

        builder.setPositiveButton("Enviar comentario", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //No pasa nada... el código se trasladó a la clase CustomListener
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new actMyAccountHistoryDetail.CustomListener(alertDialog));

    }

    class CustomListener implements View.OnClickListener
    {
        private final Dialog dialog;

        public CustomListener(Dialog MyDialog) {
            this.dialog = MyDialog;
        }

        @Override
        public void onClick(View v) {

            try
            {
                //Abrimos la base de datos 'DBUsuarios' en modo escritura
                cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actMyAccountHistoryDetail.this, "USUARIOSHM", null, 2);
                SQLiteDatabase db = usdbh.getWritableDatabase();

                //Si hemos abierto correctamente la base de datos
                if(db != null)
                {
                    String[] campos = new String[] {"user_id", "user_email", "user_password"};
                    final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

                    try
                    {
                        CursorDB.moveToFirst();
                        if (CursorDB.getCount() == 0)
                        {
                            //Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
                            Intent actMainLogin = new Intent(actMyAccountHistoryDetail.this, actMainLogin.class);
                            startActivity(actMainLogin);
                            return;

                        }
                        else
                        {
                            //Si el USUARIO EXISTE LOCALMENTE voy a buscarlo al servidor web
                            //Busco la direccion de correo entre los usarios registrados
                            //Obtenemos la instancia única de la cola de peticiones
                            requestQueue = RequestQueueSingleton.getRequestQueue(actMyAccountHistoryDetail.this);

                            //Con esta URL verifico si se han creado registros con este usuario.
                            String urlUser = "http://app.towpod.net/ws_get_login_data_customer.php?usr="+CursorDB.getString(1)+"&pwd="+CursorDB.getString(2);;
                            urlUser = urlUser.replaceAll(" ", "%20");

                            //Creamos la petición
                            JsonArrayRequest requestUser = new JsonArrayRequest(urlUser,
                                    new Response.Listener<JSONArray>() {
                                        @Override
                                        public void onResponse(JSONArray response) {

                                            if (response.length() > 0)
                                            {
                                                try
                                                {
                                                    response.getJSONObject(0).getInt("customer_id");

                                                    final EditText txtMyComment = (EditText) dialog.findViewById(R.id.txtMyComment);
                                                    if (CampoVacio(txtMyComment) == true) {
                                                        txtMyComment.requestFocus();
                                                        InputMethodManager IMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                        IMM.showSoftInput(txtMyComment, InputMethodManager.SHOW_IMPLICIT);

                                                        Toast.makeText(
                                                                actMyAccountHistoryDetail.this,
                                                                "Debes escribir un comentario.",
                                                                Toast.LENGTH_LONG)
                                                                .show();
                                                        return;

                                                    }


                                                    RatingBar rateProveedor = (RatingBar) dialog.findViewById(R.id.rateBar);
                                                    if (rateProveedor.getRating() == 0) {
                                                        Toast.makeText(
                                                                actMyAccountHistoryDetail.this,
                                                                "Danos tu calificación.",
                                                                Toast.LENGTH_LONG)
                                                                .show();
                                                        return;
                                                    }


                                                    //Obtenemos la instancia única de la cola de peticiones
                                                    requestQueue = RequestQueueSingleton.getRequestQueue(actMyAccountHistoryDetail.this);

                                                    //URL para guardar datos
                                                    //http://app.towpod.net/ws_add_comment.php?idp=7&txt=Esto%20siempre%20es%20as%C3%AD&rate=2&email=danielirias@gmail.com
                                                    String url = "http://app.towpod.net/ws_add_comment.php?" +
                                                            "idp=" + intIdProveedor +
                                                            "&txt=" + txtMyComment.getText() +
                                                            "&rate=" + rateProveedor.getRating() +
                                                            "&email=" + CursorDB.getString(1);

                                                    url = url.replaceAll(" ", "%20");

                                                    //Creamos la petición
                                                    JsonArrayRequest request = new JsonArrayRequest(url,
                                                            new Response.Listener<JSONArray>() {

                                                                @Override
                                                                public void onResponse(JSONArray response)
                                                                {


                                                                }
                                                            },
                                                            new Response.ErrorListener() {
                                                                @Override
                                                                public void onErrorResponse(VolleyError error)
                                                                {
                                                                    dialog.dismiss();

                                                                }
                                                            }
                                                    );

                                                    //Le ponemos un tag que servirá para identificarla si la queremos cancelar
                                                    request.setTag(JSON_REQUEST);
                                                    //Al ponerle la política de reintento, le hacemos casting, ya que el metodo recibe
                                                    //un objeto de la clase padre
                                                    request = (JsonArrayRequest) setRetryPolicy(request);
                                                    //Iniciamos la petición añadiéndola a la cola
                                                    requestQueue.add(request);

                                                    final AlertDialog.Builder builder = new AlertDialog.Builder(actMyAccountHistoryDetail.this);
                                                    //LayoutInflater inflater = actMyAccountHistoryDetail.this.getLayoutInflater();
                                                    //builder.setView(inflater.inflate(R.layout.dialog_set_policy, null));

                                                    builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                                    {
                                                        public void onClick(DialogInterface dialog, int id) {

                                                        }
                                                    });

                                                    AlertDialog alertDialog = builder.create();
                                                    alertDialog.setTitle("¡Muchas gracias!");
                                                    alertDialog.setMessage("Tu comentario ha sido recibido.");
                                                    alertDialog.show();

                                                }
                                                catch (JSONException e)
                                                {

                                                    Toast popUpError = Toast.makeText(actMyAccountHistoryDetail.this, e.getMessage(), Toast.LENGTH_LONG);
                                                    popUpError.show();
                                                }
                                            }
                                            else
                                            {
                                                //Si NO EXISTE LOCALMENTE NI EN LA WEB lo envio al Login para que inicie sesion o se registre
                                                Intent actMainLogin = new Intent(actMyAccountHistoryDetail.this, actMainLogin.class);
                                                startActivity(actMainLogin);
                                                return;
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            //Si recibimos un error, mostraremos la causa

                                        }
                                    }
                            );

                            //Le ponemos un tag que servirá para identificarla si la queremos cancelar
                            requestUser.setTag(JSON_REQUEST);
                            //Al ponerle la política de reintento, le hacemos casting, ya que el metodo recibe un objeto de la clase padre
                            requestUser = (JsonArrayRequest) setRetryPolicy(requestUser);
                            //Iniciamos la petición añadiéndola a la cola
                            requestQueue.add(requestUser);
                        }
                    }
                    catch (Exception e)
                    {
                        Toast popUpError = Toast.makeText(actMyAccountHistoryDetail.this,  e.getMessage(), Toast.LENGTH_LONG);
                        popUpError.show();
                    }

                    //Cerramos la base de datos
                    db.close();

                }
            }
            catch (Exception e)
            {

                return;
            }

        }


    }

    private boolean CampoVacio(EditText myEditText) {
        return myEditText.getText().toString().trim().length() == 0;
    }

    public Request setRetryPolicy(Request request)
    {
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }

}
