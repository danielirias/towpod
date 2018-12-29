package net.towpod;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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

public class actMyAccountPolicy extends AppCompatActivity {

    ListView ListaPolizas;
    ArrayAdapter AdaptadorPolizas;
    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_my_account_policy);
        //setContentView(R.layout.act_main_activity_navigation_drawer);

        //=============================================================================
        //ASIGNO EL CONTENIDO QUE DESEO MOSTRAR
        //Drawer
        //DrawerLayout Contenedor = (DrawerLayout)findViewById(R.id.drawer_layout);
        //Contenido de la Activity
        //View loContenido = getLayoutInflater().inflate(R.layout.act_service_directory, null);
        //Contenedor.addView(loContenido);
        //=============================================================================
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        setTitle("Servicios contratados");


    }

    @Override
    protected void onStart()
    {
        super.onStart();
        //Realizo el proceo en segundo plano y muestro el cuadro de espera
        ProgressDialog VentanaEspera = new ProgressDialog(this);
        VentanaEspera.setTitle("Obteniendo datos");
        VentanaEspera.setMessage("Espere un momento, por favor...");
        new actMyAccountPolicy.CargarListaEnSegundoPlano(VentanaEspera, this).execute();
    }


    private class CargarListaEnSegundoPlano extends AsyncTask<Void, Void, Void>
    {

        ProgressDialog progress;
        actMyAccountPolicy act;

        public CargarListaEnSegundoPlano(ProgressDialog progress, actMyAccountPolicy act) {
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
            CargarPolizas();
            return null;
        }

    }

    public void CargarPolizas()
    {

        try
        {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actMyAccountPolicy.this, "USUARIOSHM", null, 2);
            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Si hemos abierto correctamente la base de datos
            if(db != null)
            {
                String[] campos = new String[] {"user_email", "user_password"};
                final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

                try
                {
                    CursorDB.moveToFirst();
                    if (CursorDB.getCount() == 0)
                    {
                        //Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
                        Intent actMainLogin = new Intent(actMyAccountPolicy.this, actMainLogin.class);
                        startActivity(actMainLogin);
                        return;

                    }
                    else
                    {
                        //Si el USUARIO EXISTE LOCALMENTE voy a buscarlo al servidor web
                        //Busco la direccion de correo entre los usarios registrados
                        //Obtenemos la instancia única de la cola de peticiones
                        requestQueue = RequestQueueSingleton.getRequestQueue(this);

                        //Con esta URL verifico si se han creado registros con este usuario.
                        String urlUser = "http://app.towpod.net/ws_get_login_data_customer.php?usr="+CursorDB.getString(0)+"&pwd="+CursorDB.getString(1);;
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
                                                //==========================================================================================
                                                //=======CARGA DE POLIZAS DE LOS USUARIOS===============================================
                                                //==========================================================================================

                                                //Instancia del ListView
                                                ListaPolizas = (ListView) findViewById(R.id.ListViewPolizas);

                                                //Obtenemos la instancia única de la cola de peticiones
                                                requestQueue = RequestQueueSingleton.getRequestQueue(actMyAccountPolicy.this);

                                                //Inicializar el adaptador con la fuente de datos
                                                List<clsPoliza> Polizas = new ArrayList<clsPoliza>();
                                                AdaptadorPolizas = new clsPolizaArrayAdapter(actMyAccountPolicy.this, Polizas);
                                                AdaptadorPolizas.clear();

                                                //URL del detalle del proveedor
                                                String urlPoliza = "http://app.towpod.net/ws_get_customer_policy.php?idc=" + response.getJSONObject(0).getInt("customer_id");
                                                urlPoliza = urlPoliza.replaceAll(" ", "%20");

                                                JsonArrayRequest requestPoliza = new JsonArrayRequest(urlPoliza,
                                                        new Response.Listener<JSONArray>() {
                                                            @Override
                                                            public void onResponse(JSONArray responsePoliza) {
                                                                try {
                                                                    if (responsePoliza.length() == 0)
                                                                    {
                                                                        RelativeLayout loNoData = (RelativeLayout) findViewById(R.id.loNoData);
                                                                        LinearLayout.LayoutParams medidasLAYOUT = (LinearLayout.LayoutParams) loNoData.getLayoutParams();
                                                                        medidasLAYOUT.height = medidasLAYOUT.MATCH_PARENT;
                                                                        loNoData.setLayoutParams(medidasLAYOUT);
                                                                    }
                                                                    else
                                                                    {
                                                                        for (int i = 0; i < responsePoliza.length(); i++)
                                                                        {
                                                                            AdaptadorPolizas.add(new clsPoliza(
                                                                                    responsePoliza.getJSONObject(i).getString("car_id"),
                                                                                    responsePoliza.getJSONObject(i).getString("car_brand"),
                                                                                    responsePoliza.getJSONObject(i).getString("car_model"),
                                                                                    responsePoliza.getJSONObject(i).getInt("car_year"),
                                                                                    responsePoliza.getJSONObject(i).getString("policy_number"),
                                                                                    responsePoliza.getJSONObject(i).getString("policy_type_name"),
                                                                                    responsePoliza.getJSONObject(i).getString("policy_type_description"),
                                                                                    responsePoliza.getJSONObject(i).getString("card_number"),
                                                                                    responsePoliza.getJSONObject(i).getDouble("policy_value"),
                                                                                    responsePoliza.getJSONObject(i).getString("bank_name")));
                                                                        }

                                                                        RelativeLayout loNoData = (RelativeLayout) findViewById(R.id.loNoData);
                                                                        LinearLayout.LayoutParams medidasLAYOUT = (LinearLayout.LayoutParams) loNoData.getLayoutParams();
                                                                        medidasLAYOUT.height =0;//medidasLAYOUT.MATCH_PARENT;
                                                                        loNoData.setLayoutParams(medidasLAYOUT);

                                                                    }

                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }

                                                            }
                                                        },
                                                        new Response.ErrorListener()
                                                        {
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
                                                AdaptadorPolizas = new clsPolizaArrayAdapter(actMyAccountPolicy.this, Polizas);

                                                //Relacionando la lista con el adaptador
                                                ListaPolizas.setAdapter(AdaptadorPolizas);

                                                //Creo el evento Clic para cada objeto de la lista
                                                ListaPolizas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                                    @Override
                                                    public boolean onItemLongClick(AdapterView<?> pariente, View view, int posicion, long id) {
                                                        final clsPoliza polizaActual = (clsPoliza) pariente.getItemAtPosition(posicion);

                                                        AlertDialog.Builder builder = new AlertDialog.Builder(actMyAccountPolicy.this);
                                                        LayoutInflater inflater = actMyAccountPolicy.this.getLayoutInflater();
                                                        //builder.setView(inflater.inflate(R.layout.dialog_action_on_my_card, null));

                                                        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
                                                        {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                //Nada para hacer aquí
                                                            }
                                                        });

                                                        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener()
                                                        {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                //((actMyAccount)getActivity()).EliminarPolizaRegistrada(polizaActual.get_car_id());
                                                                EliminarPolizaRegistrada(polizaActual.get_policy_number());
                                                            }
                                                        });

                                                        AlertDialog alertDialog = builder.create();
                                                        alertDialog.setTitle("¿Deseas eliminar la póliza de seguro seleccionada?");
                                                        alertDialog.setMessage(polizaActual.get_policy_name() + "\n \nAsignada a: \n" + polizaActual.get_car_brand() + ", " + polizaActual.get_car_model() + " (" + polizaActual.get_car_year() + ")\n" + polizaActual.get_car_id());
                                                        alertDialog.show();

                                                        return false;
                                                    }

                                                });
                                                //==========================================================================================
                                                //=======FIN DE CARGA DE POLIZAS DE LOS USUARIOS========================================
                                                //==========================================================================================

                                            }
                                            catch (JSONException e) {
                                                Toast popUpError = Toast.makeText(actMyAccountPolicy.this, e.getMessage(), Toast.LENGTH_LONG);
                                                popUpError.show();
                                            }
                                        }
                                        else
                                        {
                                            //Si NO EXISTE LOCALMENTE NI EN LA WEB lo envio al Login para que inicie sesion o se registre
                                            Intent actMainLogin = new Intent(actMyAccountPolicy.this, actMainLogin.class);
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
                    Toast popUpError = Toast.makeText(actMyAccountPolicy.this,  e.getMessage(), Toast.LENGTH_LONG);
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


    public void ContratarPoliza (View view)
    {
        final View coordinatorLayoutView = findViewById(R.id.snackbarPosition);

        //1 -- Verifico si hay autos registrados

        //Abrimos la base de datos 'DBUsuarios' en modo escritura
        cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actMyAccountPolicy.this, "USUARIOSHM", null, 2);
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
                    Intent actMainLogin = new Intent(actMyAccountPolicy.this, actMainLogin.class);
                    startActivity(actMainLogin);
                    return;

                }
                else
                {
                    //Obtenemos la instancia única de la cola de peticiones
                    requestQueue = RequestQueueSingleton.getRequestQueue(this);

                    //URL del detalle del proveedor
                    String urlMyCar = "http://app.towpod.net/ws_get_customer_car.php?idc=" + CursorDB.getString(0);
                    urlMyCar = urlMyCar.replaceAll(" ", "%20");

                    JsonArrayRequest requestAuto = new JsonArrayRequest(urlMyCar,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray responseAutos) {
                                    if (responseAutos.length() > 0)
                                    {
                                        try
                                        {
                                            responseAutos.getJSONObject(0).getString("car_id");

                                            // --VERIFICO SI HAY TARJETAS REGISTRADAS
                                            //Obtenemos la instancia única de la cola de peticiones
                                            requestQueue = RequestQueueSingleton.getRequestQueue(actMyAccountPolicy.this);

                                            //URL del detalle del proveedor
                                            String urlTarjeta = "http://app.towpod.net/ws_get_customer_card.php?idc=" + CursorDB.getString(0);
                                            urlTarjeta = urlTarjeta.replaceAll(" ", "%20");

                                            JsonArrayRequest requestTarjeta = new JsonArrayRequest(urlTarjeta,
                                                    new Response.Listener<JSONArray>() {
                                                        @Override
                                                        public void onResponse(JSONArray responseTarjeta) {
                                                            if (responseTarjeta.length() > 0)
                                                            {
                                                                try
                                                                {
                                                                    Intent actNuevaPoliza = new Intent(actMyAccountPolicy.this, actPolicyType.class);
                                                                    startActivity(actNuevaPoliza);
                                                                    responseTarjeta.getJSONObject(0).getString("card_number");
                                                                }
                                                                catch (JSONException e)
                                                                {
                                                                    Toast popUpError = Toast.makeText(actMyAccountPolicy.this, e.toString() , Toast.LENGTH_LONG);
                                                                    popUpError.show();
                                                                }
                                                            }
                                                            else
                                                            {
                                                                Snackbar.make(coordinatorLayoutView, "Registra un método de pago antes de contratar una póliza de seguro.", Snackbar.LENGTH_LONG)
                                                                        //.setActionTextColor(Color.CYAN)
                                                                        //Color del texto de la acción
                                                                        .setDuration(Snackbar.LENGTH_LONG)
                                                                        .setActionTextColor(Color.parseColor("#FFC107"))
                                                                        .setAction("Aceptar", new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View view)
                                                                            {
                                                                                Intent actNuevaTarjeta = new Intent(actMyAccountPolicy.this, actAddMyCard.class);
                                                                                startActivity(actNuevaTarjeta);
                                                                            }
                                                                        })
                                                                        .show();
                                                                return;
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

                                        }
                                        catch (JSONException e)
                                        {
                                            Toast popUpError = Toast.makeText(actMyAccountPolicy.this, e.toString() , Toast.LENGTH_LONG);
                                            popUpError.show();
                                        }
                                    }
                                    else
                                    {
                                        Snackbar.make(coordinatorLayoutView, "Debes registrar un vehículo antes de contratar una póliza de seguro.", Snackbar.LENGTH_LONG)
                                                //.setActionTextColor(Color.CYAN)
                                                //Color del texto de la acción
                                                .setActionTextColor(Color.parseColor("#FFC107"))
                                                .setAction("Aceptar", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view)
                                                    {
                                                        Intent actNuevoAuto = new Intent(actMyAccountPolicy.this, actAddNewCar.class);
                                                        startActivity(actNuevoAuto);
                                                    }
                                                })
                                                .show();
                                        return;

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
                }
            }
            catch (Exception e)
            {
                Toast popUpError = Toast.makeText(actMyAccountPolicy.this,  e.getMessage(), Toast.LENGTH_LONG);
                popUpError.show();
                return;
            }
            //Cerramos la base de datos
            db.close();
        }

    }

    public void EliminarPolizaRegistrada(String strKeyValue)
    {
        //El código se movió a la clase CustomListener
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
                    if (CursorDB.getCount() == 0) {
                        //Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
                        Intent actMainLogin = new Intent(this, actMainLogin.class);
                        startActivity(actMainLogin);
                        return;
                    }
                    else
                    {
                        //URL del detalle del proveedor
                        String url = "http://app.towpod.net/ws_drop_from_my_account.php?type=p&usr_id=" + CursorDB.getString(0) + "&key_value=" + strKeyValue;
                        url = url.replaceAll(" ", "%20");

                        JsonArrayRequest requestNewPolicy = new JsonArrayRequest(url,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray responseAuto)
                                    {

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

                        Toast popUpMensaje = Toast.makeText(this, "Tu póliza ha sido eliminada.", Toast.LENGTH_LONG);
                        popUpMensaje.show();

                        this.recreate();


                        //Le ponemos un tag que servirá para identificarla si la queremos cancelar
                        requestNewPolicy.setTag(JSON_REQUEST);
                        //Al ponerle la política de reintento, le hacemos casting, ya que el metodo recibe un objeto de la clase padre
                        requestNewPolicy = (JsonArrayRequest) setRetryPolicy(requestNewPolicy);
                        //Iniciamos la petición añadiéndola a la cola
                        requestQueue.add(requestNewPolicy);
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


    //Le pone una política de 4 reintentos con 4 segundos entre cada uno y la devuelve.
    public Request setRetryPolicy(Request request){
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }

}
