package net.towpod;

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
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class actListaProveedores extends AppCompatActivity {
    ListView ListaProveedores;
    ArrayAdapter AdaptadorProveedores;

    public static String SESSION_USER_MAIL = "";


    public ArrayAdapter<String> Adaptador;
    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    public static String EXTRA_ID_CATEGORIA = "extra.id.categoria";
    public static String EXTRA_NOMBRE_CATEGORIA = "extra.nombre.categoria";


    public final static String EXTRA_ID_PROVEEDOR = "extra.id.proveedor";
    public final static String EXTRA_NOMBRE_PROVEEDOR = "extra.nombre.proveedor";
    public final static String EXTRA_DIRECCION_PROVEEDOR = "extra.direccion.proveedor";
    public final static String EXTRA_CIUDAD_PROVEEDOR = "extra.ciudad.proveedor";
    public static String EXTRA_LATITUD_USUARIO = "extra.latitud.usuario";
    public static String EXTRA_LONGITUD_USUARIO = "extra.longitud.usuario";

    public String strLatitud;
    public String strLongitud;

    public Integer intDistanciaBusqueda = 0; //Inicio la distancia del radio de busqueda con 30KM


    LocationManager locationManager;
    double longitudeBest, latitudeBest;
    double longitudeGPS, latitudeGPS;
    double longitudeNetwork, latitudeNetwork;
    TextView longitudeValueBest, latitudeValueBest;
    TextView longitudeValueGPS, latitudeValueGPS;
    TextView longitudeValueNetwork, latitudeValueNetwork;
    public String LocationProvider;

    public Boolean blnDatosCargados = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.menuMyAccount:
                Intent actMyAccount = new Intent(this, actMyAccount.class);
                actMyAccount.putExtra(SESSION_USER_MAIL, SESSION_USER_MAIL);
                startActivity(actMyAccount);
                return true;

            case R.id.menuMyRecord:
                Intent actRegistros = new Intent(this, actMyAccountStores.class);
                actRegistros.putExtra(SESSION_USER_MAIL, SESSION_USER_MAIL);
                startActivity(actRegistros);
                return true;

            case R.id.menuAbout:
                Intent actAbout = new Intent(this, actAbout.class);
                startActivity(actAbout);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_lista_proveedores_content);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Obteniendo la instancia del Intent
        Intent intent = getIntent();
        //Extrayendo el extra de tipo cadena

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        longitudeValueBest = (TextView) findViewById(R.id.longitudeValueBest);
        latitudeValueBest = (TextView) findViewById(R.id.latitudeValueBest);
        longitudeValueGPS = (TextView) findViewById(R.id.longitudeValueGPS);
        latitudeValueGPS = (TextView) findViewById(R.id.latitudeValueGPS);
        longitudeValueNetwork = (TextView) findViewById(R.id.longitudeValueNetwork);
        latitudeValueNetwork = (TextView) findViewById(R.id.latitudeValueNetwork);
        GetGPSUpdates();
        GetNetworkUpdates();
        GetBestUpdates();


        //Seteo el título de la Activity con el nombre de la categoría
        setTitle(intent.getStringExtra(actServiceDirectory.EXTRA_NOMBRE_CATEGORIA));

        ImageView iconoCategoria = (ImageView) findViewById(R.id.iconCategoria);

        //Para cargar el ciono de la categoria localmente:
        String strMyCatNumber = "";
        if (Integer.parseInt(intent.getStringExtra(actServiceDirectory.EXTRA_ID_CATEGORIA)) < 10)
        {
            strMyCatNumber = "cat0" + intent.getStringExtra(actServiceDirectory.EXTRA_ID_CATEGORIA);
        }
        else
        {
            strMyCatNumber = "cat" + intent.getStringExtra(actServiceDirectory.EXTRA_ID_CATEGORIA);
        }
        int resID = getResources().getIdentifier(strMyCatNumber, "drawable", getPackageName());
        iconoCategoria.setImageResource(resID);

        //Para cargar el ciono de la categoria desde el servidor:
        //new AsyncTaskLoadImage(iconoCategoria).execute(intent.getStringExtra(actServiceDirectory.EXTRA_URL_ICONO_CATEGORIA));


        /*FloatingActionButton btnNuevoProveedor = (FloatingActionButton) findViewById(R.id.btnNuevoProveedor);
        btnNuevoProveedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent actAddProveedor = new Intent(actListaProveedores.this, actAddProveedorBienvenida.class);
                Intent intent = getIntent();
                actAddProveedor.putExtra(EXTRA_ID_CATEGORIA, intent.getStringExtra(actServiceDirectory.EXTRA_ID_CATEGORIA));
                actAddProveedor.putExtra(EXTRA_NOMBRE_CATEGORIA, intent.getStringExtra(actServiceDirectory.EXTRA_NOMBRE_CATEGORIA));
                actAddProveedor.putExtra(SESSION_USER_MAIL, intent.getStringExtra(actServiceDirectory.SESSION_USER_MAIL));

                startActivity(actAddProveedor);
            }
        });*/

        final SeekBar sbDistancia  = (SeekBar) findViewById(R.id.sbDistancia);
        final TextView lblRadioDistanciaBusqueda = (TextView) findViewById(R.id.lblRadioDistanciaBusqueda);

        intDistanciaBusqueda = sbDistancia.getProgress();


        lblRadioDistanciaBusqueda.setText(String.valueOf(String.valueOf(sbDistancia.getProgress()) + " km"));

        sbDistancia.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser)
            {
                intDistanciaBusqueda = progresValue;
                lblRadioDistanciaBusqueda.setText(String.valueOf(intDistanciaBusqueda + " km"));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                //Realizo el proceo en segundo plano y muestro el cuadro de espera
                /*ProgressDialog VentanaEsperaReload = new ProgressDialog(actListaProveedores.this);
                VentanaEsperaReload.setTitle("Creando lista");
                VentanaEsperaReload.setMessage("Espere un momento, por favor...");
                new actListaProveedores.ProcesoSegundoPlano(VentanaEsperaReload, actListaProveedores.this).execute();*/
                blnDatosCargados = false;
                CargarProveedores();
                //TextView lblRadioDistanciaBusqueda = (TextView) findViewById(R.id.lblRadioDistanciaBusqueda);
                //lblRadioDistanciaBusqueda.setText(String.valueOf(intDistanciaBusqueda + " km"));
            }

        });

        IniciarCargaDeDatos();

    }

    public void IniciarCargaDeDatos()
    {
        if (blnDatosCargados == true)
        {
            return;
        }

        //Verifico la mejor ubuicacion
        if(latitudeValueBest.getText().toString().equals("N/D"))
        {
            //Si la mejor ubicacion esta vacia verifico el GPS
            //Siempre se solicita la ubicacion por GPS porque es más precisa que la de RED
            if(latitudeValueGPS.getText().toString().equals("N/D"))
            {
                //Si el GPS esta vacio verifico la RED
                //En caso de no tener la de GPS procedo a tomar la de RED
                if(latitudeValueNetwork.getText().toString().equals("N/D"))
                {
                    //Si la de RED tampoco está disponible muestro el mensaje de error
                    //NotificarSinUbicacion();

                    TextView lblActiveLocation = (TextView) findViewById(R.id.lblActiveLocation);
                    lblActiveLocation.setText("Estamos intentando establecer tu ubicación.\n\nEspera un momento...");
                }
                else
                {
                    strLatitud = latitudeValueNetwork.getText().toString();
                    strLongitud = longitudeValueNetwork.getText().toString();

                    //Realizo el proceo en segundo plano y muestro el cuadro de espera
                    ProgressDialog VentanaEspera = new ProgressDialog(this);
                    VentanaEspera.setTitle("Creando lista");
                    VentanaEspera.setMessage("Espere un momento, por favor...");
                    new actListaProveedores.ProcesoSegundoPlano(VentanaEspera, this).execute();

                }
            }
            else
            {
                strLatitud = latitudeValueGPS.getText().toString();
                strLongitud = longitudeValueGPS.getText().toString();

                //Realizo el proceo en segundo plano y muestro el cuadro de espera
                ProgressDialog VentanaEspera = new ProgressDialog(this);
                VentanaEspera.setTitle("Creando lista");
                VentanaEspera.setMessage("Espere un momento, por favor...");
                new actListaProveedores.ProcesoSegundoPlano(VentanaEspera, this).execute();

            }
        }
        else
        {
            strLatitud = latitudeValueBest.getText().toString();
            strLongitud = longitudeValueBest.getText().toString();

            //Realizo el proceo en segundo plano y muestro el cuadro de espera
            ProgressDialog VentanaEspera = new ProgressDialog(this);
            VentanaEspera.setTitle("Creando lista");
            VentanaEspera.setMessage("Espere un momento, por favor...");
            new actListaProveedores.ProcesoSegundoPlano(VentanaEspera, this).execute();
        }


    }

    public void NotificarSinUbicacion()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(actListaProveedores.this);
        LayoutInflater inflater = actListaProveedores.this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_no_location, null));

        /*builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id) {
                //Nada para hacer aquí
            }
        });*/

        builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog alertDialog = builder.create();
        //alertDialog.setTitle("Ubicación no determinada");
        //alertDialog.setMessage("No hemos podido determinar tu ubicación actual.\nPor favor vuelve a intentarlo en un par de minutos.");
        alertDialog.show();
    }

    private class ProcesoSegundoPlano extends AsyncTask<Void, Void, Void>
    {

        ProgressDialog progress;
        actListaProveedores act;

        public ProcesoSegundoPlano(ProgressDialog progress, actListaProveedores act) {
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
            }, 1500);

        }

        protected Void doInBackground(Void... params) {
            //realizar la operación aquí
            CargarProveedores();
            blnDatosCargados = true;
            return null;
        }

    }

    private void CargarProveedores()
    {

        if (strLatitud == "" && strLongitud =="")
        {
            //si no hay coordenadas debo salir del proceso para evitar un error
            return;
        }

        try
        {
            //Instancia del ListView
            ListaProveedores = (ListView)findViewById(R.id.ListView_Proveedores);

            //Obtenemos la instancia única de la cola de peticiones
            requestQueue = RequestQueueSingleton.getRequestQueue(this);

            //Obteniendo la instancia del Intent
            Intent intent = getIntent();
            //URL de las categorías
            String strIDCategoria = intent.getStringExtra(actServiceDirectory.EXTRA_ID_CATEGORIA);

            String url = "http://app.towpod.net/ws_get_category_provider.php?cat="+strIDCategoria+"&lat="+strLatitud+"&lon="+strLongitud+"&dis="+intDistanciaBusqueda;

            //Prueba desde Valle de Ángeles
            //String url = "http://app.towpod.net/ws_get_category_provider.php?cat="+strIDCategoria+"&lat=14.158572&lon=-87.042031&dis="+intDistanciaBusqueda;

            url = url.replaceAll(" ", "%20");

            //Toast popUpError = Toast.makeText(actListaProveedores.this, url , Toast.LENGTH_LONG);
            //popUpError.show();

            //Inicializar el adaptador con la fuente de datos
            List<clsProveedor> Proveedores = new ArrayList<clsProveedor>();
            AdaptadorProveedores = new clsProveedorArrayAdapter(this, Proveedores);
            AdaptadorProveedores.clear();
            //Creamos la petición
            JsonArrayRequest request = new JsonArrayRequest(url,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            //TextView txtDescripcion = (TextView) findViewById(R.id.textoDescripcionCategoria);

                            try {
                                response.getJSONObject(0).getString("category_description");
                                if (response.length()==0)
                                {
                                    LinearLayout loListado = (LinearLayout) findViewById(R.id.loListado);
                                    loListado.setVisibility(View.GONE);

                                    RelativeLayout loNoData = (RelativeLayout) findViewById(R.id.loNoData);
                                    loNoData.setVisibility(View.VISIBLE);

                                    TextView lblActiveLocation = (TextView) findViewById(R.id.lblActiveLocation);
                                    lblActiveLocation.setText("No se encontraron proveedores de servicio cercanos a ti. Intenta con una distancia mayor.");
                                }
                                else
                                {
                                    TextView lblMainMsg = (TextView) findViewById(R.id.lblMainMsg);
                                    lblMainMsg.setText("Estamos organizado los datos");

                                    TextView lblActiveLocation = (TextView) findViewById(R.id.lblActiveLocation);
                                    lblActiveLocation.setText("Espera un momento...");
                                    for (int i = 0; i < response.length(); i++){
                                        try {
                                            //txtDescripcion.setText(response.getJSONObject(0).getString("category_description"));
                                            AdaptadorProveedores.add(new clsProveedor(
                                                    response.getJSONObject(i).getString("provider_id"),
                                                    response.getJSONObject(i).getString("provider_name"),
                                                    response.getJSONObject(i).getString("provider_address"),
                                                    response.getJSONObject(i).getString("provider_city"),
                                                    response.getJSONObject(i).getDouble("provider_lat"),
                                                    response.getJSONObject(i).getDouble("provider_lon"),
                                                    response.getJSONObject(i).getDouble("distance"),
                                                    response.getJSONObject(i).getInt("provider_rate")));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    LinearLayout loListado = (LinearLayout) findViewById(R.id.loListado);
                                    loListado.setVisibility(View.VISIBLE);

                                    RelativeLayout loNoData = (RelativeLayout) findViewById(R.id.loNoData);
                                    loNoData.setVisibility(View.GONE);
                                }
                            } catch (JSONException e) {
                                Toast popUpError = Toast.makeText(actListaProveedores.this, e.getMessage() , Toast.LENGTH_LONG);
                                popUpError.show();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //Si recibimos un error, mostraremos la causa
                            Toast popUpError = Toast.makeText(actListaProveedores.this, error.getMessage() , Toast.LENGTH_LONG);
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

            //Inicializar el adaptador con la fuente de datos
            AdaptadorProveedores = new clsProveedorArrayAdapter(this, Proveedores);

            //Relacionando la lista con el adaptador
            ListaProveedores.setAdapter(AdaptadorProveedores);

            //Creo el evento Clic para cada objeto de la lista
            ListaProveedores.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {
                    clsProveedor proveedorActual = (clsProveedor) pariente.getItemAtPosition(posicion);
                    CharSequence texto = "Seleccionado: "+proveedorActual.getNombre()+"-"+ proveedorActual.getDireccion()+"="+strLatitud+", "+strLongitud;
                    Toast toast = Toast.makeText(actListaProveedores.this, texto, Toast.LENGTH_SHORT);
                    //toast.show();


                    Intent actDetalleProveedor = new Intent(actListaProveedores.this, actInfoProveedor.class);
                    actDetalleProveedor.putExtra(EXTRA_ID_PROVEEDOR, proveedorActual.getIdProveedor());
                    actDetalleProveedor.putExtra(EXTRA_LATITUD_USUARIO, strLatitud);
                    actDetalleProveedor.putExtra(EXTRA_LONGITUD_USUARIO, strLongitud);

                    startActivity(actDetalleProveedor);
                }
            });

        }
        catch (Exception e)
        {
            Toast popUpError = Toast.makeText(actListaProveedores.this, e.getMessage() , Toast.LENGTH_LONG);
            popUpError.show();
        }

    }

    //Le pone una política de 4 reintentos con 4 segundos entre cada uno y la devuelve.
    public Request setRetryPolicy(Request request)
    {
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
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

            longitudeValueGPS.setText(longitudeGPS + "");
            latitudeValueGPS.setText(latitudeGPS + "");

            IniciarCargaDeDatos();

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

            longitudeValueNetwork.setText(longitudeNetwork + "");
            latitudeValueNetwork.setText(latitudeNetwork + "");

            IniciarCargaDeDatos();

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
        LocationProvider = locationManager.getBestProvider(criteria, true);
        if (LocationProvider != null)
        {
            locationManager.requestLocationUpdates(LocationProvider, 3000, 1, locationListenerBest);
            //Toast.makeText(this, "USANDO: " + Proveedor, Toast.LENGTH_LONG).show();
        }

    }

    private final LocationListener locationListenerBest = new LocationListener()
    {
        public void onLocationChanged(Location location)
        {
            latitudeBest = location.getLatitude();
            longitudeBest = location.getLongitude();

            longitudeValueBest.setText(longitudeBest + "");
            latitudeValueBest.setText(latitudeBest + "");

            IniciarCargaDeDatos();

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
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //==============================================================================================
    //FIN DE LOCALIZACION===========================================================================
    //==============================================================================================

}
