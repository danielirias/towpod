package net.towpod;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class actMyAccountHistory extends AppCompatActivity {

    ListView ListaHistorial;
    ArrayAdapter AdaptadorHistorial;
    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    public final static String EXTRA_ID_HISTORY = "extra.id.history";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_my_account_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Historial de asistencias");
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        //Realizo el proceo en segundo plano y muestro el cuadro de espera
        ProgressDialog VentanaEspera = new ProgressDialog(this);
        VentanaEspera.setTitle("Obteniendo datos");
        VentanaEspera.setMessage("Espere un momento, por favor...");
        new actMyAccountHistory.CargarListaEnSegundoPlano(VentanaEspera, this).execute();
    }


    private class CargarListaEnSegundoPlano extends AsyncTask<Void, Void, Void>
    {

        ProgressDialog progress;
        actMyAccountHistory act;

        public CargarListaEnSegundoPlano(ProgressDialog progress, actMyAccountHistory act) {
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
            CargarHistorial();
            return null;
        }

    }

    public void CargarHistorial()
    {

        try
        {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actMyAccountHistory.this, "USUARIOSHM", null, 2);
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
                        Intent actMainLogin = new Intent(actMyAccountHistory.this, actMainLogin.class);
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
                        String urlUser = "http://app.towpod.net/ws_get_login_data_customer.php?usr="+CursorDB.getString(0)+"&pwd="+CursorDB.getString(1);
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
                                                //=======CARGA DE AUTOS DE LOS USUARIOS===============================================
                                                //==========================================================================================
                                                //TextView txtIDCustomer = (TextView) MyView.findViewById(R.id.txtIDCustomer);
                                                //txtIDCustomer.setText(String.valueOf(getArguments().getInt("prmIDCustomer")));

                                                //Instancia del ListView
                                                ListaHistorial = (ListView) findViewById(R.id.ListViewHistory);

                                                //Obtenemos la instancia única de la cola de peticiones
                                                requestQueue = RequestQueueSingleton.getRequestQueue(actMyAccountHistory.this);

                                                //Inicializar el adaptador con la fuente de datos
                                                List<clsHistory> Historia = new ArrayList<clsHistory>();
                                                AdaptadorHistorial = new clsHistoryArrayAdapter(actMyAccountHistory.this, Historia);
                                                AdaptadorHistorial.clear();

                                                //URL del detalle del proveedor
                                                String url = "http://app.towpod.net/ws_get_customer_history.php?idc=" + response.getJSONObject(0).getInt("customer_id");
                                                url = url.replaceAll(" ", "%20");

                                                JsonArrayRequest request = new JsonArrayRequest(url,
                                                        new Response.Listener<JSONArray>() {
                                                            @Override
                                                            public void onResponse(JSONArray response) {

                                                                if (response.length() > 0)
                                                                {
                                                                    RelativeLayout loNoData = (RelativeLayout) findViewById(R.id.loNoData);
                                                                    loNoData.setVisibility(View.GONE);

                                                                    LinearLayout loListado  = (LinearLayout) findViewById(R.id.loListado);
                                                                    loListado.setVisibility(View.VISIBLE);

                                                                    for (int i = 0; i < response.length(); i++){
                                                                        try
                                                                        {
                                                                            AdaptadorHistorial.add(new clsHistory(
                                                                                    response.getJSONObject(i).getInt("row_id"),
                                                                                    response.getJSONObject(i).getInt("customer_id"),
                                                                                    response.getJSONObject(i).getString("request_date"),
                                                                                    response.getJSONObject(i).getString("request_time"),
                                                                                    response.getJSONObject(i).getInt("assistance_id"),
                                                                                    response.getJSONObject(i).getString("assistance_name"),
                                                                                    response.getJSONObject(i).getString("customer_comment"),
                                                                                    response.getJSONObject(i).getString("policy_number"),
                                                                                    response.getJSONObject(i).getString("customer_lat"),
                                                                                    response.getJSONObject(i).getString("customer_lon"),
                                                                                    response.getJSONObject(i).getString("car_id"),
                                                                                    response.getJSONObject(i).getString("car_brand"),
                                                                                    response.getJSONObject(i).getString("car_model"),
                                                                                    response.getJSONObject(i).getInt("car_year"),
                                                                                    response.getJSONObject(i).getString("card_number"),
                                                                                    response.getJSONObject(i).getDouble("service_value"),
                                                                                    response.getJSONObject(i).getString("confirmation_code"),
                                                                                    response.getJSONObject(i).getInt("verification_status"),
                                                                                    response.getJSONObject(i).getInt("assistance_driver_required")));
                                                                        }
                                                                        catch (JSONException e)
                                                                        {
                                                                            //Toast popUpError = Toast.makeText(actProviderData.this, e.toString() , Toast.LENGTH_LONG);
                                                                            //popUpError.show();
                                                                        }
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
                                                request.setTag(JSON_REQUEST);
                                                //Al ponerle la política de reintento, le hacemos casting, ya que el metodo recibe un objeto de la clase padre
                                                request = (JsonArrayRequest) setRetryPolicy(request);
                                                //Iniciamos la petición añadiéndola a la cola
                                                requestQueue.add(request);

                                                //Inicializar el adaptador con la fuente de datos
                                                AdaptadorHistorial = new clsHistoryArrayAdapter(actMyAccountHistory.this, Historia);

                                                //Relacionando la lista con el adaptador
                                                ListaHistorial.setAdapter(AdaptadorHistorial);

                                                //Creo el evento Clic para cada objeto de la lista
                                                ListaHistorial.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id)
                                                    {
                                                        final clsHistory itemSleccionado = (clsHistory) pariente.getItemAtPosition(posicion);

                                                        if (itemSleccionado.getDriverRequired() == 0)
                                                        {
                                                            Intent actHistoryDetail = new Intent(actMyAccountHistory.this, actMyAccountHistoryDetail.class);
                                                            actHistoryDetail.putExtra(EXTRA_ID_HISTORY, String.valueOf(itemSleccionado.get_row_id()));
                                                            startActivity(actHistoryDetail);
                                                        }
                                                        else if (itemSleccionado.getDriverRequired() == 1)
                                                        {
                                                            Intent actHistoryDetail = new Intent(actMyAccountHistory.this, actMyAccountHistoryDetailDriver.class);
                                                            actHistoryDetail.putExtra(EXTRA_ID_HISTORY, String.valueOf(itemSleccionado.get_row_id()));
                                                            startActivity(actHistoryDetail);
                                                        }

                                                    }

                                                });

                                                //==========================================================================================
                                                //=======FIN DE CARGA DE Autos DE LOS USUARIOS========================================
                                                //==========================================================================================

                                            }
                                            catch (JSONException e) {
                                                Toast popUpError = Toast.makeText(actMyAccountHistory.this, e.getMessage(), Toast.LENGTH_LONG);
                                                popUpError.show();
                                            }
                                        }
                                        else
                                        {
                                            //Si NO EXISTE LOCALMENTE NI EN LA WEB lo envio al Login para que inicie sesion o se registre
                                            Intent actMainLogin = new Intent(actMyAccountHistory.this, actMainLogin.class);
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
                    Toast popUpError = Toast.makeText(actMyAccountHistory.this,  e.getMessage(), Toast.LENGTH_LONG);
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

    public Request setRetryPolicy(Request request){
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }
    
}
