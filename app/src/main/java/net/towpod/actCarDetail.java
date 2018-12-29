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

public class actCarDetail extends AppCompatActivity {

    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;
    public String prmIDCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_my_account_car_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        setTitle("Detalle del vehículo");

        //Realizo el proceo en segundo plano y muestro el cuadro de espera
        ProgressDialog VentanaEspera = new ProgressDialog(this);
        VentanaEspera.setTitle("Obteniendo información");
        VentanaEspera.setMessage("Espere un momento, por favor...");
        new actCarDetail.ProcesoSegundoPlano(VentanaEspera, this).execute();

    }

    private class ProcesoSegundoPlano extends AsyncTask<Void, Void, Void>
    {

        ProgressDialog progress;
        actCarDetail act;

        public ProcesoSegundoPlano(ProgressDialog progress, actCarDetail act) {
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

        try
        {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actCarDetail.this, "USUARIOSHM", null, 2);
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
                        Intent actMainLogin = new Intent(actCarDetail.this, actMainLogin.class);
                        startActivity(actMainLogin);
                        return;

                    }
                    else
                    {
                        Intent intent = getIntent();
                        prmIDCar = intent.getStringExtra(actMyAccountCars.EXTRA_ID_CAR);

                        Toast popUpError = Toast.makeText(actCarDetail.this, prmIDCar, Toast.LENGTH_LONG);
                        popUpError.show();

                        ///==================================================================================================
                        //Obtenemos la instancia única de la cola de peticiones
                        requestQueue = RequestQueueSingleton.getRequestQueue(this);

                        //URL del detalle del proveedor
                        String url = "http://app.towpod.net/ws_get_customer_car_info.php?idc="+CursorDB.getString(0)+"&idv="+prmIDCar;
                        url = url.replaceAll(" ", "%20");

                        JsonArrayRequest request = new JsonArrayRequest(url,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        try
                                        {
                                            TextView lblPlaca = (TextView) findViewById(R.id.lblPlaca);
                                            lblPlaca.setText(response.getJSONObject(0).getString("car_id"));

                                            TextView lblMarca = (TextView) findViewById(R.id.lblMarca);
                                            lblMarca.setText(response.getJSONObject(0).getString("car_brand"));

                                            TextView lblModelo = (TextView) findViewById(R.id.lblModelo);
                                            lblModelo.setText(response.getJSONObject(0).getString("car_model"));

                                            TextView lblYear = (TextView) findViewById(R.id.lblYear);
                                            lblYear.setText(response.getJSONObject(0).getString("car_year"));

                                            TextView lblColor = (TextView) findViewById(R.id.lblColor);
                                            lblColor.setText(response.getJSONObject(0).getString("car_color"));



                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        //Si recibimos un error, mostraremos la causa
                                        Toast popUpError = Toast.makeText(actCarDetail.this, error.getMessage(), Toast.LENGTH_LONG);
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

                        //==================================================================================================
                    }
                }
                catch (Exception e)
                {
                    Toast popUpError = Toast.makeText(actCarDetail.this,  e.getMessage(), Toast.LENGTH_LONG);
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

    public void SolicitarEliminar(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(actCarDetail.this);
        final LayoutInflater inflater = actCarDetail.this.getLayoutInflater();
        //builder.setView(inflater.inflate(R.layout.dialog_action_on_car, null));

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id) {
                //Nada para hacer aquí
            }
        });

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id) {
                EliminarAutoRegistrado();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setTitle("Eliminar vehículo");
        alertDialog.setMessage("Esta acción tambien eliminará cualquier póliza de seguro asignada al vehículo.\n\n¿Deseas continuar?");
        alertDialog.show();


    }
    public void EliminarAutoRegistrado()
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
                        String url = "http://app.towpod.net/ws_drop_from_my_account.php?type=v&usr_id="+CursorDB.getString(0)+"&key_value="+prmIDCar;
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

                        Toast popUpMensaje = Toast.makeText(this, "Tu vehículo ha sido eliminado.", Toast.LENGTH_LONG);
                        popUpMensaje.show();

                        this.finish();

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
