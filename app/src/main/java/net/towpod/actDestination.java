package net.towpod;

import android.*;
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
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class actDestination extends AppCompatActivity  implements OnMapReadyCallback {

    Double currentLat = 0.0;
    Double currentLong = 0.0;

    Double DestinationLat = 0.0;
    Double DestinationLong = 0.0;

    private GoogleMap mMap;
    
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
    public Double TotalDistance = 0.0;
    public Double distanceUserToDestiny = 0.0;

    public Integer intDestinationRequired;

    public String strCardNumber = "";

    public String strCarID = "";
    

    public String[] providerPhone = new String[3];

    public Integer nPhones;
    public String strNumberToDial = "";
    Double fixed_service_value = 0.0;

    Double DistanciaReal = 0.0;

    public String strFinalURL = "";

    public Integer intDriverRequired;

    public static String extraCurrentLat = "extraCurrentLat";
    public static String extraCurrentLong = "extraCurrentLong";
    public static String extraNombreAsistencia = "extraNombreAsistencia";
    public static String extraIdAsistencia = "extraIdAsistencia";
    public static String extraDriver = "extraDriver";

    public String strAreaCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_destination);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();

        setTitle(intent.getStringExtra(actDestination.extraNombreAsistencia));
	    setTitle("Ubicación de destino");

        currentLat = 0.0 + Double.parseDouble(intent.getStringExtra(actDestination.extraCurrentLat).toString());
        currentLong = 0.0 + Double.parseDouble(intent.getStringExtra(actDestination.extraCurrentLong).toString());

        DestinationLat = currentLat;
        DestinationLong = currentLong;

        intDriverRequired = Integer.parseInt(intent.getStringExtra(actDestination.extraDriver).toString());

        intIdTipoAsistencia = Integer.parseInt(intent.getStringExtra(actDestination.extraIdAsistencia).toString());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // Add a marker in Sydney and move the camera
        LatLng Cordenadas = new LatLng(currentLat, currentLong);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(Cordenadas);
        markerOptions.title("Destino");
        markerOptions.snippet("--");

        //Marker locationMarker = mMap.addMarker(markerOptions);
        //locationMarker.setDraggable(true);


        //mMap.addMarker(markerOptions);

        //locationMarker.showInfoWindow();

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Cordenadas, 15.0f));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);


        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener()
        {
            @Override
            public void onCameraChange(CameraPosition cameraPosition)
            {
                DestinationLat = cameraPosition.target.latitude;
                DestinationLong = cameraPosition.target.longitude;

                //Log.i("centerLat", String.valueOf(cameraPosition.target.latitude)); Log.i("centerLong", String.valueOf(cameraPosition.target.longitude));
                Toast popUpError = Toast.makeText(actDestination.this, String.valueOf(cameraPosition.target.latitude)+", "+ String.valueOf(cameraPosition.target.longitude), Toast.LENGTH_SHORT);
                //popUpError.show();
            }
        });
    }


    public void getBestProviders(View view)
    {
        //========================NO REQUIERE DESTINO=============================
        VerificarExistenciaDePolizas();//Verifico si hay polizas

        //===========================================================================================================
        //Verifico si hay proveedores cercanos para el tipo de asistencia cercanos a la ubicación y si trabajan con poliza o no
        //String url = "http://app.towpod.net/ws_get_assistance_value.php?id_ta="+intIdTipoAsistencia+"&lat="+strLatitud+"&lon="+strLongitud+"&use_policy="+intHayPolizas;

        final String url = "http://app.towpod.net/ws_get_assistance_closer_provider_destination.php?id_ta="+intIdTipoAsistencia+"&current_lat="+currentLat+"&current_lon="+currentLong+"&use_policy="+intHayPolizas+"&destination_lat="+DestinationLat+"&destination_lon="+DestinationLong;

        //url = url.replaceAll(" ", "%20");
        Log.i("TP_TAG", url);

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


                                    AlertDialog.Builder builder = new AlertDialog.Builder(actDestination.this);
                                    LayoutInflater inflater = actDestination.this.getLayoutInflater();
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
                                    List<clsProveedorCercanoDestino> Proveedores = new ArrayList<clsProveedorCercanoDestino>();
                                    AdaptadorProveedores = new clsProveedorCercanoDestinoArrayAdapter(actDestination.this, Proveedores);
                                    AdaptadorProveedores.clear();


                                    for (int i = 0; i < response.length(); i++)
                                    {
                                        try
                                        {
                                            //====================================================================================================================
                                            //Recupero el precio en base a la zona de cobertura

                                            //txtDescripcion.setText(response.getJSONObject(0).getString("category_description"));
                                            AdaptadorProveedores.add(new clsProveedorCercanoDestino(
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
                                                    response.getJSONObject(i).getDouble("total_service_value"),
                                                    response.getJSONObject(i).getDouble("total_distance"),
                                                    response.getJSONObject(i).getDouble("user_to_destiny"),
                                                    response.getJSONObject(i).getString("provider_car_brand"),
                                                    response.getJSONObject(i).getString("provider_car_model"),
                                                    response.getJSONObject(i).getInt("provider_car_year"),
                                                    response.getJSONObject(i).getString("provider_car_color"),
                                                    response.getJSONObject(i).getString("provider_car_id")));

	                                        strAreaCode = response.getJSONObject(0).getString("country_area_code");


                                            //====================================================================================================================

                                        } catch (JSONException e)
                                        {
                                            Toast popUpError = Toast.makeText(actDestination.this, e.getMessage() , Toast.LENGTH_LONG);
                                            popUpError.show();
                                        }
                                    }


                                    //Inicializar el adaptador con la fuente de datos
                                    AdaptadorProveedores = new clsProveedorCercanoDestinoArrayAdapter(actDestination.this, Proveedores);

                                    //Relacionando la lista con el adaptador
                                    ListaProveedores.setAdapter(AdaptadorProveedores);

                                    //Creo el evento Clic para cada objeto de la lista
                                    ListaProveedores.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id)
                                        {
                                            clsProveedorCercanoDestino itemSeleccionado = (clsProveedorCercanoDestino) pariente.getItemAtPosition(posicion);

                                            intIdProveedor = Integer.parseInt(itemSeleccionado.get_id_proveedor());
                                            strComentario = "";
                                            dblPrecioServicio = 0.0;
                                            intPaymentMethod = 2;
                                            dblPrecioServicio = itemSeleccionado.get_service_value_fixed();

                                            providerPhone[0] = itemSeleccionado.get_Phone_1();
                                            providerPhone[1] = itemSeleccionado.get_Phone_2();
                                            providerPhone[2] = itemSeleccionado.get_Phone_3();

                                            TotalDistance = itemSeleccionado.get_distancia();
                                            distanceUserToDestiny = itemSeleccionado.getDistanceUserToDestiny();

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

                                            AlertDialog.Builder builder = new AlertDialog.Builder(actDestination.this);
                                            LayoutInflater inflater = actDestination.this.getLayoutInflater();
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
                                            theButton.setOnClickListener(new actDestination.CustomListener(alertDialog));
                                            */

                                }


                            }
                            catch (JSONException e)
                            {
                                Toast popUpError = Toast.makeText(actDestination.this, e.toString() , Toast.LENGTH_LONG);
                                //popUpError.show();
                            }
                        }
                        else
                        {

                            //Si no hay proveedores cercanos
                            AlertDialog.Builder builder = new AlertDialog.Builder(actDestination.this);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.setTitle("Lo sentimos");
                            alertDialog.setMessage("En este momenton no encontramos un servicio disponible.");
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
                        Toast popUpError = Toast.makeText(actDestination.this, error.getMessage() , Toast.LENGTH_LONG);
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
                        actDestination.this.finish();
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
                                                //Intent actGetHelpWithPolicy = new Intent(actDestination.this, actDestination.class);
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
            Toast popUpError = Toast.makeText(actDestination.this, "El número telefónico no está disponible.", Toast.LENGTH_SHORT);
            popUpError.show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(actDestination.this);

        if (intDriverRequired == 0)
        {
            LayoutInflater inflater = actDestination.this.getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.dialog_make_call, null));
        }
        else if (intDriverRequired == 1)
        {
            LayoutInflater inflater = actDestination.this.getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.dialog_get_taxi, null));
        }


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
                Toast popUp = Toast.makeText(actDestination.this, "Has marcado el número: " + items[wich].toString(), Toast.LENGTH_SHORT);
                //popUp.show();
                strNumberToDial = items[wich].toString();
            }
        });
        */

        // Add the buttons
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast popUp = Toast.makeText(actDestination.this, "Has cancelado tu llamada", Toast.LENGTH_SHORT);
                //popUp.show();
            }
        });

        /*
        builder.setPositiveButton("Llamar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast popUp = Toast.makeText(actDestination.this, "Llamando", Toast.LENGTH_SHORT);
                //popUp.show();
                //Intent callIntent = new Intent(Intent.ACTION_DIAL); //Escribe el número telefónico pero no lo marca
                Intent callIntent = new Intent(Intent.ACTION_CALL); //Marca el número inmediatamente
                callIntent.setData(Uri.parse("tel:" + strNumberToDial));
                if (ActivityCompat.checkSelfPermission(actDestination.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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
                    if (ActivityCompat.checkSelfPermission(actDestination.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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
            DialogoMostrarAutosDisponibles();
        }

        if(intHayPolizas == 1)
        {
            // Muestro las polizas disponibles.
            // Cada poliza está asociada a un vehiculo por lo que no necesito preguntar cual es el vehiculo que necesita asistencia. 
            DialogoMostrarPolizasDisponibles();
        }

        //Realizo el proceo en segundo plano y muestro el cuadro de espera
       /* ProgressDialog VentanaEspera = new ProgressDialog(actDestination.this);
        VentanaEspera.setTitle("Enviando datos");
        VentanaEspera.setMessage("Espere un momento, por favor...");
        new actDestination.SolicitarAsistenciaVialEnSegundoPlano(VentanaEspera, actDestination.this).execute();*/
    }

    public void EnviarUbicacionParataxiDesdeFormasDeContacto(View view)
    {
        //Cuando el usuario hace clic en el boton de Enviar ubicacion

        SolicitarDescripcionProblema();



    }

    public void DialogoMostrarAutosDisponibles()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(actDestination.this);
        LayoutInflater inflater = actDestination.this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_select_car, null));

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

        final AlertDialog alertDialogMyCar = builder.create();
        alertDialogMyCar.show();

        try {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actDestination.this, "USUARIOSHM", null, 2);
            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Si hemos abierto correctamente la base de datos
            if (db != null) {
                String[] campos = new String[]{"user_id", "user_email", "user_password", "user_keep_session"};
                final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

                try {
                    CursorDB.moveToFirst();
                    if (CursorDB.getCount() == 0) {
                        //Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
                        Intent actMainLogin = new Intent(actDestination.this, actMainLogin.class);
                        startActivity(actMainLogin);
                        return;

                    }
                    else
                    {
                        //Instancia del ListView
                        ListaAutos = (GridView) alertDialogMyCar.findViewById(R.id.ListViewAutos);

                        //Obtenemos la instancia única de la cola de peticiones
                        requestQueue = RequestQueueSingleton.getRequestQueue(actDestination.this);

                        //Inicializar el adaptador con la fuente de datos
                        List<clsAuto> Autos = new ArrayList<clsAuto>();
                        AdaptadorAutos = new clsAutoArrayAdapter(actDestination.this, Autos);
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
                                                    responseAuto.getJSONObject(i).getString("car_id");
                                                    responseAuto.getJSONObject(i).getString("car_brand");
                                                    responseAuto.getJSONObject(i).getString("car_model");
                                                    responseAuto.getJSONObject(i).getString("car_year");

                                                    RadioButton opcionAuto = new RadioButton(actDestination.this);
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
                        AdaptadorAutos = new clsAutoArrayAdapter(actDestination.this, Autos);

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

                                SolicitarDescripcionProblema();

                                //Realizo el proceo en segundo plano y muestro el cuadro de espera
                                /*ProgressDialog VentanaEspera = new ProgressDialog(actDestination.this);
                                VentanaEspera.setTitle("Enviando datos");
                                VentanaEspera.setMessage("Espere un momento, por favor...");
                                new actDestination.SolicitarAsistenciaVialEnSegundoPlano(VentanaEspera, actDestination.this).execute();*/


                            }

                        });

                    }
                } catch (Exception e) {
                    Toast popUpError = Toast.makeText(actDestination.this, e.getMessage(), Toast.LENGTH_LONG);
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

    public void SolicitarDescripcionProblema()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(actDestination.this);
        LayoutInflater inflater = actDestination.this.getLayoutInflater();
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
        alertDialog.setTitle("Añade un comentario");
        alertDialog.show();

        Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new actDestination.CustomListenerDescribirProblema(alertDialog));

    }

    class CustomListenerDescribirProblema implements View.OnClickListener
    {
        private final Dialog dialog;

        public CustomListenerDescribirProblema(Dialog MyDialog)
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
                        actDestination.this,
                        "Por favor añade un comentario adicional.",
                        Toast.LENGTH_LONG)
                        .show();
                return;
            }
            else
            {
                dialog.dismiss();

                strComentario = txtMyComment.getText().toString().trim();

                //Realizo el proceo en segundo plano y muestro el cuadro de espera
                ProgressDialog VentanaEspera = new ProgressDialog(actDestination.this);
                VentanaEspera.setTitle("Enviando datos");
                VentanaEspera.setMessage("Espere un momento, por favor...");
                new actDestination.SolicitarAsistenciaVialEnSegundoPlano(VentanaEspera, actDestination.this).execute();

            }

        }
    }

    public void DialogoMostrarPolizasDisponibles()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(actDestination.this);
        LayoutInflater inflater = actDestination.this.getLayoutInflater();
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

                Toast popUp = Toast.makeText(actDestination.this, "Hola", Toast.LENGTH_LONG);
                //popUp.show();
            }
        });

        try {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actDestination.this, "USUARIOSHM", null, 2);
            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Si hemos abierto correctamente la base de datos
            if (db != null) {
                String[] campos = new String[]{"user_id", "user_email", "user_password", "user_keep_session"};
                final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

                try {
                    CursorDB.moveToFirst();
                    if (CursorDB.getCount() == 0) {
                        //Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
                        Intent actMainLogin = new Intent(actDestination.this, actMainLogin.class);
                        startActivity(actMainLogin);
                    }
                    else
                    {
                        //Instancia del ListView
                        ListaPolizas = (ListView) alertDialogPolicy.findViewById(R.id.ListViewPolizas);

                        //Obtenemos la instancia única de la cola de peticiones
                        requestQueue = RequestQueueSingleton.getRequestQueue(actDestination.this);

                        //Inicializar el adaptador con la fuente de datos
                        List<clsPoliza> Polizas = new ArrayList<clsPoliza>();
                        AdaptadorPolizas = new clsPolizaArrayAdapter(actDestination.this, Polizas);
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


                                                /*RadioButton opcionPoliza = new RadioButton(actDestination.this);
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
                        AdaptadorPolizas = new clsPolizaArrayAdapter(actDestination.this, Polizas);

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
                                ProgressDialog VentanaEspera = new ProgressDialog(actDestination.this);
                                VentanaEspera.setTitle("Enviando solicitud");
                                VentanaEspera.setMessage("Espere un momento, por favor...");
                                new actDestination.SolicitarAsistenciaVialEnSegundoPlano(VentanaEspera, actDestination.this).execute();

                            }

                        });
                    }
                } catch (Exception e) {
                    Toast popUpError = Toast.makeText(actDestination.this, e.getMessage(), Toast.LENGTH_LONG);
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

    private class SolicitarAsistenciaVialEnSegundoPlano extends AsyncTask<Void, Void, Void>
    {

        ProgressDialog progress;
        actDestination act;

        public SolicitarAsistenciaVialEnSegundoPlano(ProgressDialog progress, actDestination act) {
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(actDestination.this);

                    if (intDriverRequired == 0)
                    {
                        LayoutInflater inflater = actDestination.this.getLayoutInflater();
                        builder.setView(inflater.inflate(R.layout.dialog_assistance_result, null));
                    }
                    else if (intDriverRequired == 1)
                    {
                        LayoutInflater inflater = actDestination.this.getLayoutInflater();
                        builder.setView(inflater.inflate(R.layout.dialog_get_taxi_result, null));
                    }


                    builder.setNegativeButton("Ver historial", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            Intent actMyAccountHistory = new Intent(actDestination.this, actMyAccountHistory.class);
                            startActivity(actMyAccountHistory);
                        }
                    });

                    builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            actDestination.super.finish();

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
            strLatitud = String.valueOf(currentLat);
            strLongitud = String.valueOf(currentLong);

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
                        String destination_url = "http://app.towpod.net/ws_add_assistance_request.php?" +
                                "usr_id="+CursorDB.getInt(0) +
                                "&dreq="+myCurrentDateTime+
                                "&id_assist="+ prmIDAssist +
                                "&comment="+prmComment+
                                "&p_no="+prmPolicyNumber +
                                "&lat="+prmLat+
                                "&lon="+prmLon+
                                "&destination_lat="+DestinationLat+
                                "&destination_lon="+DestinationLong+
                                "&total_distance="+distanceUserToDestiny+
                                "&car_id="+prmCarID+
                                "&c_num="+prmCardNumber+
                                "&s_val="+prmServiceValue+
                                "&pid="+prmIdProveedor+
                                "&paym="+intPaymentMethod;

                        destination_url = destination_url.replaceAll(" ", "%20");

                        strFinalURL = destination_url;

                        Toast popUpError = Toast.makeText(actDestination.this, strFinalURL, Toast.LENGTH_LONG);
                        popUpError.show();

                        JsonArrayRequest request = new JsonArrayRequest(destination_url,
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
    
    private boolean CadenaVacia(String MyString)
    {
        return MyString.toString().trim().length() == 0;
    }

    private boolean CampoVacio(EditText myeditText)
    {
        return myeditText.getText().toString().trim().length() == 0;
    }
    
    public Request setRetryPolicy(Request request)
    {
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }
}
