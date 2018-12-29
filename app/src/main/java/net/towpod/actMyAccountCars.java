package net.towpod;

import android.app.Dialog;
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
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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

public class actMyAccountCars extends AppCompatActivity {

    GridView ListaAutos;
    ArrayAdapter AdaptadorAutos;
    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    public final static String EXTRA_ID_CAR = "extra.id.car";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_my_account_cars);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Mis vehículos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        //Realizo el proceo en segundo plano y muestro el cuadro de espera
        ProgressDialog VentanaEspera = new ProgressDialog(this);
        VentanaEspera.setTitle("Obteniendo datos");
        VentanaEspera.setMessage("Espere un momento, por favor...");
        new actMyAccountCars.CargarListaEnSegundoPlano(VentanaEspera, this).execute();
    }


    private class CargarListaEnSegundoPlano extends AsyncTask<Void, Void, Void>
    {

        ProgressDialog progress;
        actMyAccountCars act;

        public CargarListaEnSegundoPlano(ProgressDialog progress, actMyAccountCars act)
        {
            this.progress = progress;
            this.act = act;
        }

        public void onPreExecute()
        {
            progress.show();
            //aquí se puede colocar código a ejecutarse previo
            //a la operación
        }

        public void onPostExecute(Void unused)
        {
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

        protected Void doInBackground(Void... params)
        {
            //realizar la operación aquí
            CargarVehiculos();
            return null;
        }

    }

    public void CargarVehiculos()
    {

        try
        {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actMyAccountCars.this, "USUARIOSHM", null, 2);
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
                        Intent actMainLogin = new Intent(actMyAccountCars.this, actMainLogin.class);
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
                                                //=======CARGA DE AUTOS DE LOS USUARIOS===============================================
                                                //==========================================================================================
                                                //TextView txtIDCustomer = (TextView) MyView.findViewById(R.id.txtIDCustomer);
                                                //txtIDCustomer.setText(String.valueOf(getArguments().getInt("prmIDCustomer")));

                                                //Instancia del ListView
                                                ListaAutos = (GridView) findViewById(R.id.ListViewAutos);

                                                //Obtenemos la instancia única de la cola de peticiones
                                                requestQueue = RequestQueueSingleton.getRequestQueue(actMyAccountCars.this);

                                                //Inicializar el adaptador con la fuente de datos
                                                List<clsAuto> Autos = new ArrayList<clsAuto>();
                                                AdaptadorAutos = new clsAutoArrayAdapter(actMyAccountCars.this, Autos);
                                                AdaptadorAutos.clear();

                                                //URL del detalle del proveedor
                                                String urlMyCar = "http://app.towpod.net/ws_get_customer_car.php?idc=" + response.getJSONObject(0).getInt("customer_id");
                                                urlMyCar = urlMyCar.replaceAll(" ", "%20");

                                                JsonArrayRequest requestAuto = new JsonArrayRequest(urlMyCar,
                                                        new Response.Listener<JSONArray>() {
                                                            @Override
                                                            public void onResponse(JSONArray responseAutos) {
                                                                try {
                                                                    if (responseAutos.length() == 0)
                                                                    {
                                                                        RelativeLayout loNoData = (RelativeLayout) findViewById(R.id.loNoData);
                                                                        loNoData.setVisibility(View.VISIBLE);

                                                                        LinearLayout loListado = (LinearLayout) findViewById(R.id.loListado);
                                                                        loListado.setVisibility(View.GONE);
                                                                    }
                                                                    else
                                                                    {
                                                                        for (int i = 0; i < responseAutos.length(); i++)
                                                                        {
                                                                            AdaptadorAutos.add(new clsAuto(
                                                                                    responseAutos.getJSONObject(i).getString("car_id"),
                                                                                    responseAutos.getJSONObject(i).getString("car_brand"),
                                                                                    responseAutos.getJSONObject(i).getString("car_model"),
                                                                                    responseAutos.getJSONObject(i).getInt("car_year")));
                                                                        }

                                                                        RelativeLayout loNoData = (RelativeLayout) findViewById(R.id.loNoData);
                                                                        loNoData.setVisibility(View.GONE);

                                                                        LinearLayout loListado = (LinearLayout) findViewById(R.id.loListado);
                                                                        loListado.setVisibility(View.VISIBLE);

                                                                    }

                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
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
                                                AdaptadorAutos = new clsAutoArrayAdapter(actMyAccountCars.this, Autos);

                                                //Relacionando la lista con el adaptador
                                                ListaAutos.setAdapter(AdaptadorAutos);

                                                //Creo el evento Clic para cada objeto de la lista
                                                ListaAutos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {
                                                        final clsAuto autoSeleccionado = (clsAuto) pariente.getItemAtPosition(posicion);

                                                        Intent actCarDetail = new Intent(actMyAccountCars.this, actCarDetail.class);
                                                        actCarDetail.putExtra(EXTRA_ID_CAR, autoSeleccionado.get_car_id());
                                                        startActivity(actCarDetail);

                                                        /*AlertDialog.Builder builder = new AlertDialog.Builder(actMyAccountCars.this);
                                                        final LayoutInflater inflater = actMyAccountCars.this.getLayoutInflater();
                                                        builder.setView(inflater.inflate(R.layout.dialog_action_on_car, null));

                                                        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
                                                        {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                //Nada para hacer aquí
                                                            }
                                                        });

                                                        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                                        {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                //((actMyAccount)getActivity()).EliminarAutoRegistrado(autoSeleccionado.get_car_id());

                                                                //CargarAutos(inflater, container, savedInstanceState);
                                                            }
                                                        });*/





                                                        /*AlertDialog alertDialog = builder.create();
                                                        alertDialog.setTitle("¿Qué deseas hacer con el vehículo seleccionado?");
                                                        //alertDialog.setMessage("Esta acción tambien eliminará cualquier póliza de seguro asignada al vehículo.\n\n" + + "]");
                                                        alertDialog.show();

                                                        TextView lblInfoCar = (TextView) alertDialog.findViewById(R.id.lblInfoSelectedCar);
                                                        lblInfoCar.setText(autoSeleccionado.get_car_brand() + " - " + autoSeleccionado.get_car_model() + " - " + autoSeleccionado.get_car_year() + "\nPlaca: " + autoSeleccionado.get_car_id());

                                                        Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                                                        theButton.setOnClickListener(new actMyAccountCars.CustomListenerActionOnCar(alertDialog, autoSeleccionado.get_car_id()));*/

                                                    }

                                                });

                                                //==========================================================================================
                                                //=======FIN DE CARGA DE Autos DE LOS USUARIOS========================================
                                                //==========================================================================================

                                            }
                                            catch (JSONException e) {
                                                Toast popUpError = Toast.makeText(actMyAccountCars.this, e.getMessage(), Toast.LENGTH_LONG);
                                                popUpError.show();
                                            }
                                        }
                                        else
                                        {
                                            //Si NO EXISTE LOCALMENTE NI EN LA WEB lo envio al Login para que inicie sesion o se registre
                                            Intent actMainLogin = new Intent(actMyAccountCars.this, actMainLogin.class);
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
                    Toast popUpError = Toast.makeText(actMyAccountCars.this,  e.getMessage(), Toast.LENGTH_LONG);
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

    class CustomListenerActionOnCar implements View.OnClickListener
    {
        private final Dialog dialog;
        private final String strCarID;

        public CustomListenerActionOnCar(Dialog MyDialog, String strCarID)
        {
            this.dialog = MyDialog;
            this.strCarID = strCarID;
        }

        @Override
        public void onClick(View v)
        {
            //DEFINO SI PAGO MATRICULA O ELIMINO
            final RadioButton rbOpcion1 = (RadioButton) dialog.findViewById(R.id.option1);
            final RadioButton rbOpcion2 = (RadioButton) dialog.findViewById(R.id.option2);
            //final RadioButton rbOpcion3 = (RadioButton) dialogTipo.findViewById(R.id.option3);

            dialog.dismiss();

            if (rbOpcion1.isChecked())
            {
                return;
            }
            if (rbOpcion2.isChecked())
            {
                EliminarAutoRegistrado(strCarID);
                return;
            }


        }
    }

    public void RegistrarAuto (View view)
    {
        Intent actAddNewCar = new Intent(this, actAddNewCar.class);
        startActivity(actAddNewCar);
    }

    public void EliminarAutoRegistrado(String strKeyValue)
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
                        String url = "http://app.towpod.net/ws_drop_from_my_account.php?type=v&usr_id=" + CursorDB.getString(0) + "&key_value=" + strKeyValue;
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
