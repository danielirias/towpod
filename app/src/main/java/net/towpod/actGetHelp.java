package net.towpod;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class actGetHelp extends AppCompatActivity  implements OnMapReadyCallback {

    private RequestQueue requestQueue;
    private RequestQueue requestQueueFixed;
    private RequestQueue requestQueueFixedDistance;
    private final static int JSON_REQUEST = 0;

    ListView ListaCategorias;
    ArrayAdapter AdaptadorCategorias;

    ListView ListaPolizas;
    ArrayAdapter AdaptadorPolizas;

    ListView ListaTarjetas;
    ArrayAdapter AdaptadorTarjetas;

    ListView ListaProveedores;
    ArrayAdapter AdaptadorProveedores;

    GridView ListaAutos;
    ArrayAdapter AdaptadorAutos;

    public Integer intHayPolizas = 0;
    public String strComentario;
    public String strCustomerEmail;
    public Integer intIdTipoAsistencia;
    public String strPolicyNumber = "";
    public String strNombreAsistencia;
    public Integer intIdProveedor;
    public String strNombreProveedor;
    public Double dblPrecioServicio;
    public Integer intPaymentMethod;
    public String strLatitud;
    public String strLongitud;

    public Integer intDestinationRequired;
    public Integer intDriverRequired;

    public String strCardNumber = "";

    public String strCarID = "";

    LocationManager locationManager;
    double longitudeBest, latitudeBest;
    double longitudeGPS, latitudeGPS;
    double longitudeNetwork, latitudeNetwork;
    TextView longitudeValueBest, latitudeValueBest;
    TextView longitudeValueGPS, latitudeValueGPS;
    TextView longitudeValueNetwork, latitudeValueNetwork;

    public String strLocationProvider;

    public Marker currentPositionMarker = null;
    private GoogleMap mMap;
    Double currentLat, currentLong;
    
    public String[] providerPhone = new String[3];

    public Integer nPhones;
    public String strNumberToDial = "";
    Double fixed_service_value = 0.0;

    Double DistanciaReal = 0.0;

    public String strFinalURL = "";

    public static String extraCurrentLat = "extraCurrentLat";
    public static String extraCurrentLong = "extraCurrentLong";
    public static String extraNombreAsistencia = "extraNombreAsistencia";
    public static String extraIdAsistencia = "extraIdAsistencia";
    public static String extraDriver = "extraDriver";

	public String strAreaCode = "";

	public String strPartDescription;
	public Integer intOnlyForSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_get_help);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("¿Qué necesitas?");

        //Realizo el proceo en segundo plano y muestro el cuadro de espera
        ProgressDialog VentanaEspera = new ProgressDialog(this);
        VentanaEspera.setTitle("Creando lista de servicios");
        VentanaEspera.setMessage("Espere un momento, por favor...");
        new actGetHelp.ProcesoSegundoPlano(VentanaEspera, this).execute();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(actGetHelp.this);

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

    }

    private class ProcesoSegundoPlano extends AsyncTask<Void, Void, Void>
    {

        ProgressDialog progress;
        actGetHelp act;

        public ProcesoSegundoPlano(ProgressDialog progress, actGetHelp act) {
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
            CargarCategoriasAsistencia();
            return null;
        }

    }

    private void CargarCategoriasAsistencia()
    {
        VerificarExistenciaDePolizas(); //Aquí verifico si hay alguna poliza disponible para el usuario

        //======================================================================
        //Cargar las categorias
        //======================================================================
        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this);

        //Instancia del GridView
        ListaCategorias = (ListView) findViewById(R.id.ListViewAsistencia);

        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this);

        //URL de las categorías
        String url = "http://app.towpod.net/ws_get_assistance_list.php";
        url = url.replaceAll(" ", "%20");

        //Inicializar el adaptador con la fuente de datos
        List<clsCategoriaAsistencia> clsCategoriaAsistencia = new ArrayList<clsCategoriaAsistencia>();
        AdaptadorCategorias = new clsCategoriaAsistenciaArrayAdapter(this, clsCategoriaAsistencia);
        AdaptadorCategorias.clear();

        //Creamos la petición
        JsonArrayRequest request = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response)
                    {
                        for (int i = 0; i < response.length(); i++){
                            try {
                                AdaptadorCategorias.add(new clsCategoriaAsistencia(
                                        response.getJSONObject(i).getString("assistance_id"),
                                        response.getJSONObject(i).getString("assistance_name"),
                                        response.getJSONObject(i).getInt("assistance_destination_required"),
                                        response.getJSONObject(i).getInt("assistance_driver_required"),
                                        response.getJSONObject(i).getInt("only_for_search")));
                            }
                            catch (JSONException e)
                            {
                                Toast popUpError = Toast.makeText(actGetHelp.this, e.toString() , Toast.LENGTH_LONG);
                                popUpError.show();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Si recibimos un error, mostraremos la causa
                        Toast popUpError = Toast.makeText(actGetHelp.this, error.getMessage() , Toast.LENGTH_LONG);
                        //popUpError.show();
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
        AdaptadorCategorias = new clsCategoriaAsistenciaArrayAdapter(this, clsCategoriaAsistencia);

        //Relacionando la lista con el adaptador
        ListaCategorias.setAdapter(AdaptadorCategorias);

        //Creo el evento Clic para cada objeto de la lista
        ListaCategorias.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id)
            {
	            final clsCategoriaAsistencia CategoriaSeleccionada = (clsCategoriaAsistencia) pariente.getItemAtPosition(posicion);

	            intIdTipoAsistencia = Integer.parseInt(CategoriaSeleccionada.getIdCategoria());
	            strNombreAsistencia = CategoriaSeleccionada.getNombreCategoria();
	            intDestinationRequired = CategoriaSeleccionada.getDestinationRequired();
	            intDriverRequired = CategoriaSeleccionada.getDriverRequired();
	            intOnlyForSearch = CategoriaSeleccionada.getOnlyForSearch();

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
                            DialogMostrarUbicacionNoEstablecida();
                            return;
                        }
                        else
                        {
                            strLatitud = latitudeValueNetwork.getText().toString();
                            strLongitud = longitudeValueNetwork.getText().toString();
                        }
                    }
                    else
                    {
                        strLatitud = latitudeValueGPS.getText().toString();
                        strLongitud = longitudeValueGPS.getText().toString();
                    }
                }
                else
                {
                    strLatitud = latitudeValueBest.getText().toString();
                    strLongitud = longitudeValueBest.getText().toString();
                }



                if (intOnlyForSearch == 1)
                {
                	//Si solo es para búsqueda de repuestos
	                //1. Selecciono el vehículo
	                DialogoMostrarAutosDisponibles(SendCarTo.PartDescription);
                }
                else if(intDestinationRequired == 1)
                {
                    //========================SI REQUIERE DESTINO=============================
                    Intent actDestination = new Intent(actGetHelp.this, actDestination.class);
                    actDestination.putExtra(extraCurrentLat, currentLat.toString());
                    actDestination.putExtra(extraCurrentLong, currentLong.toString());
                    actDestination.putExtra(extraNombreAsistencia, strNombreAsistencia);
                    actDestination.putExtra(extraIdAsistencia, intIdTipoAsistencia.toString());
                    actDestination.putExtra(extraDriver, intDriverRequired.toString());

                    startActivity(actDestination);
                }
                else
                {
                    //========================NO REQUIERE DESTINO=============================
                    VerificarExistenciaDePolizas();//Verifico si hay polizas

                    //===========================================================================================================
                    //Verifico si hay proveedores cercanos para el tipo de asistencia cercanos a la ubicación y si trabajan con poliza o no
                    //String url = "http://app.towpod.net/ws_get_assistance_value.php?id_ta="+intIdTipoAsistencia+"&lat="+strLatitud+"&lon="+strLongitud+"&use_policy="+intHayPolizas;

                    final String url = "http://app.towpod.net/ws_get_assistance_closer_provider_value.php?id_ta="+intIdTipoAsistencia+"&lat="+strLatitud+"&lon="+strLongitud+"&use_policy="+intHayPolizas;
                    //final String url = "http://app.towpod.net/ws_get_assistance_closer_provider_value.php?id_ta=6&lat=14.071795&lon=-87.202584&use_policy=0";

	                Log.i("TP_TAG", url);


                    //url = url.replaceAll(" ", "%20");

                    //Inicializar el adaptador con la fuente de datos
                    List<clsCategoriaAsistencia> clsCategoriaAsistencia = new ArrayList<clsCategoriaAsistencia>();

                    //Creamos la petición
                    JsonArrayRequest request = new JsonArrayRequest(url,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response)
                                {
                                    if (response.length() > 0)
                                    {
                                        try
                                        {
                                            response.getJSONObject(0).getInt("provider_id");


                                            //BYPASS para hacer llamadas directas sin mas procedimientos
                                            //Este no es l método que desearía implementar pero debido a que los proveedores prefieren reibir una llamada
                                            //no me queda otra opción que hacer un bypass de procesos

                                            if(intHayPolizas == 0)
                                            {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(actGetHelp.this);
                                                LayoutInflater inflater = actGetHelp.this.getLayoutInflater();
                                                builder.setView(inflater.inflate(R.layout.dialog_proveedores_disponibles, null));

                                                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //Nada para hacer aquí
                                                    }
                                                });


                                                AlertDialog alertDialog = builder.create();
                                                alertDialog.show();

                                                //==========================================================================================

                                                //Instancia del ListView
                                                ListaProveedores = (ListView)alertDialog.findViewById(R.id.ListView_Proveedores);

                                                //Inicializar el adaptador con la fuente de datos
                                                List<clsProveedorCercano> Proveedores = new ArrayList<clsProveedorCercano>();
                                                AdaptadorProveedores = new clsProveedorCercanoArrayAdapter(actGetHelp.this, Proveedores);
                                                AdaptadorProveedores.clear();

                                                for (int i = 0; i < response.length(); i++)
                                                {
                                                    try
                                                    {
                                                        //====================================================================================================================
                                                        //Recupero el precio en base a la zona de cobertura

                                                        //txtDescripcion.setText(response.getJSONObject(0).getString("category_description"));
                                                        AdaptadorProveedores.add(new clsProveedorCercano(
                                                                response.getJSONObject(i).getString("provider_id"),
                                                                response.getJSONObject(i).getString("provider_name"),
                                                                response.getJSONObject(i).getInt("assistance_id"),
                                                                response.getJSONObject(i).getDouble("service_value"),
                                                                response.getJSONObject(i).getInt("provider_rate"),
                                                                response.getJSONObject(i).getDouble("distance"),
                                                                response.getJSONObject(i).getString("provider_phone"),
                                                                response.getJSONObject(i).getString("provider_phone2"),
                                                                response.getJSONObject(i).getString("whatsapp_number"),
                                                                response.getJSONObject(i).getInt("assistance_area_id"),
                                                                response.getJSONObject(i).getString("assistance_area_name"),
                                                                response.getJSONObject(i).getString("aprox_time"),
                                                                response.getJSONObject(i).getString("country_currency"),
                                                                response.getJSONObject(i).getDouble("total_service_value")));
                                                        //====================================================================================================================
                                                    } catch (JSONException e)
                                                    {
                                                        Toast popUpError = Toast.makeText(actGetHelp.this, "ERROR DATA 0:"+e.getMessage() , Toast.LENGTH_LONG);
                                                        popUpError.show();
                                                    }
                                                }


                                                //Inicializar el adaptador con la fuente de datos
                                                AdaptadorProveedores = new clsProveedorCercanoArrayAdapter(actGetHelp.this, Proveedores);

                                                //Relacionando la lista con el adaptador
                                                ListaProveedores.setAdapter(AdaptadorProveedores);


                                                //Creo el evento Clic para cada objeto de la lista
                                                ListaProveedores.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id)
                                                    {
                                                        clsProveedorCercano itemSeleccionado = (clsProveedorCercano) pariente.getItemAtPosition(posicion);

                                                        intIdProveedor = Integer.parseInt(itemSeleccionado.get_id_proveedor());
                                                        strComentario = "";
                                                        dblPrecioServicio = 0.0;
                                                        intPaymentMethod = 2;
                                                        dblPrecioServicio = itemSeleccionado.get_service_value_fixed();

                                                        providerPhone[0] = itemSeleccionado.get_Phone_1();
                                                        providerPhone[1] = itemSeleccionado.get_Phone_2();
                                                        providerPhone[2] = itemSeleccionado.get_Phone_3();

                                                        DialogMostrarFormasDeContacto();

                                                    }
                                                });

                                            }


                                            if(intHayPolizas == 1)
                                            {
                                                DialogoMostrarPolizasDisponibles();

                                                //A PARTIR DE ESTA LINE EL CODIGO FUE COMENTADO PARA USAR EL BYPASS DE ARRIBA

                                                //Si hay algun proveedor cercano muestro el cuadro para ingresar la descripcion del problema
                                                //y paso los valores a las variables
	                                            /*
	                                            intIdProveedor = response.getJSONObject(0).getInt("provider_id");
	                                            dblPrecioServicio = response.getJSONObject(0).getDouble("service_value");
	                                            strNombreProveedor  = response.getJSONObject(0).getString("provider_name");

	                                            AlertDialog.Builder builder = new AlertDialog.Builder(actGetHelp.this);
	                                            LayoutInflater inflater = actGetHelp.this.getLayoutInflater();
	                                            builder.setView(inflater.inflate(R.layout.dialog_get_help_comment, null));

	                                            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
	                                            {
	                                                public void onClick(DialogInterface dialog, int id) {
	                                                    //Nada para hacer aquí
	                                                }
	                                            });

	                                            builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener()
	                                            {
	                                                public void onClick(DialogInterface dialog, int id) {
	                                                    //El código se movió a la clase CustomListener
	                                                }
	                                            });

	                                            AlertDialog alertDialog = builder.create();
	                                            alertDialog.setTitle(CategoriaSeleccionada.getNombreCategoria());
	                                            alertDialog.show();

	                                            Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
	                                            theButton.setOnClickListener(new actGetHelp.CustomListener(alertDialog));
	                                            */

                                            }


                                        }
                                        catch (JSONException e)
                                        {
                                            Toast popUpError = Toast.makeText(actGetHelp.this, e.toString() , Toast.LENGTH_LONG);
                                            //popUpError.show();
                                        }
                                    }
                                    else
                                    {

                                        //Si no hay proveedores cercanos
                                        AlertDialog.Builder builder = new AlertDialog.Builder(actGetHelp.this);
                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {

                                            }
                                        });
                                        AlertDialog alertDialog = builder.create();
                                        alertDialog.setTitle("Lo sentimos");
                                        alertDialog.setMessage("No encontramos cerca de ti la ayuda que solicitaste.");
                                        alertDialog.show();

                                        /*
                                        final View coordinatorLayoutView = findViewById(R.id.snackbarPosition);
                                        Snackbar.make(coordinatorLayoutView, "No hay proveedores de asistencia cercanos a ti.", Snackbar.LENGTH_LONG)
                                                //.setActionTextColor(Color.CYAN)
                                                //Color del texto de la acción
                                                .setActionTextColor(Color.parseColor("#FFC107"))
                                                .setAction("Aceptar", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view)
                                                    {

                                                    }
                                                })
                                                .show();
                                        */
                                        return;
                                    }

                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    //Si recibimos un error, mostraremos la causa
                                    Toast popUpError = Toast.makeText(actGetHelp.this, error.getMessage() , Toast.LENGTH_LONG);
                                    //popUpError.show();
                                }
                            }
                    );

                    //Le ponemos un tag que servirá para identificarla si la queremos cancelar
                    request.setTag(JSON_REQUEST);
                    //Al ponerle la política de reintento, le hacemos casting, ya que el metodo recibe un objeto de la clase padre
                    request = (JsonArrayRequest) setRetryPolicy(request);
                    //Iniciamos la petición añadiéndola a la cola
                    requestQueue.add(request);
                    //===========================================================================================================

                    return;
                }




            }
        });
        //======================================================================
    }

    public void VerificarExistenciaDePolizas ()
    {
        //Verifico si tiene polizas registradas
        try {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(this, "USUARIOSHM", null, 2);
            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Si hemos abierto correctamente la base de datos
            if (db != null) {
                String[] campos = new String[]{"user_id", "user_email", "user_password", "user_keep_session"};
                final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

                try {
                    CursorDB.moveToFirst();
                    if (CursorDB.getCount() == 0)
                    {
                        actGetHelp.this.finish();
                        //Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
                        Intent actMainLogin = new Intent(this, actMainLogin.class);
                        startActivity(actMainLogin);
                        return;
                    }
                    else
                    {
                        //==========================================================================================
                        //=======CARGA DE POLIZAS DE LOS USUARIOS===============================================
                        //==========================================================================================

                        //Obtenemos la instancia única de la cola de peticiones
                        requestQueue = RequestQueueSingleton.getRequestQueue(this);

                        //Inicializar el adaptador con la fuente de datos
                        List<clsPoliza> Polizas = new ArrayList<clsPoliza>();
                        AdaptadorPolizas = new clsPolizaArrayAdapter(this, Polizas);
                        AdaptadorPolizas.clear();

                        String urlPoliza = "http://app.towpod.net/ws_get_customer_policy.php?idc=" +CursorDB.getInt(0);
                        urlPoliza = urlPoliza.replaceAll(" ", "%20");

                        strCustomerEmail = CursorDB.getString(1);

                        JsonArrayRequest requestPoliza = new JsonArrayRequest(urlPoliza,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray responsePoliza)
                                    {
                                        try
                                        {
                                            if (responsePoliza.length() > 0)
                                            {
                                                intHayPolizas = 1;
                                                String strcarID = responsePoliza.getJSONObject(0).getString("car_id");
                                                //MostrarOpcionPagoAsistencia();
                                                DialogoMostrarPolizasDisponibles();
                                            }
                                            else
                                            {
                                                intHayPolizas = 0;
                                                //Si no hay polizas registradas

                                                //SeleccionarProveedorMasCercano();
                                                //DialogMostrarPrecioAsistencia();

                                                //DialogMostrarPrecioAsistencia();
                                                //DialogoMostrarTarjetasDisponibles();
                                                //Intent actGetHelpWithPolicy = new Intent(actGetHelp.this, actGetHelp.class);
                                                //startActivity(actGetHelpWithPolicy);
                                            }
                                        }
                                        catch (JSONException e)
                                        {



                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        //Si recibimos un error, mostraremos la causa
                                        //Toast popUpError = Toast.makeText(actProviderData.this, error.getMessage(), Toast.LENGTH_LONG);
                                        //popUpError.show();
                                    }
                                }
                        );

                        //Le ponemos un tag que servirá para identificarla si la queremos cancelar
                        requestPoliza.setTag(JSON_REQUEST);
                        //Al ponerle la política de reintento, le hacemos casting, ya que el metodo recibe un objeto de la clase padre
                        requestPoliza = (JsonArrayRequest) setRetryPolicy(requestPoliza);
                        //Iniciamos la petición añadiéndola a la cola
                        requestQueue.add(requestPoliza);

                        //Inicializar el adaptador con la fuente de datos
                        AdaptadorPolizas = new clsPolizaArrayAdapter(this, Polizas);

                        //==========================================================================================
                        //=======FIN DE CARGA DE POLIZAS DE LOS USUARIOS========================================
                    }
                } catch (Exception e) {
                    Toast popUpError = Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
                    popUpError.show();
                }

                //Cerramos la base de datos
                db.close();

            }
        } catch (Exception e) {
            return;
        }
    }

    public void DialogMostrarFormasDeContacto()
    {

        nPhones = 1;

        if (CadenaVacia(providerPhone[0].toString()) == true) {
            Toast popUpError = Toast.makeText(actGetHelp.this, "El número telefónico no está disponible.", Toast.LENGTH_SHORT);
            popUpError.show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(actGetHelp.this);
        LayoutInflater inflater = actGetHelp.this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_make_call, null));

        if (CadenaVacia(providerPhone[1].toString()) == false) {
            nPhones = nPhones + 1;
        }

        if (CadenaVacia(providerPhone[2].toString()) == false) {
            nPhones = nPhones + 1;
        }

        final CharSequence[] items = new CharSequence[nPhones];

        for (int i = 1; i <= nPhones; i++) {
            items[i - 1] = providerPhone[i - 1];
        }

        //Asigno el número por default, o sea el primero
        strNumberToDial = items[0].toString();

        /*
        builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int wich) {
                Toast popUp = Toast.makeText(actGetHelp.this, "Has marcado el número: " + items[wich].toString(), Toast.LENGTH_SHORT);
                //popUp.show();
                strNumberToDial = items[wich].toString();
            }
        });
        */

        // Add the buttons
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast popUp = Toast.makeText(actGetHelp.this, "Has cancelado tu llamada", Toast.LENGTH_SHORT);
                //popUp.show();
            }
        });

        /*
        builder.setPositiveButton("Llamar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast popUp = Toast.makeText(actGetHelp.this, "Llamando", Toast.LENGTH_SHORT);
                //popUp.show();
                //Intent callIntent = new Intent(Intent.ACTION_DIAL); //Escribe el número telefónico pero no lo marca
                Intent callIntent = new Intent(Intent.ACTION_CALL); //Marca el número inmediatamente
                callIntent.setData(Uri.parse("tel:" + strNumberToDial));
                if (ActivityCompat.checkSelfPermission(actGetHelp.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(callIntent);

            }
        });
        */

        AlertDialog dialog = builder.create();
        //dialog.setTitle("Selecciona un número telefónico");
        dialog.show();

        //String[] sistemas = {"Ubuntu", "Android", "iOS", "Windows", "Mac OSX", "Google Chrome OS", "Debian", "Mandriva", "Solaris", "Unix"};


        List<clsPhones> Phones = new ArrayList<clsPhones>();
        ArrayAdapter AdaptadorPhones;
        AdaptadorPhones = new clsPhonesArrayAdapter(this, Phones);
        AdaptadorPhones.clear();


        for(int i = 1; i <= nPhones; i++)
        {
            AdaptadorPhones.add(new clsPhones(providerPhone[i-1]));
        }


        GridView ListViewPhones = (GridView) dialog.findViewById(R.id.ListViewPhones);
        ListViewPhones.setAdapter(AdaptadorPhones);
        //Creo el evento Clic para cada objeto de la lista
        ListViewPhones.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {
                clsPhones itemSeleccionado = (clsPhones) pariente.getItemAtPosition(posicion);

	            if (posicion < 2){
	                Intent callIntent = new Intent(Intent.ACTION_CALL); //Marca el número inmediatamente
		            callIntent.setData(Uri.parse("tel:"+strAreaCode+itemSeleccionado.get_phone_number()));
	                if (ActivityCompat.checkSelfPermission(actGetHelp.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
	                    // TODO: Consider calling
	                    //    ActivityCompat#requestPermissions
	                    // here to request the missing permissions, and then overriding
	                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
	                    //                                          int[] grantResults)
	                    // to handle the case where the user grants the permission. See the documentation
	                    // for ActivityCompat#requestPermissions for more details.
	                    return;
	                }
	                startActivity(callIntent);
	            }
	            else
	            {
		            //================
		            Intent _intencion = new Intent("android.intent.action.MAIN");
		            _intencion.setComponent(new ComponentName("com.whatsapp","com.whatsapp.Conversation"));
		            _intencion.putExtra("jid", strAreaCode+providerPhone[2]+"@s.whatsapp.net");
		            startActivity(_intencion);
	            }

            }
        });

    }

    public void EnviarUbicacionDesdeFormasDeContacto(View view)
    {
        //Cuando el usuario hace clic en el boton de Enviar ubicacion
        
        if(intHayPolizas == 0)
        {
            //Muestro los atos disponibles para que seleccione
            DialogoMostrarAutosDisponibles(SendCarTo.ProblemDescription);
        }

        if(intHayPolizas == 1)
        {
            // Muestro las polizas disponibles.
            // Cada poliza está asociada a un vehiculo por lo que no necesito preguntar cual es el vehiculo que necesita asistencia. 
            DialogoMostrarPolizasDisponibles();
        }

        //Realizo el proceo en segundo plano y muestro el cuadro de espera
       /* ProgressDialog VentanaEspera = new ProgressDialog(actGetHelp.this);
        VentanaEspera.setTitle("Enviando datos");
        VentanaEspera.setMessage("Espere un momento, por favor...");
        new actGetHelp.SolicitarAsistenciaVialEnSegundoPlano(VentanaEspera, actGetHelp.this).execute();*/
    }

    public void DialogoMostrarAutosDisponibles(final Integer SendTo)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(actGetHelp.this);
        LayoutInflater inflater = actGetHelp.this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_select_car, null));

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {

            }
        });

        /*builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {

            }
        });*/

        final AlertDialog alertDialogMyCar = builder.create();
        alertDialogMyCar.show();

        try {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actGetHelp.this, "USUARIOSHM", null, 2);
            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Si hemos abierto correctamente la base de datos
            if (db != null) {
                String[] campos = new String[]{"user_id", "user_email", "user_password", "user_keep_session"};
                final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

                try {
                    CursorDB.moveToFirst();
                    if (CursorDB.getCount() == 0) {
                        //Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
                        Intent actMainLogin = new Intent(actGetHelp.this, actMainLogin.class);
                        startActivity(actMainLogin);
                        return;

                    }
                    else
                    {
                        //Instancia del ListView
                        ListaAutos = (GridView) alertDialogMyCar.findViewById(R.id.ListViewAutos);

                        //Obtenemos la instancia única de la cola de peticiones
                        requestQueue = RequestQueueSingleton.getRequestQueue(actGetHelp.this);

                        //Inicializar el adaptador con la fuente de datos
                        List<clsAuto> Autos = new ArrayList<clsAuto>();
                        AdaptadorAutos = new clsAutoArrayAdapter(actGetHelp.this, Autos);
                        AdaptadorAutos.clear();

                        //URL del detalle del proveedor
                        String urlAuto = "http://app.towpod.net/ws_get_customer_car.php?idc=" + CursorDB.getString(0);
                        urlAuto = urlAuto.replaceAll(" ", "%20");

                        JsonArrayRequest requestAuto = new JsonArrayRequest(urlAuto,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray responseAuto) {
                                        if (responseAuto.length() >0 )
                                        {
                                            for (int i = 0; i < responseAuto.length(); i++){
                                                try
                                                {
                                                    RadioButton opcionAuto = new RadioButton(actGetHelp.this);
                                                    LinearLayout.LayoutParams params = new RadioGroup.LayoutParams(
                                                            RadioGroup.LayoutParams.WRAP_CONTENT,
                                                            RadioGroup.LayoutParams.WRAP_CONTENT);

                                                    opcionAuto.setLayoutParams(params);
                                                    opcionAuto.setPadding(0, 0, 0, 15);
                                                    opcionAuto.setId(i);
                                                    opcionAuto.setText(responseAuto.getJSONObject(i).getString("car_brand") + ", " + responseAuto.getJSONObject(i).getString("car_model") + " (" + responseAuto.getJSONObject(i).getString("car_year") +")\n[" + responseAuto.getJSONObject(i).getString("car_id") + "]");
                                                    opcionAuto.setTag(responseAuto.getJSONObject(i).getString("car_id"));

                                                    //grupoAutos.addView(opcionAuto);

                                                    AdaptadorAutos.add(new clsAuto(
                                                            responseAuto.getJSONObject(i).getString("car_id"),
                                                            responseAuto.getJSONObject(i).getString("car_brand"),
                                                            responseAuto.getJSONObject(i).getString("car_model"),
                                                            responseAuto.getJSONObject(i).getInt("car_year")));

                                                    if (i == 0)
                                                    {
                                                        opcionAuto.setChecked(true);
                                                    }

                                                }
                                                catch (JSONException e)
                                                {
                                                    //Toast popUpError = Toast.makeText(actProviderData.this, e.toString() , Toast.LENGTH_LONG);
                                                    //popUpError.show();
                                                }
                                            }
                                        }
                                        else
                                        {
                                            LinearLayout loNoCars = (LinearLayout) alertDialogMyCar.findViewById(R.id.loNoCars);
                                            //loNoCard.setVisibility(View.VISIBLE);
                                            //RelativeLayout loNoSignal = (RelativeLayout) findViewById(R.id.loNoSignal);
                                            LinearLayout.LayoutParams medidasLAYOUT = (LinearLayout.LayoutParams) loNoCars.getLayoutParams();
                                            medidasLAYOUT.height = medidasLAYOUT.MATCH_PARENT;
                                            loNoCars.setLayoutParams(medidasLAYOUT);
                                        }

                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        //Si recibimos un error, mostraremos la causa
                                        //Toast popUpError = Toast.makeText(actProviderData.this, error.getMessage(), Toast.LENGTH_LONG);
                                        //popUpError.show();
                                    }
                                }
                        );

                        //Le ponemos un tag que servirá para identificarla si la queremos cancelar
                        requestAuto.setTag(JSON_REQUEST);
                        //Al ponerle la política de reintento, le hacemos casting, ya que el metodo recibe un objeto de la clase padre
                        requestAuto = (JsonArrayRequest) setRetryPolicy(requestAuto);
                        //Iniciamos la petición añadiéndola a la cola
                        requestQueue.add(requestAuto);


                        //Inicializar el adaptador con la fuente de datos
                        AdaptadorAutos = new clsAutoArrayAdapter(actGetHelp.this, Autos);

                        //Relacionando la lista con el adaptador
                        ListaAutos.setAdapter(AdaptadorAutos);

                        //Creo el evento Clic para cada objeto de la lista
                        ListaAutos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {
                                final clsAuto autoSeleccionado = (clsAuto) pariente.getItemAtPosition(posicion);
                                strCarID = autoSeleccionado.get_car_id();
                                //alertDialogMyCar.dismiss();

                                //Solicito el mensaje descriptivo
	                            if(SendTo == SendCarTo.ProblemDescription) {
	                            	Log.i("TP_TAG", "Descripción del problema");
		                            SolicitarDescripcionProblema();
	                            }
	                            if(SendTo == SendCarTo.PartDescription) {
		                            SolicitarDescripcionRepuesto();
	                            }

                                //Realizo el proceo en segundo plano y muestro el cuadro de espera
                                /*ProgressDialog VentanaEspera = new ProgressDialog(actGetHelp.this);
                                VentanaEspera.setTitle("Enviando datos");
                                VentanaEspera.setMessage("Espere un momento, por favor...");
                                new actGetHelp.SolicitarAsistenciaVialEnSegundoPlano(VentanaEspera, actGetHelp.this).execute();*/
                            }

                        });

                    }
                } catch (Exception e) {
                    Toast popUpError = Toast.makeText(actGetHelp.this, e.getMessage(), Toast.LENGTH_LONG);
                    popUpError.show();
                }

                //Cerramos la base de datos
                db.close();

            }
        } catch (Exception e) {
            return;
        }

        Button theButton = alertDialogMyCar.getButton(DialogInterface.BUTTON_POSITIVE);
        //theButton.setOnClickListener(new actPolicyType.CustomListenerAutos(alertDialogCard));

    }

    public void DialogoMostrarPolizasDisponibles()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(actGetHelp.this);
        LayoutInflater inflater = actGetHelp.this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_select_my_policy, null));

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {

            }
        });

        /*builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {

            }
        });*/

        final AlertDialog alertDialogPolicy = builder.create();
        alertDialogPolicy.show();

        final RadioGroup grupoPolizas = (RadioGroup) alertDialogPolicy.findViewById(R.id.grupoPolizas);

        grupoPolizas.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected

                RadioButton optSelecccionada = (RadioButton) grupoPolizas.getChildAt(checkedId);

                /*strCardNumber = optSelecccionada.getText().toString();
                String[] parts = strCardNumber.split("\\ - "); // escape
                strCardNumber = parts[0];

                strCardNumber = strCardNumber.toString().trim();*/

                Toast popUp = Toast.makeText(actGetHelp.this, "Hola", Toast.LENGTH_LONG);
                //popUp.show();
            }
        });

        try {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actGetHelp.this, "USUARIOSHM", null, 2);
            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Si hemos abierto correctamente la base de datos
            if (db != null) {
                String[] campos = new String[]{"user_id", "user_email", "user_password", "user_keep_session"};
                final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

                try {
                    CursorDB.moveToFirst();
                    if (CursorDB.getCount() == 0) {
                        //Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
                        Intent actMainLogin = new Intent(actGetHelp.this, actMainLogin.class);
                        startActivity(actMainLogin);
                    }
                    else
                    {
                        //Instancia del ListView
                        ListaPolizas = (ListView) alertDialogPolicy.findViewById(R.id.ListViewPolizas);

                        //Obtenemos la instancia única de la cola de peticiones
                        requestQueue = RequestQueueSingleton.getRequestQueue(actGetHelp.this);

                        //Inicializar el adaptador con la fuente de datos
                        List<clsPoliza> Polizas = new ArrayList<clsPoliza>();
                        AdaptadorPolizas = new clsPolizaArrayAdapter(actGetHelp.this, Polizas);
                        AdaptadorPolizas.clear();

                        //URL del detalle del proveedor
                        String url = "http://app.towpod.net/ws_get_customer_policy.php?idc=" + CursorDB.getString(0);
                        url = url.replaceAll(" ", "%20");

                        JsonArrayRequest requestPoliza = new JsonArrayRequest(url,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray responsePolizas) {
                                        for (int i = 0; i < responsePolizas.length(); i++){
                                            try
                                            {
                                                responsePolizas.getJSONObject(i).getString("car_id");
                                                responsePolizas.getJSONObject(i).getString("car_brand");
                                                responsePolizas.getJSONObject(i).getString("car_model");
                                                responsePolizas.getJSONObject(i).getInt("car_year");
                                                responsePolizas.getJSONObject(i).getString("policy_number");
                                                responsePolizas.getJSONObject(i).getString("policy_type_name");

                                                AdaptadorPolizas.add(new clsPoliza(
                                                        responsePolizas.getJSONObject(i).getString("car_id"),
                                                        responsePolizas.getJSONObject(i).getString("car_brand"),
                                                        responsePolizas.getJSONObject(i).getString("car_model"),
                                                        responsePolizas.getJSONObject(i).getInt("car_year"),
                                                        responsePolizas.getJSONObject(i).getString("policy_number"),
                                                        responsePolizas.getJSONObject(i).getString("policy_type_name"),
                                                        responsePolizas.getJSONObject(i).getString("policy_type_description"),
                                                        responsePolizas.getJSONObject(i).getString("card_number"),
                                                        responsePolizas.getJSONObject(i).getDouble("policy_value"),
                                                        responsePolizas.getJSONObject(i).getString("bank_name")));


                                                /*RadioButton opcionPoliza = new RadioButton(actGetHelp.this);
                                                LinearLayout.LayoutParams params = new RadioGroup.LayoutParams(
                                                        RadioGroup.LayoutParams.WRAP_CONTENT,
                                                        RadioGroup.LayoutParams.WRAP_CONTENT);

                                                opcionPoliza.setLayoutParams(params);
                                                opcionPoliza.setPadding(0, 0, 0, 15);
                                                opcionPoliza.setId(i);
                                                opcionPoliza.setText(responsePolizas.getJSONObject(i).getString("policy_type_name")+"\n"+responsePolizas.getJSONObject(i).getString("car_brand")+", "+responsePolizas.getJSONObject(i).getString("car_model")+", "+responsePolizas.getJSONObject(i).getString("car_year")+ "\n["+responsePolizas.getJSONObject(i).getString("car_id")+"]");
                                                opcionPoliza.setTag(responsePolizas.getJSONObject(i).getString("car_id"));

                                                grupoPolizas.addView(opcionPoliza);

                                                if (i == 0){opcionPoliza.setChecked(true);}*/

                                            }
                                            catch (JSONException e)
                                            {
                                                //Toast popUpError = Toast.makeText(actProviderData.this, e.toString() , Toast.LENGTH_LONG);
                                                //popUpError.show();
                                            }
                                        }


                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        //Si recibimos un error, mostraremos la causa
                                        //Toast popUpError = Toast.makeText(actProviderData.this, error.getMessage(), Toast.LENGTH_LONG);
                                        //popUpError.show();
                                    }
                                }
                        );

                        //Le ponemos un tag que servirá para identificarla si la queremos cancelar
                        requestPoliza.setTag(JSON_REQUEST);
                        //Al ponerle la política de reintento, le hacemos casting, ya que el metodo recibe un objeto de la clase padre
                        requestPoliza = (JsonArrayRequest) setRetryPolicy(requestPoliza);
                        //Iniciamos la petición añadiéndola a la cola
                        requestQueue.add(requestPoliza);

                        //Inicializar el adaptador con la fuente de datos
                        AdaptadorPolizas = new clsPolizaArrayAdapter(actGetHelp.this, Polizas);

                        //Relacionando la lista con el adaptador
                        ListaPolizas.setAdapter(AdaptadorPolizas);

                        //Creo el evento Clic para cada objeto de la lista
                        ListaPolizas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {
                                final clsPoliza polizaSeleccionada = (clsPoliza) pariente.getItemAtPosition(posicion);
                                alertDialogPolicy.dismiss();

                                strPolicyNumber = polizaSeleccionada.get_policy_number();
                                strCarID = polizaSeleccionada.get_car_id();
                                strCardNumber = polizaSeleccionada.get_policy_card_number();
                                dblPrecioServicio = polizaSeleccionada.get_policy_value();
                                intPaymentMethod = 1;

                                //Realizo el proceo en segundo plano y muestro el cuadro de espera
                                ProgressDialog VentanaEspera = new ProgressDialog(actGetHelp.this);
                                VentanaEspera.setTitle("Enviando solicitud");
                                VentanaEspera.setMessage("Espere un momento, por favor...");
                                new actGetHelp.SolicitarAsistenciaVialEnSegundoPlano(VentanaEspera, actGetHelp.this).execute();

                                //EnviarSolicitudAsistencia(intIdTipoAsistencia, strComentario, polizaSeleccionada.get_policy_number(), strLatitud, strLongitud, polizaSeleccionada.get_car_id(), polizaSeleccionada.get_policy_card_number(), (double) 0);
                            }

                        });
                    }
                } catch (Exception e) {
                    Toast popUpError = Toast.makeText(actGetHelp.this, e.getMessage(), Toast.LENGTH_LONG);
                    popUpError.show();
                }

                //Cerramos la base de datos
                db.close();

            }
        } catch (Exception e) {
            return;
        }


        Button theButton = alertDialogPolicy.getButton(DialogInterface.BUTTON_POSITIVE);
        //theButton.setOnClickListener(new actPolicyType.CustomListenerAutos(alertDialogCard));

    }
    
    public void DialogMostrarUbicacionNoEstablecida()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(actGetHelp.this);
        LayoutInflater inflater = actGetHelp.this.getLayoutInflater();
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
    

    public void DialogoMostrarTarjetasDisponibles()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(actGetHelp.this);
        LayoutInflater inflater = actGetHelp.this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_select_my_card, null));

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {

            }
        });

        /*builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {

            }
        });*/

        final AlertDialog alertDialogCard = builder.create();
        alertDialogCard.show();

        try {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actGetHelp.this, "USUARIOSHM", null, 2);
            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Si hemos abierto correctamente la base de datos
            if (db != null) {
                String[] campos = new String[]{"user_id", "user_email", "user_password", "user_keep_session"};
                final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

                try {
                    CursorDB.moveToFirst();
                    if (CursorDB.getCount() == 0) {
                        //Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
                        Intent actMainLogin = new Intent(actGetHelp.this, actMainLogin.class);
                        startActivity(actMainLogin);
                        return;

                    }
                    else
                    {
                        //==========================================================================================
                        //=======CARGA DE Tarjetas DE LOS USUARIOS===============================================
                        //==========================================================================================

                        //Instancia del ListView
                        ListaTarjetas = (ListView) alertDialogCard.findViewById(R.id.ListViewTarjetas);

                        //Obtenemos la instancia única de la cola de peticiones
                        requestQueue = RequestQueueSingleton.getRequestQueue(actGetHelp.this);

                        //Inicializar el adaptador con la fuente de datos
                        List<clsTarjeta> Tarjetas = new ArrayList<clsTarjeta>();
                        AdaptadorTarjetas = new clsTarjetaArrayAdapter(actGetHelp.this, Tarjetas);
                        AdaptadorTarjetas.clear();

                        //URL del detalle del proveedor
                        String urlTarjeta = "http://app.towpod.net/ws_get_customer_card.php?idc=" + CursorDB.getString(0);
                        urlTarjeta = urlTarjeta.replaceAll(" ", "%20");

                        JsonArrayRequest requestTarjeta = new JsonArrayRequest(urlTarjeta,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray responseTarjeta) {
                                        if (responseTarjeta.length() > 0)
                                        {
                                            for (int i = 0; i < responseTarjeta.length(); i++){
                                                try
                                                {
                                                    AdaptadorTarjetas.add(new clsTarjeta(
                                                            responseTarjeta.getJSONObject(i).getString("card_number"),
                                                            responseTarjeta.getJSONObject(i).getString("card_name"),
                                                            responseTarjeta.getJSONObject(i).getString("card_valid"),
                                                            responseTarjeta.getJSONObject(i).getString("card_code")));
                                                }
                                                catch (JSONException e)
                                                {
                                                    //Toast popUpError = Toast.makeText(actProviderData.this, e.toString() , Toast.LENGTH_LONG);
                                                    //popUpError.show();
                                                }
                                            }
                                        }
                                        else
                                        {
                                            LinearLayout loNoCard = (LinearLayout) alertDialogCard.findViewById(R.id.loNoCard);
                                            //loNoCard.setVisibility(View.VISIBLE);
                                            //RelativeLayout loNoSignal = (RelativeLayout) findViewById(R.id.loNoSignal);
                                            LinearLayout.LayoutParams medidasLAYOUT = (LinearLayout.LayoutParams) loNoCard.getLayoutParams();
                                            medidasLAYOUT.height = medidasLAYOUT.MATCH_PARENT;
                                            loNoCard.setLayoutParams(medidasLAYOUT);

                                            //alertDialogCard.dismiss();
                                            Toast popUp = Toast.makeText(actGetHelp.this, "No tienes tarjetas registradas.", Toast.LENGTH_LONG);
                                            //popUp.show();
                                        }

                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        //Si recibimos un error, mostraremos la causa
                                        //Toast popUpError = Toast.makeText(actProviderData.this, error.getMessage(), Toast.LENGTH_LONG);
                                        //popUpError.show();
                                    }
                                }
                        );

                        //Le ponemos un tag que servirá para identificarla si la queremos cancelar
                        requestTarjeta.setTag(JSON_REQUEST);
                        //Al ponerle la política de reintento, le hacemos casting, ya que el metodo recibe un objeto de la clase padre
                        requestTarjeta = (JsonArrayRequest) setRetryPolicy(requestTarjeta);
                        //Iniciamos la petición añadiéndola a la cola
                        requestQueue.add(requestTarjeta);

                        //Inicializar el adaptador con la fuente de datos
                        AdaptadorTarjetas = new clsTarjetaArrayAdapter(actGetHelp.this, Tarjetas);

                        //Relacionando la lista con el adaptador
                        ListaTarjetas.setAdapter(AdaptadorTarjetas);

                        //Creo el evento Clic para cada objeto de la lista
                        ListaTarjetas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {
                                final clsTarjeta tarjetaSeleccionada = (clsTarjeta) pariente.getItemAtPosition(posicion);
                                strCardNumber = tarjetaSeleccionada.get_card_number();
                                alertDialogCard.dismiss();
                                intPaymentMethod = 2;
                                DialogoMostrarAutosDisponibles(SendCarTo.ProblemDescription);
                            }

                        });

                        //==========================================================================================
                        //=======FIN DE CARGA DE Tarjetas DE LOS USUARIOS========================================
                        //==========================================================================================
                    }
                } catch (Exception e) {
                    Toast popUpError = Toast.makeText(actGetHelp.this, e.getMessage(), Toast.LENGTH_LONG);
                    popUpError.show();
                }

                //Cerramos la base de datos
                db.close();

            }
        } catch (Exception e) {
            return;
        }

        Button theButton = alertDialogCard.getButton(DialogInterface.BUTTON_POSITIVE);
        //theButton.setOnClickListener(new actPolicyType.CustomListenerAutos(alertDialogCard));

    }
    
    public void SolicitarDescripcionProblema()
    {
	    Log.i("TP_TAG", "Descripción del problema inside");
        AlertDialog.Builder builder = new AlertDialog.Builder(actGetHelp.this);
        LayoutInflater inflater = actGetHelp.this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_get_help_comment, null));

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Nada para hacer aquí
            }
        });

        builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //El código se movió a la clase CustomListener
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setTitle("¿Tienes algún comentario?");
        alertDialog.show();

        Button PositiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
	    PositiveButton.setOnClickListener(new actGetHelp.validateHelpComment(alertDialog));

    }

	class validateHelpComment implements View.OnClickListener
	{
		private final Dialog dialog;

		public validateHelpComment(Dialog MyDialog)
		{
			this.dialog = MyDialog;
		}

		@Override
		public void onClick(View v)
		{
			final EditText txtMyComment = (EditText) dialog.findViewById(R.id.txtMyComment);
			if (CampoVacio(txtMyComment)==true)
			{
				txtMyComment.requestFocus();
				InputMethodManager IMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				IMM.showSoftInput(txtMyComment, InputMethodManager.SHOW_IMPLICIT);

				Toast.makeText(
						actGetHelp.this,
						"Debes escribir una descripción de tu problema.",
						Toast.LENGTH_LONG)
						.show();
				return;
			}
			else
			{
				dialog.dismiss();

				strComentario = txtMyComment.getText().toString().trim();

				//Realizo el proceo en segundo plano y muestro el cuadro de espera
				ProgressDialog VentanaEspera = new ProgressDialog(actGetHelp.this);
				VentanaEspera.setTitle("Enviando datos");
				VentanaEspera.setMessage("Espere un momento, por favor...");
				new actGetHelp.SolicitarAsistenciaVialEnSegundoPlano(VentanaEspera, actGetHelp.this).execute();

			}

		}
	}

	private class SolicitarAsistenciaVialEnSegundoPlano extends AsyncTask<Void, Void, Void>
	{

		ProgressDialog progress;
		actGetHelp act;

		public SolicitarAsistenciaVialEnSegundoPlano(ProgressDialog progress, actGetHelp act) {
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
					AlertDialog.Builder builder = new AlertDialog.Builder(actGetHelp.this);
					LayoutInflater inflater = actGetHelp.this.getLayoutInflater();
					builder.setView(inflater.inflate(R.layout.dialog_assistance_result, null));

					builder.setNegativeButton("Ver historial", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id)
						{
							Intent actMyAccountHistory = new Intent(actGetHelp.this, actMyAccountHistory.class);
							startActivity(actMyAccountHistory);
						}
					});

					builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id)
						{
							actGetHelp.super.finish();

						}
					});

					AlertDialog alertDialogConfirmacion = builder.create();
					alertDialogConfirmacion.show();

					TextView lblEmail = (TextView) alertDialogConfirmacion.findViewById(R.id.lblCustomerEmail);
					lblEmail.setText(strCustomerEmail);

				}
			}, 2000);



		}

		protected Void doInBackground(Void... params) {
			//realizar la operación aquí
			EnviarSolicitudAsistencia(intIdTipoAsistencia, strComentario, strPolicyNumber, strLatitud, strLongitud, strCarID, strCardNumber, dblPrecioServicio, intIdProveedor, intPaymentMethod);
			return null;
		}

	}

	public void EnviarSolicitudAsistencia(Integer prmIDAssist, String prmComment, String prmPolicyNumber, String prmLat, String prmLon, String prmCarID, String prmCardNumber, Double prmServiceValue, Integer prmIdProveedor, Integer intPaymentMethod)
	{
		Calendar MyCalendar = Calendar.getInstance();
		//Verifico si tiene polizas registradas
		try {
			//Abrimos la base de datos 'DBUsuarios' en modo escritura
			cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(this, "USUARIOSHM", null, 2);
			SQLiteDatabase db = usdbh.getWritableDatabase();

			//Si hemos abierto correctamente la base de datos
			if (db != null) {
				String[] campos = new String[]{"user_id, user_email", "user_password", "user_keep_session"};
				final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

				try {
					CursorDB.moveToFirst();
					if (CursorDB.getCount() == 0)
					{
						//Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
						Intent actMainLogin = new Intent(this, actMainLogin.class);
						startActivity(actMainLogin);
						return;
					}
					else
					{
						//OBTENGO LA FECHA Y LA HORA ACTUAL:
						//ejemplo: 2017-03-20 16:19:50
						String myCurrentDateTime;

						myCurrentDateTime = String.valueOf(MyCalendar.get(Calendar.YEAR)+"-"+String.valueOf(MyCalendar.get(Calendar.MONTH)+1)+"-"+String.valueOf(MyCalendar.get(Calendar.DAY_OF_MONTH)+" "+String.valueOf(MyCalendar.get(Calendar.HOUR_OF_DAY)))+":"+String.valueOf(MyCalendar.get(Calendar.MINUTE))+":"+String.valueOf(MyCalendar.get(Calendar.SECOND)));

						//URL para guardar datos
						String url = "http://app.towpod.net/ws_add_assistance_request.php?" +
								"usr_id="+CursorDB.getInt(0) +
								"&dreq="+myCurrentDateTime+
								"&id_assist="+ prmIDAssist +
								"&comment="+prmComment+
								"&p_no="+prmPolicyNumber +
								"&lat="+prmLat +
								"&lon="+prmLon+
								"&destination_lat=0"+
								"&destination_lon=0"+
								"&car_id="+prmCarID+
								"&c_num="+prmCardNumber+
								"&s_val="+prmServiceValue+
								"&pid="+prmIdProveedor+
								"&paym="+intPaymentMethod;

						url = url.replaceAll(" ", "%20");

						strFinalURL = url;

						JsonArrayRequest request = new JsonArrayRequest(url,
								new Response.Listener<JSONArray>() {
									@Override
									public void onResponse(JSONArray response)
									{
										//si marcha bien

									}
								},
								new Response.ErrorListener() {
									@Override
									public void onErrorResponse(VolleyError error) {

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



					}
				} catch (Exception e) {
					Toast popUpError = Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
					popUpError.show();
				}

				//Cerramos la base de datos
				db.close();

			}
		} catch (Exception e) {
			return;
		}
	}

	public void SolicitarDescripcionRepuesto()
	{

		AlertDialog.Builder builder = new AlertDialog.Builder(actGetHelp.this);
		LayoutInflater inflater = actGetHelp.this.getLayoutInflater();
		builder.setView(inflater.inflate(R.layout.dialog_get_part_description, null));

		builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				//Nada para hacer aquí
			}
		});

		builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				//El código se movió a la clase CustomListener
			}
		});

		AlertDialog alertDialog = builder.create();
		alertDialog.setTitle("Búsqueda de repuestos");
		alertDialog.show();

		Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
		theButton.setOnClickListener(new actGetHelp.validatePartDescription(alertDialog));

	}

	class validatePartDescription implements View.OnClickListener
	{
		private final Dialog dialog;

		public validatePartDescription(Dialog MyDialog)
		{
			this.dialog = MyDialog;
		}

		@Override
		public void onClick(View v)
		{
			final EditText txtPartDescription = (EditText) dialog.findViewById(R.id.txtPartDescription);
			if (CampoVacio(txtPartDescription)==true)
			{
				txtPartDescription.requestFocus();
				InputMethodManager IMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				IMM.showSoftInput(txtPartDescription, InputMethodManager.SHOW_IMPLICIT);

				Toast.makeText(
						actGetHelp.this,
						"Debes escribir una descripción del repuesto que necesitas.",
						Toast.LENGTH_LONG)
						.show();
				return;
			}
			else
			{
				dialog.dismiss();

				strPartDescription = txtPartDescription.getText().toString().trim();

				//Realizo el proceo en segundo plano y muestro el cuadro de espera
				ProgressDialog VentanaEspera = new ProgressDialog(actGetHelp.this);
				VentanaEspera.setTitle("Enviando datos");
				VentanaEspera.setMessage("Espere un momento, por favor...");
				new actGetHelp.setPartRequestOnBackground(VentanaEspera, actGetHelp.this).execute();

			}

		}
	}

	private class setPartRequestOnBackground extends AsyncTask<Void, Void, Void>
	{

		ProgressDialog progress;
		actGetHelp act;

		public setPartRequestOnBackground(ProgressDialog progress, actGetHelp act) {
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
					AlertDialog.Builder builder = new AlertDialog.Builder(actGetHelp.this);
					LayoutInflater inflater = actGetHelp.this.getLayoutInflater();
					builder.setView(inflater.inflate(R.layout.dialog_part_request_result, null));

					builder.setNegativeButton("Ver historial", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id)
						{
							Intent actMyAccountHistory = new Intent(actGetHelp.this, actMyAccountHistory.class);
							startActivity(actMyAccountHistory);
						}
					});

					builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id)
						{
							actGetHelp.super.finish();
						}
					});

					AlertDialog alertDialogConfirmacion = builder.create();
					alertDialogConfirmacion.show();

					TextView lblEmail = (TextView) alertDialogConfirmacion.findViewById(R.id.lblCustomerEmail);
					lblEmail.setText(strCustomerEmail);

				}
			}, 2000);



		}

		protected Void doInBackground(Void... params) {
			//realizar la operación aquí
			Calendar MyCalendar = Calendar.getInstance();
			//Verifico si tiene polizas registradas
			try {
				//Abrimos la base de datos 'DBUsuarios' en modo escritura
				cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actGetHelp.this, "USUARIOSHM", null, 2);
				SQLiteDatabase db = usdbh.getWritableDatabase();

				//Si hemos abierto correctamente la base de datos
				if (db != null) {
					String[] campos = new String[]{"user_id, user_email", "user_password", "user_keep_session"};
					final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

					try {
						CursorDB.moveToFirst();
						//OBTENGO LA FECHA Y LA HORA ACTUAL:
						//ejemplo: 2017-03-20 16:19:50
						String myCurrentDateTime;

						myCurrentDateTime = String.valueOf(MyCalendar.get(Calendar.YEAR)+"-"+String.valueOf(MyCalendar.get(Calendar.MONTH)+1)+"-"+String.valueOf(MyCalendar.get(Calendar.DAY_OF_MONTH)+" "+String.valueOf(MyCalendar.get(Calendar.HOUR_OF_DAY)))+":"+String.valueOf(MyCalendar.get(Calendar.MINUTE))+":"+String.valueOf(MyCalendar.get(Calendar.SECOND)));

						//URL para guardar datos
						String url = "http://app.towpod.net/ws_add_part_request.php?" +
								"usr_id="+CursorDB.getInt(0) +
								"&car_id="+ strCarID +
								"&lat="+currentLat+
								"&lon="+currentLong+
								"&description="+strPartDescription+
								"&time="+myCurrentDateTime;

						url = url.replaceAll(" ", "%20");

						//strFinalURL = url;
						Log.i("TP_TAG", url);

						JsonArrayRequest request = new JsonArrayRequest(url,
								new Response.Listener<JSONArray>() {
									@Override
									public void onResponse(JSONArray response)
									{
										//si marcha bien

									}
								},
								new Response.ErrorListener() {
									@Override
									public void onErrorResponse(VolleyError error) {

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
					} catch (Exception e) {
						Toast popUpError = Toast.makeText(actGetHelp.this, e.getMessage(), Toast.LENGTH_LONG);
						popUpError.show();
					}

					//Cerramos la base de datos
					db.close();

				}
			} catch (Exception e) {
				Toast popUpError = Toast.makeText(actGetHelp.this, e.getMessage(), Toast.LENGTH_LONG);
				//popUpError.show();
			}
			return null;
		}

	}

    private boolean CampoVacio(EditText myeditText)
    {
        return myeditText.getText().toString().trim().length() == 0;
    }

    private boolean CadenaVacia(String MyString)
    {
        return MyString.toString().trim().length() == 0;
    }

    //Le pone una política de 4 reintentos con 4 segundos entre cada uno y la devuelve.
    public Request setRetryPolicy(Request request)
    {
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }

    public void CalcularDistanciaReal(Double prmLatitudProveedor, Double prmLongitudProveedor, String prmLatitudCliente, String prmLongitudCliente)
    {

        //====================================================================================================================
        //Recupero el precio en base a la zona de cobertura y a la distancia real en carretera. NO la distancia radial

        requestQueueFixedDistance = RequestQueueSingleton.getRequestQueue(actGetHelp.this);

        //String urlFixed = "http://app.towpod.net/ws_get_assistance_closer_provider_value.php?aaid=1&id_ta=5&lat=14.1389425&lon=-87.2814149&use_policy=0&pid=27";
        String urlFixedDistance = "http://maps.googleapis.com/maps/api/distancematrix/json?origins="+prmLatitudProveedor+","+prmLongitudProveedor+"&destinations="+strLatitud+","+strLongitud+"&mode=driving&tolls=true";
        urlFixedDistance = urlFixedDistance.replaceAll(" ", "%20");

        JsonObjectRequest requestFixedDistance = new JsonObjectRequest(urlFixedDistance,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject responseFixedDistance)
                    {
                        try
                        {
                            if (responseFixedDistance.length() > 0)
                            {
                                Toast popUp = Toast.makeText(actGetHelp.this, "Total: " + String.valueOf(responseFixedDistance.length()), Toast.LENGTH_SHORT);
                                //popUp.show();

                                responseFixedDistance.getJSONArray("rows");

                                Toast popUpError = Toast.makeText(actGetHelp.this, responseFixedDistance.getJSONArray("rows").toString() , Toast.LENGTH_SHORT);
                                //popUpError.show();

                                //==================================================================
                                JSONArray JSON_ARRAY_ROWS = responseFixedDistance.getJSONArray("rows"); //Accedo al arreglo ROWS
                                Toast popup_1 = Toast.makeText(actGetHelp.this, "ROWS: \n" + String.valueOf(JSON_ARRAY_ROWS.length()), Toast.LENGTH_SHORT);
                                //popup_1.show();

                                JSONObject JSON_OBJECT_ROWS = JSON_ARRAY_ROWS.getJSONObject(0); //Accedo al objeto ELEMENTS del arreglo ROWS
                                Toast popup_2 = Toast.makeText(actGetHelp.this, "ELEMENTS: \n" + String.valueOf(JSON_OBJECT_ROWS.length()), Toast.LENGTH_SHORT);
                                //popup_2.show();


                                JSONArray JSON_ARRAY_ELEMENTS = JSON_OBJECT_ROWS.getJSONArray("elements");
                                Toast popup_3 = Toast.makeText(actGetHelp.this, String.valueOf(JSON_ARRAY_ELEMENTS.length()), Toast.LENGTH_SHORT);
                                //popup_3.show();

                                JSONObject JSON_OBJECT_ELEMENTS = JSON_ARRAY_ELEMENTS.getJSONObject(0); //Elements
                                Toast popup_4 = Toast.makeText(actGetHelp.this, "ELEMENTS: \n" + String.valueOf(JSON_OBJECT_ELEMENTS.length()), Toast.LENGTH_SHORT);
                                //popup_4.show();


                                JSONObject JSON_OBJECT_DISTANCE = JSON_ARRAY_ELEMENTS.getJSONObject(0); //Elements
                                Toast popup_5 = Toast.makeText(actGetHelp.this, JSON_OBJECT_DISTANCE.getString("distance"), Toast.LENGTH_SHORT);
                                //popup_5.show();

                                JSONObject JSON_ARRAY_DISTANCE_VALUE = JSON_OBJECT_DISTANCE.getJSONObject("distance");
                                Toast popup_6 = Toast.makeText(actGetHelp.this, JSON_ARRAY_DISTANCE_VALUE.getString("value"), Toast.LENGTH_SHORT);
                                //popup_6.show();

                                DistanciaReal = Double.parseDouble(JSON_ARRAY_DISTANCE_VALUE.getString("value"))/1000.00;
                                //==================================================================

                            }
                        }
                        catch (JSONException e)
                        {
                            Toast popUpError = Toast.makeText(actGetHelp.this, e.getMessage() , Toast.LENGTH_LONG);
                            popUpError.show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Toast popUpError = Toast.makeText(actGetHelp.this, error.getMessage() , Toast.LENGTH_LONG);
                        popUpError.show();
                    }
                }
        );

        requestFixedDistance.setTag(JSON_REQUEST);
        requestFixedDistance = (JsonObjectRequest) setRetryPolicy(requestFixedDistance);
        requestQueueFixedDistance.add(requestFixedDistance);

        //====================================================================================================================
    }

    public void EnviarSolicitudSinPoliza (View view)
    {
        //Realizo el proceo en segundo plano y muestro el cuadro de espera
        ProgressDialog VentanaEspera = new ProgressDialog(this);
        VentanaEspera.setTitle("Enviando ubicación");
        VentanaEspera.setMessage("Espere un momento, por favor...");
        new actGetHelp.SolicitarAsistenciaVialEnSegundoPlano(VentanaEspera, this).execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {

        mMap = googleMap;

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
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

            longitudeValueBest.setText(longitudeBest + "");
            latitudeValueBest.setText(latitudeBest + "");

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
        //markerOptions.snippet("--");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.markerdestination));



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


        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Cordenadas, 16.0f));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);

    }

    //==============================================================================================
    //FIN DE LOCALIZACION===========================================================================
    //==============================================================================================

	public class SendCarTo
	{
		public static final int ProblemDescription = 1;
		public static final int PartDescription = 2;
	}
}
