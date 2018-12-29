package net.towpod;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

public class actMyAccountMyCards extends AppCompatActivity {

    ListView ListaTarjetas;
    ArrayAdapter AdaptadorTarjetas;
    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_my_account_my_cards);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Métodos de pago");
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        //Realizo el proceo en segundo plano y muestro el cuadro de espera
        ProgressDialog VentanaEspera = new ProgressDialog(this);
        VentanaEspera.setTitle("Obteniendo datos");
        VentanaEspera.setMessage("Espere un momento, por favor...");
        new actMyAccountMyCards.CargarListaEnSegundoPlano(VentanaEspera, this).execute();
    }


    private class CargarListaEnSegundoPlano extends AsyncTask<Void, Void, Void>
    {

        ProgressDialog progress;
        actMyAccountMyCards act;

        public CargarListaEnSegundoPlano(ProgressDialog progress, actMyAccountMyCards act) {
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
            CargarTarjetas();
            return null;
        }

    }

    public void CargarTarjetas()
    {

        try
        {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actMyAccountMyCards.this, "USUARIOSHM", null, 2);
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
                        Intent actMainLogin = new Intent(actMyAccountMyCards.this, actMainLogin.class);
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
                                                //=======CARGA DE Tarjetas DE LOS USUARIOS===============================================
                                                //==========================================================================================

                                                //Instancia del ListView
                                                ListaTarjetas = (ListView) findViewById(R.id.ListViewTarjetas);

                                                //Obtenemos la instancia única de la cola de peticiones
                                                requestQueue = RequestQueueSingleton.getRequestQueue(actMyAccountMyCards.this);

                                                //Inicializar el adaptador con la fuente de datos
                                                List<clsTarjeta> Tarjetas = new ArrayList<clsTarjeta>();
                                                AdaptadorTarjetas = new clsTarjetaArrayAdapter(actMyAccountMyCards.this, Tarjetas);
                                                AdaptadorTarjetas.clear();

                                                //URL del detalle del proveedor
                                                String urlTarjeta = "http://app.towpod.net/ws_get_customer_card.php?idc=" + response.getJSONObject(0).getString("customer_id");
                                                urlTarjeta = urlTarjeta.replaceAll(" ", "%20");

                                                JsonArrayRequest requestTarjeta = new JsonArrayRequest(urlTarjeta,
                                                        new Response.Listener<JSONArray>() {
                                                            @Override
                                                            public void onResponse(JSONArray responseTarjeta)
                                                            {
                                                                if (responseTarjeta.length() > 0)
                                                                {
                                                                    RelativeLayout loNoData = (RelativeLayout) findViewById(R.id.loNoData);
                                                                    LinearLayout.LayoutParams medidasLAYOUT = (LinearLayout.LayoutParams) loNoData.getLayoutParams();
                                                                    medidasLAYOUT.height = 0 ;//medidasLAYOUT.MATCH_PARENT;
                                                                    loNoData.setLayoutParams(medidasLAYOUT);

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
                                                AdaptadorTarjetas = new clsTarjetaArrayAdapter(actMyAccountMyCards.this, Tarjetas);

                                                //Relacionando la lista con el adaptador
                                                ListaTarjetas.setAdapter(AdaptadorTarjetas);

                                                //Creo el evento Clic para cada objeto de la lista
                                                ListaTarjetas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                                    @Override
                                                    public boolean onItemLongClick(AdapterView<?> pariente, View view, int posicion, long id) {
                                                        final clsTarjeta tarjetaSeleccionada = (clsTarjeta) pariente.getItemAtPosition(posicion);

                                                        final AlertDialog.Builder builder = new AlertDialog.Builder(actMyAccountMyCards.this);
                                                        LayoutInflater inflater = actMyAccountMyCards.this.getLayoutInflater();
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
                                                                //((actMyAccount)getActivity()).EliminarTarjetaRegistrada(tarjetaSeleccionada.get_card_number());
                                                                EliminarTarjetaRegistrada(tarjetaSeleccionada.get_card_number());
                                                            }
                                                        });

                                                        AlertDialog alertDialog = builder.create();
                                                        alertDialog.setTitle("¿Deseas eliminar la tarjeta seleccionada?");
                                                        alertDialog.setMessage("Esta acción también eliminará cualquier servicio de asistencia que tengas asociado a la tarjeta:\n\n" + tarjetaSeleccionada.get_card_name() + "\n" + tarjetaSeleccionada.get_card_number());
                                                        alertDialog.show();

                                                        return false;

                                                    }

                                                });

                                                //==========================================================================================
                                                //=======FIN DE CARGA DE Tarjetas DE LOS USUARIOS========================================
                                                //==========================================================================================

                                            }
                                            catch (JSONException e) {
                                                Toast popUpError = Toast.makeText(actMyAccountMyCards.this, e.getMessage(), Toast.LENGTH_LONG);
                                                popUpError.show();
                                            }
                                        }
                                        else
                                        {
                                            //Si NO EXISTE LOCALMENTE NI EN LA WEB lo envio al Login para que inicie sesion o se registre
                                            Intent actMainLogin = new Intent(actMyAccountMyCards.this, actMainLogin.class);
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
                    Toast popUpError = Toast.makeText(actMyAccountMyCards.this,  e.getMessage(), Toast.LENGTH_LONG);
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
    
    public void RegistrarTarjeta (View view)
    {
        Intent actAddMyCard = new Intent(this, actAddMyCard.class);
        startActivity(actAddMyCard);
    }

    public void EliminarTarjetaRegistrada(String strKeyValue)
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
                        String url = "http://app.towpod.net/ws_drop_from_my_account.php?type=c&usr_id=" + CursorDB.getString(0) + "&key_value=" + strKeyValue;
                        url = url.replaceAll(" ", "%20");

                        JsonArrayRequest requestNewPolicy = new JsonArrayRequest(url,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response)
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

                        Toast popUpMensaje = Toast.makeText(this, "Tu tarjeta ha sido eliminada.", Toast.LENGTH_LONG);
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

    public Request setRetryPolicy(Request request){
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }

}
