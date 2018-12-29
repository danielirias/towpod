package net.towpod;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
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

public class actRegistration extends AppCompatActivity {

    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    Spinner ListaPaises;
    ArrayAdapter AdaptadorPaises;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_registration);

        EditText txtName = (EditText) findViewById(R.id.txtName);
        txtName.requestFocus();

        //Asigno el código de área
        String strIsoName = this.getResources().getConfiguration().locale.getCountry();


        //Busco la direccion de correo asociada al telefono entre los usarios registrados
        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this);

        //CONSULTA UTILIZADA PARA OBTENER LOS DATOS DEL PAIS EN BASE AL CÓDIGO DE PAIS OBTENIDO DE LA CONFIGURACIÓN DEL TELEÉFONO
        //String url = "http://app.towpod.net/ws_get_country_data.php?iso="+strIsoName;

        //CONSULTA UTILIZADA PARA OBTENER EL LISTADO DE PAISES HABILITADOS
        String url = "http://app.towpod.net/ws_get_country_data.php";
        url = url.replaceAll(" ", "%20");

        //Toast PopUpMensaje = Toast.makeText(actMainLogin.this, txtUserName.getText(), Toast.LENGTH_LONG);
        //PopUpMensaje.show();

        //Inicializar el adaptador con la fuente de datos
        List<clsCountry> Paises = new ArrayList<clsCountry>();
        AdaptadorPaises = new clsCountryArrayAdapter(actRegistration.this, Paises);
        AdaptadorPaises.clear();

        //Creamos la petición
        JsonArrayRequest requestCountry = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() > 0)
                        {

                            for (int i = 0; i < response.length(); i++)
                            {
                                try {
                                    //txtDescripcion.setText(response.getJSONObject(0).getString("category_description"));
                                    AdaptadorPaises.add(new clsCountry(
                                            response.getJSONObject(i).getString("country_name"),
                                            response.getJSONObject(i).getString("country_iso_name"),
                                            response.getJSONObject(i).getString("country_area_code"),
                                            response.getJSONObject(i).getString("country_currency")));
                                } catch (JSONException e) {
                                    Toast popUpError = Toast.makeText(actRegistration.this, e.getMessage() , Toast.LENGTH_LONG);
                                    popUpError.show();
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

                    }
                }
        );

        //Le ponemos un tag que servirá para identificarla si la queremos cancelar
        requestCountry.setTag(JSON_REQUEST);
        //Al ponerle la política de reintento, le hacemos casting, ya que el metodo recibe un objeto de la clase padre
        requestCountry = (JsonArrayRequest) setRetryPolicy(requestCountry);
        //Iniciamos la petición añadiéndola a la cola
        requestQueue.add(requestCountry);

        //Inicializar el adaptador con la fuente de datos
        AdaptadorPaises = new clsCountryArrayAdapter(actRegistration.this, Paises);

        //Instancia del Spinner
        ListaPaises = (Spinner) findViewById(R.id.spinnerCountry);

        //Relacionando la lista con el adaptador
        ListaPaises.setAdapter(AdaptadorPaises);

        //Creo el evento Clic para cada objeto de la lista
        ListaPaises.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> pariente, View view, int posicion, long id) {
                clsCountry itemSeleccionado = (clsCountry) pariente.getItemAtPosition(posicion);
                CharSequence texto = "Seleccionado: "+itemSeleccionado.get_country_name();
                Toast toast = Toast.makeText(actRegistration.this, texto, Toast.LENGTH_SHORT);
                //toast.show();

                TextView txtAreaCode = (TextView) findViewById(R.id.txtAreaCode);
                txtAreaCode.setText(itemSeleccionado.get_country_area_code());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });




    }

    public void RegistrarUsuario(View view)
    {

        View coordinatorLayoutView = findViewById(R.id.snackbarPosition);
        final EditText txtName = (EditText) findViewById(R.id.txtName);
        final EditText txtLastname = (EditText) findViewById(R.id.txtLastname);
        final EditText txtPhone = (EditText) findViewById(R.id.txtPhone);
        final EditText txtEmail = (EditText) findViewById(R.id.txtEmail);
        final EditText txtPassword = (EditText) findViewById(R.id.txtPassword);
        final EditText txtRePassword = (EditText) findViewById(R.id.txtRePassword);

        if (CampoVacio(txtName)==true)
        {
            Snackbar.make(coordinatorLayoutView, "Debes escribir tu nombre.", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(Color.CYAN)
                    //Color del texto de la acción
                    .setActionTextColor(Color.parseColor("#FFC107"))
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            txtName.requestFocus();
                            InputMethodManager IMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            IMM.showSoftInput(txtName, InputMethodManager.SHOW_IMPLICIT);
                        }
                    })
                    .show();

            return;
        }

        if (CampoVacio(txtLastname)==true)
        {
            Snackbar.make(coordinatorLayoutView, "Debes escribir tu apellido.", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(Color.CYAN)
                    //Color del texto de la acción
                    .setActionTextColor(Color.parseColor("#FFC107"))
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            txtLastname.requestFocus();
                            InputMethodManager IMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            IMM.showSoftInput(txtLastname, InputMethodManager.SHOW_IMPLICIT);
                        }
                    })
                    .show();
            return;
        }

        if (CampoVacio(txtPhone)==true)
        {
            Snackbar.make(coordinatorLayoutView, "Debes un número telefónico.", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(Color.CYAN)
                    //Color del texto de la acción
                    .setActionTextColor(Color.parseColor("#FFC107"))
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            txtPhone.requestFocus();
                            InputMethodManager IMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            IMM.showSoftInput(txtPhone, InputMethodManager.SHOW_IMPLICIT);
                        }
                    })
                    .show();
            return;
        }

        if (CampoVacio(txtEmail)==true)
        {
            Snackbar.make(coordinatorLayoutView, "Debes escribir tu correo electrónico.", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(Color.CYAN)
                    //Color del texto de la acción
                    .setActionTextColor(Color.parseColor("#FFC107"))
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            txtEmail.requestFocus();
                            InputMethodManager IMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            IMM.showSoftInput(txtEmail, InputMethodManager.SHOW_IMPLICIT);
                        }
                    })
                    .show();
            return;
        }

        if (CampoVacio(txtPassword)==true)
        {
            Snackbar.make(coordinatorLayoutView, "Escribe una contraseña.", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(Color.CYAN)
                    //Color del texto de la acción
                    .setActionTextColor(Color.parseColor("#FFC107"))
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            txtPassword.requestFocus();
                            InputMethodManager IMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            IMM.showSoftInput(txtPassword, InputMethodManager.SHOW_IMPLICIT);
                        }
                    })
                    .show();
            return;
        }
        if (txtPassword.getText().toString().trim().length() < 8)
        {
            Snackbar.make(coordinatorLayoutView, "La contraseña debe tener al menos 8 caracteres.", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(Color.CYAN)
                    //Color del texto de la acción
                    .setActionTextColor(Color.parseColor("#FFC107"))
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            txtPassword.requestFocus();
                            InputMethodManager IMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            IMM.showSoftInput(txtPassword, InputMethodManager.SHOW_IMPLICIT);
                        }
                    })
                    .show();
            return;
        }

        if (CampoVacio(txtRePassword)==true)
        {
            Snackbar.make(coordinatorLayoutView, "Debes confirmar tu contraseña.", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(Color.CYAN)
                    //Color del texto de la acción
                    .setActionTextColor(Color.parseColor("#FFC107"))
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            txtRePassword.requestFocus();
                            InputMethodManager IMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            IMM.showSoftInput(txtRePassword, InputMethodManager.SHOW_IMPLICIT);
                        }
                    })
                    .show();
            return;
        }

        if (txtPassword.getText().toString().trim().equals(txtRePassword.getText().toString().trim()))
        {
            Button btnRegister  = (Button) findViewById(R.id.btnRegister);
            btnRegister.setEnabled(false);

            //Guardo los datos del usuario
            //Realizo el proceo en segundo plano y muestro el cuadro de espera
            ProgressDialog VentanaEspera = new ProgressDialog(this);
            VentanaEspera.setTitle("Guardando datos");
            VentanaEspera.setMessage("Espere un momento, por favor...");
            new actRegistration.ProcesoSegundoPlano(VentanaEspera, this).execute();
            return;
        }
        else
        {

            Toast PopUpMensaje = Toast.makeText(this, txtPassword.getText(), Toast.LENGTH_LONG);
            //PopUpMensaje.show();

            Toast PopUpMensaje2 = Toast.makeText(this, txtRePassword.getText(), Toast.LENGTH_LONG);
            //PopUpMensaje2.show();

            Snackbar.make(coordinatorLayoutView, "Las contraseñas no coinciden.", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(Color.CYAN)
                    //Color del texto de la acción
                    .setActionTextColor(Color.parseColor("#FFC107"))
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            txtRePassword.requestFocus();
                            InputMethodManager IMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            IMM.showSoftInput(txtRePassword, InputMethodManager.SHOW_IMPLICIT);
                        }
                    })
                    .show();
        }
    }

    private class ProcesoSegundoPlano extends AsyncTask<Void, Void, Void> {

        ProgressDialog progress;
        actRegistration act;

        public ProcesoSegundoPlano(ProgressDialog progress, actRegistration act) {
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
            }, 1000);

        }

        protected Void doInBackground(Void... params) {
            //realizar la operación aquí
            GuardarDatos();
            return null;
        }

    }

    private void GuardarDatos()
    {
        //1- Envvio los datos al servidor para verificar si el usuario ya existe

        final EditText txtName = (EditText) findViewById(R.id.txtName);
        final EditText txtLastname = (EditText) findViewById(R.id.txtLastname);
        final TextView txtAreaCode = (TextView) findViewById(R.id.txtAreaCode);
        final EditText txtPhone = (EditText) findViewById(R.id.txtPhone);
        final EditText txtEmail = (EditText) findViewById(R.id.txtEmail);
        final EditText txtPassword = (EditText) findViewById(R.id.txtPassword);
        final EditText txtRePassword = (EditText) findViewById(R.id.txtRePassword);

        final View coordinatorLayoutView = findViewById(R.id.snackbarPosition);

        //Busco la direccion de correo entre los usarios registrados
        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this);

        //Con esta URL verifico si se han creado registros con este usuario.
        String urlUser = "http://app.towpod.net/ws_get_registered_customer.php?usr="+txtEmail.getText();
        urlUser = urlUser.replaceAll(" ", "%20");

        //Toast PopUpMensaje = Toast.makeText(actMainLogin.this, txtUserName.getText(), Toast.LENGTH_LONG);
        //PopUpMensaje.show();

        //Creamos la petición
        JsonArrayRequest requestUser = new JsonArrayRequest(urlUser,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() > 0)
                        {
                            Button btnRegister  = (Button) findViewById(R.id.btnRegister);
                            btnRegister.setEnabled(true);

                            Snackbar.make(coordinatorLayoutView, "La dirección de correo ya está siendo utilizada por otro usuario.", Snackbar.LENGTH_LONG)
                                    //.setActionTextColor(Color.CYAN)
                                    //Color del texto de la acción
                                    .setActionTextColor(Color.parseColor("#FFC107"))
                                    .setAction("Aceptar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            txtEmail.requestFocus();
                                            InputMethodManager IMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            IMM.showSoftInput(txtEmail, InputMethodManager.SHOW_IMPLICIT);
                                        }
                                    })
                                    .show();
                            return;
                        }
                        else
                        {



                            //==============================================================
                            //=====ACCEDIENDO AL GPS========================================
                            clsUbicacion MiUbicacion;
                            MiUbicacion = new clsUbicacion(actRegistration.this);

                            MiUbicacion.getLocation();
                            //new AsyncTaskGetLocation(MiUbicacion).execute();
                            //==============================================================

                            //Si NO EXISTE LOCALMENTE NI EN LA WEB PROCEDO A GUARDAR LOS DATOS.
                            //URL para guardar datos
                            String url = "http://app.towpod.net/ws_add_customer.php?" +
                                    "name="+ txtName.getText().toString().trim() +
                                    "&lastname="+ txtLastname.getText().toString().trim() +
                                    "&mail="+txtEmail.getText().toString().trim() +
                                    "&phone="+txtPhone.getText().toString().trim() +
                                    "&country_area_code="+txtAreaCode.getText().toString().trim()+
                                    "&state="+MiUbicacion.Estado +
                                    "&city="+MiUbicacion.Ciudad +
                                    "&pwd="+txtPassword.getText();

                            url = url.replaceAll(" ", "%20");

                            //Creamos la petición
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

                            RegistrarLocalmente(txtEmail.getText().toString(), txtRePassword.getText().toString());

                            actRegistration.this.finish();
                            //Abro la actividad del login
                            Intent actMainLogin = new Intent(actRegistration.this, actMainLogin.class);
                            startActivity(actMainLogin);

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

    public void RegistrarLocalmente(final String strUserMail, final String strPassword)
    {

        //Busco la direccion de correo asociada al telefono entre los usarios registrados
        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this);

        //Con esta URL verifico si se han creado registros con este usuario.
        String urlUser = "http://app.towpod.net/ws_get_registered_customer.php?usr="+strUserMail;
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
                                //Abrimos la base de datos 'DBUsuarios' en modo escritura
                                cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actRegistration.this, "USUARIOSHM", null, 2);
                                SQLiteDatabase db = usdbh.getWritableDatabase();
                                //Elimino los registros locales para asgurarme de que solo existe un registro en la tabla
                                //Eliminar un registro
                                db.execSQL("DELETE FROM USUARIO");

                                //Inserto los datos del usuario en la BDD local para MANTENER ACTIVA LA SESIÓN
                                db.execSQL("INSERT INTO USUARIO (user_id, user_email, user_password, user_keep_session) VALUES (" + response.getJSONObject(0).getInt("customer_id") + ", '" + strUserMail + "', '" + strPassword + "', 1)");

                                Toast msgSuccess = Toast.makeText(actRegistration.this, "Tu registro se realizó con éxito." , Toast.LENGTH_LONG);
                                msgSuccess.show();

                                actRegistration.this.finish();
                                //Abro la actividad del login
                                Intent actMainLogin = new Intent(actRegistration.this, actMainLogin.class);
                                startActivity(actMainLogin);

                                return;

                            }
                            catch (JSONException e)
                            {
                                Toast popUpError = Toast.makeText(actRegistration.this, e.getMessage() , Toast.LENGTH_LONG);
                                popUpError.show();
                            }
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

    //Le pone una política de 4 reintentos con 4 segundos entre cada uno y la devuelve.
    public Request setRetryPolicy(Request request){
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }

    private boolean CampoVacio(EditText MyEditText)
    {
        return MyEditText.getText().toString().trim().length() == 0;
    }
}
