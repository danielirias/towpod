package net.towpod;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;

public class actAddNewCar extends AppCompatActivity {

    ListView ListaPolizas;
    ArrayAdapter AdaptadorMarcas;
    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_add_new_car);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Registrar nuevo vehículo");

        //Busco la direccion de correo asociada al telefono entre los usarios registrados
        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this);

        //Con esta URL verifico si se han creado registros con este usuario.
        String urlUser = "http://app.towpod.net/ws_get_car_brand.php";
        urlUser = urlUser.replaceAll(" ", "%20");

        //Toast PopUpMensaje = Toast.makeText(actMainLogin.this, txtUserName.getText(), Toast.LENGTH_LONG);
        //PopUpMensaje.show();



        //Creamos la petición
        JsonArrayRequest requestUser = new JsonArrayRequest(urlUser,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        ArrayAdapter<CharSequence> adapter = new ArrayAdapter <CharSequence> (actAddNewCar.this, android.R.layout.simple_spinner_item );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        adapter.add("- Marca del vehículo -");

                        for (int i = 0; i < response.length(); i++)
                        {
                            try {
                                response.getJSONObject(i).getString("brand_name");
                                adapter.add(response.getJSONObject(i).getString("brand_name").toUpperCase());
                            } catch (JSONException e) {
                                Toast popUpError = Toast.makeText(actAddNewCar.this, e.getMessage() , Toast.LENGTH_LONG);
                                popUpError.show();
                            }
                        }

                        AutoCompleteTextView acMarca = (AutoCompleteTextView) findViewById(R.id.acMarca);
                        acMarca.setAdapter(adapter);

                        //Spinner spinMarca = (Spinner) findViewById(R.id.spinMarca);
                        //spinMarca.setAdapter(adapter);
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

    //Le pone una política de 4 reintentos con 4 segundos entre cada uno y la devuelve.
    public Request setRetryPolicy(Request request){
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }

    public void VerificarDatos(View view)
    {
        final EditText txtCarID = (EditText) findViewById(R.id.txtIDVehicular);
        final AutoCompleteTextView txtCarBrand = (AutoCompleteTextView) findViewById(R.id.acMarca);
        final EditText txtCarModel = (EditText) findViewById(R.id.txtModelo);
        final EditText txtCarColor = (EditText) findViewById(R.id.txtColor);
        final EditText txtCarYear = (EditText) findViewById(R.id.txtYear);

        final View coordinatorLayoutView = findViewById(R.id.snackbarPosition);
        final InputMethodManager IMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        IMM.hideSoftInputFromWindow(view.getWindowToken(), 0);

        if (CampoVacio(txtCarID) == true)
        {
            Snackbar.make(coordinatorLayoutView, "Escribe el número de registro vehicular o número de placa.", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(Color.CYAN)
                    //Color del texto de la acción
                    .setActionTextColor(Color.parseColor("#FFC107"))
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            txtCarID.requestFocus();
                            IMM.showSoftInput(txtCarID, InputMethodManager.SHOW_IMPLICIT);
                        }
                    })
                    .show();
            return;
        }

        if (CampoVacio(txtCarBrand) == true)
        {
            Snackbar.make(coordinatorLayoutView, "Escribe la marca de tu vehículo.", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(Color.CYAN)
                    //Color del texto de la acción
                    .setActionTextColor(Color.parseColor("#FFC107"))
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            txtCarBrand.requestFocus();
                            IMM.showSoftInput(txtCarBrand, InputMethodManager.SHOW_IMPLICIT);
                        }
                    })
                    .show();
            return;
        }


        if (CampoVacio(txtCarModel) == true)
        {
            Snackbar.make(coordinatorLayoutView, "Debes indicar el modelo de tu vehículo.", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(Color.CYAN)
                    //Color del texto de la acción
                    .setActionTextColor(Color.parseColor("#FFC107"))
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            txtCarModel.requestFocus();
                            IMM.showSoftInput(txtCarModel, InputMethodManager.SHOW_IMPLICIT);
                        }
                    })
                    .show();
            return;
        }

        if (CampoVacio(txtCarColor) == true)
        {
            Snackbar.make(coordinatorLayoutView, "Debes indicar el color de tu vehículo.", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(Color.CYAN)
                    //Color del texto de la acción
                    .setActionTextColor(Color.parseColor("#FFC107"))
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            txtCarColor.requestFocus();
                            IMM.showSoftInput(txtCarColor, InputMethodManager.SHOW_IMPLICIT);
                        }
                    })
                    .show();
            return;
        }

        if (CampoVacio(txtCarYear) == true)
        {
            Snackbar.make(coordinatorLayoutView, "Debes indicar el año de tu vehículo.", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(Color.CYAN)
                    //Color del texto de la acción
                    .setActionTextColor(Color.parseColor("#FFC107"))
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            txtCarYear.requestFocus();
                            IMM.showSoftInput(txtCarYear, InputMethodManager.SHOW_IMPLICIT);
                        }
                    })
                    .show();
            return;
        }
        if (txtCarYear.getText().toString().trim().length() < 4)
        {
            Snackbar.make(coordinatorLayoutView, "El año de tu vehículo deben 4 dígitos.", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(Color.CYAN)
                    //Color del texto de la acción
                    .setActionTextColor(Color.parseColor("#FFC107"))
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            txtCarYear.requestFocus();
                            IMM.showSoftInput(txtCarYear, InputMethodManager.SHOW_IMPLICIT);
                        }
                    })
                    .show();
            return;
        }

        //Realizo el proceso en segundo plano y muestro el cuadro de espera
        ProgressDialog VentanaEspera = new ProgressDialog(this);
        VentanaEspera.setTitle("Guardando datos");
        VentanaEspera.setMessage("Espere un momento, por favor...");
        new actAddNewCar.ProcesoSegundoPlano(VentanaEspera, this).execute();

    }

    public void AgregarAuto()
    {
        final EditText txtCarID = (EditText) findViewById(R.id.txtIDVehicular);
        final AutoCompleteTextView txtCarBrand = (AutoCompleteTextView) findViewById(R.id.acMarca);
        final EditText txtCarModel = (EditText) findViewById(R.id.txtModelo);
        final EditText txtCarColor = (EditText) findViewById(R.id.txtColor);
        final EditText txtCarYear = (EditText) findViewById(R.id.txtYear);


        //Abrimos la base de datos 'DBUsuarios' en modo escritura
        cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actAddNewCar.this, "USUARIOSHM", null, 2);
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
                    Intent actMainLogin = new Intent(actAddNewCar.this, actMainLogin.class);
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

                                            //URL para guardar datos
                                            String url = "http://app.towpod.net/ws_add_new_car.php?" +
                                                    "c_id="+ txtCarID.getText() +
                                                    "&c_brand="+ txtCarBrand.getText() +
                                                    "&c_model="+txtCarModel.getText() +
                                                    "&c_year="+txtCarYear.getText().toString().trim() +
                                                    "&c_color="+txtCarColor.getText().toString().trim() +
                                                    "&usr_id="+response.getJSONObject(0).getInt("customer_id");

                                            url = url.replaceAll(" ", "%20");

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
                                        catch (JSONException e) {
                                            Toast popUpError = Toast.makeText(actAddNewCar.this, e.getMessage(), Toast.LENGTH_LONG);
                                            popUpError.show();
                                        }
                                    }
                                    else
                                    {
                                        //Si NO EXISTE LOCALMENTE NI EN LA WEB lo envio al Login para que inicie sesion o se registre
                                        Intent actMainLogin = new Intent(actAddNewCar.this, actMainLogin.class);
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
                Toast popUpError = Toast.makeText(actAddNewCar.this,  e.getMessage(), Toast.LENGTH_LONG);
                popUpError.show();
            }

            //Cerramos la base de datos
            db.close();

        }

    }

    private class ProcesoSegundoPlano extends AsyncTask<Void, Void, Void> {

        ProgressDialog progress;
        actAddNewCar act;

        public ProcesoSegundoPlano(ProgressDialog progress, actAddNewCar act) {
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

                    Toast popUpMensaje = Toast.makeText(actAddNewCar.this, "Tu vehículo ha sido registrado con éxito", Toast.LENGTH_LONG);
                    popUpMensaje.show();

                    actAddNewCar.super.finish();
                }
            }, 1500);

        }

        protected Void doInBackground(Void... params) {
            //realizar la operación aquí
            AgregarAuto();
            return null;
        }

    }

    private boolean CampoVacio(EditText myeditText)
    {
        return myeditText.getText().toString().trim().length() == 0;
    }


}
