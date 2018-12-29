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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Calendar;

public class actAddMyCard extends AppCompatActivity {

    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_add_my_card);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Registrar tarjeta");

        //Lleno el spinner de los meses
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        int MyMonth = Calendar.getInstance().get(Calendar.MONTH);
        for(int i = (MyMonth + 1); i <= 12; i+=1)
        {
            if (i<10)
            {
                adapter.add("0" + String.valueOf(i));
            }
            else
            {
                adapter.add(String.valueOf(i));
            }

        }
        Spinner s = (Spinner) findViewById(R.id.spinnerMonth);
        s.setAdapter(adapter);

        //Lleno los años
        ArrayAdapter<CharSequence> adapterYear = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        int MyYear = Calendar.getInstance().get(Calendar.YEAR);
        for(int i = MyYear; i <= (MyYear + 10); i+=1)
        {
            adapterYear.add(String.valueOf(i));
        }
        Spinner sy = (Spinner) findViewById(R.id.spinnerYear);
        sy.setAdapter(adapterYear);
    }


    public void VerificarDatos(View view)
    {
        final EditText txtNumeroTarjeta = (EditText) findViewById(R.id.txtNumeroTarjeta);
        final EditText txtNombreTarjeta = (EditText) findViewById(R.id.txtNombreTarjeta);
        final Spinner spinnerMonth = (Spinner) findViewById(R.id.spinnerMonth);
        final Spinner spinnerYear = (Spinner) findViewById(R.id.spinnerYear);
        final EditText txtVerificationCode = (EditText) findViewById(R.id.txtVerificationCode);


        final View coordinatorLayoutView = findViewById(R.id.snackbarPosition);
        final InputMethodManager IMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        IMM.hideSoftInputFromWindow(view.getWindowToken(), 0);

        if (CampoVacio(txtNumeroTarjeta) == true)
        {
            Snackbar.make(coordinatorLayoutView, "Escribe el número de tu tarjeta.", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(Color.CYAN)
                    //Color del texto de la acción
                    .setActionTextColor(Color.parseColor("#FFC107"))
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            txtNumeroTarjeta.requestFocus();
                            IMM.showSoftInput(txtNumeroTarjeta, InputMethodManager.SHOW_IMPLICIT);
                        }
                    })
                    .show();
            return;
        }

        if (CampoVacio(txtNombreTarjeta) == true)
        {
            Snackbar.make(coordinatorLayoutView, "Escribe el nombre que aparece en tu tarjeta.", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(Color.CYAN)
                    //Color del texto de la acción
                    .setActionTextColor(Color.parseColor("#FFC107"))
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            txtNombreTarjeta.requestFocus();
                            IMM.showSoftInput(txtNombreTarjeta, InputMethodManager.SHOW_IMPLICIT);
                        }
                    })
                    .show();
            return;
        }


        if (CampoVacio(txtVerificationCode) == true)
        {
            Snackbar.make(coordinatorLayoutView, "Hace falta el código de verificación de tu tarjeta.", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(Color.CYAN)
                    //Color del texto de la acción
                    .setActionTextColor(Color.parseColor("#FFC107"))
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            txtVerificationCode.requestFocus();
                            IMM.showSoftInput(txtVerificationCode, InputMethodManager.SHOW_IMPLICIT);
                        }
                    })
                    .show();
            return;
        }


        //Realizo el proceso en segundo plano y muestro el cuadro de espera
        ProgressDialog VentanaEspera = new ProgressDialog(this);
        VentanaEspera.setTitle("Guardando datos");
        VentanaEspera.setMessage("Espere un momento, por favor...");
        new actAddMyCard.ProcesoSegundoPlano(VentanaEspera, this).execute();

    }

    private boolean CampoVacio(EditText myeditText)
    {
        return myeditText.getText().toString().trim().length() == 0;
    }

    private class ProcesoSegundoPlano extends AsyncTask<Void, Void, Void> {

        ProgressDialog progress;
        actAddMyCard act;

        public ProcesoSegundoPlano(ProgressDialog progress, actAddMyCard act) {
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

                    Toast popUpMensaje = Toast.makeText(actAddMyCard.this, "Tu tarjeta ha sido registrada con éxito", Toast.LENGTH_LONG);
                    popUpMensaje.show();

                    actAddMyCard.super.finish();
                }
            }, 1500);

        }

        protected Void doInBackground(Void... params) {
            //realizar la operación aquí
            AgregarTarjeta();
            return null;
        }

    }

    public void AgregarTarjeta()
    {
        final EditText txtNumeroTarjeta = (EditText) findViewById(R.id.txtNumeroTarjeta);
        final EditText txtNombreTarjeta = (EditText) findViewById(R.id.txtNombreTarjeta);
        final Spinner spinnerMonth = (Spinner) findViewById(R.id.spinnerMonth);
        final Spinner spinnerYear = (Spinner) findViewById(R.id.spinnerYear);
        final EditText txtVerificationCode = (EditText) findViewById(R.id.txtVerificationCode);


        //Abrimos la base de datos 'DBUsuarios' en modo escritura
        cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actAddMyCard.this, "USUARIOSHM", null, 2);
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
                    Intent actMainLogin = new Intent(actAddMyCard.this, actMainLogin.class);
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
                                            String url = "http://app.towpod.net/ws_add_my_card.php?" +
                                                    "card_number="+ txtNumeroTarjeta.getText().toString().trim() +
                                                    "&card_name="+ txtNombreTarjeta.getText().toString().trim() +
                                                    "&card_valid="+spinnerMonth.getSelectedItem().toString() + "-" + spinnerYear.getSelectedItem().toString() +
                                                    "&card_code="+txtVerificationCode.getText().toString().trim() +
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
                                            Toast popUpError = Toast.makeText(actAddMyCard.this, e.getMessage(), Toast.LENGTH_LONG);
                                            popUpError.show();
                                        }
                                    }
                                    else
                                    {
                                        //Si NO EXISTE LOCALMENTE NI EN LA WEB lo envio al Login para que inicie sesion o se registre
                                        Intent actMainLogin = new Intent(actAddMyCard.this, actMainLogin.class);
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
                Toast popUpError = Toast.makeText(actAddMyCard.this,  e.getMessage(), Toast.LENGTH_LONG);
                popUpError.show();
            }

            //Cerramos la base de datos
            db.close();

        }

    }

    public Request setRetryPolicy(Request request){
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }


}
