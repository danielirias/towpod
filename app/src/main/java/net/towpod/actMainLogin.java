package net.towpod;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;

public class actMainLogin extends AppCompatActivity  {

    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    public static String SESSION_USER_MAIL = "";


    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(refreshedToken);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main_login);

        final EditText txtUserName = (EditText) findViewById(R.id.txtUserName);
        final EditText txtPassword = (EditText) findViewById(R.id.txtPassword);

        VerificarUsuario();

    }

    public void VerificarUsuario()
    {
        try
        {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actMainLogin.this, "USUARIOSHM", null, 2);
            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Si hemos abierto correctamente la base de datos
            if(db != null)
            {
                String[] campos = new String[] {"user_name", "user_email", "user_password", "user_keep_session"};
                final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

                try
                {
                    CursorDB.moveToFirst();
                    if (CursorDB.getCount() == 0)
                    {
                        //Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
                        //Intent actMainLogin = new Intent(actMainLogin.this, actMainLogin.class);
                        //startActivity(actMainLogin);
                        return;
                    }
                    else
                    {
                        actMainLogin.this.finish();
                        Intent actMainActivity = new Intent(actMainLogin.this, actMainActivity.class);
                        startActivity(actMainActivity);
                    }
                }
                catch (Exception e)
                {
                    Toast popUpError = Toast.makeText(actMainLogin.this,  e.getMessage(), Toast.LENGTH_LONG);
                    popUpError.show();
                }


                //db.execSQL("INSERT INTO USUARIO (user_email, user_password) VALUES ('" + txtUserName.getText() + "', '" + txtPassword.getText() +"')");

                //Cerramos la base de datos
                db.close();

            }
            else
            {
                Toast popUpError = Toast.makeText(actMainLogin.this,  "BDD inaccesible", Toast.LENGTH_LONG);
                popUpError.show();
            }
        }
        catch (Exception e)
        {

            return;
        }
    }

    public void SolicitarAcceso(View v)
    {
        //Realizo el proceo en segundo plano y muestro el cuadro de espera
        ProgressDialog VentanaEspera = new ProgressDialog(this);
        VentanaEspera.setTitle("Obteniendo datos");
        VentanaEspera.setMessage("Espere un momento, por favor...");
        new actMainLogin.ProcesoSegundoPlano(VentanaEspera, this).execute();
    }

    private class ProcesoSegundoPlano extends AsyncTask<Void, Void, Void>
    {

        ProgressDialog progress;
        actMainLogin act;

        public ProcesoSegundoPlano(ProgressDialog progress, actMainLogin act) {
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
            progress.dismiss();

        }

        protected Void doInBackground(Void... params) {
            //realizar la operación aquí
            RegistrarUsuarioLocal();
            return null;
        }

    }

    private void RegistrarUsuarioLocal()
    {
        try
        {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actMainLogin.this, "USUARIOSHM", null, 2);
            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Si hemos abierto correctamente la base de datos
            if(db != null)
            {
                try
                {
                    //Compruebo que el ususario exista en el servidor WEB
                    BuscarUsuario();
                    return;
                }
                catch (Exception e)
                {
                    Toast popUpError = Toast.makeText(actMainLogin.this,  e.getMessage(), Toast.LENGTH_LONG);
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

    private void BuscarUsuario()
    {
        final EditText txtUserName = (EditText) findViewById(R.id.txtUserName);
        final EditText txtPassword = (EditText) findViewById(R.id.txtPassword);


        final View coordinatorLayoutView = findViewById(R.id.snackbarPosition);
        final InputMethodManager IMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (CampoVacio(txtUserName) == true)
        {
            Snackbar.make(coordinatorLayoutView, "Escribe tu dirección de correo.", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(Color.CYAN)
                    //Color del texto de la acción
                    .setActionTextColor(Color.parseColor("#FFC107"))
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            txtUserName.requestFocus();
                            IMM.showSoftInput(txtUserName, InputMethodManager.SHOW_IMPLICIT);
                        }
                    })
                    .show();
            return;
        }

        if (CampoVacio(txtPassword) == true)
        {
            Snackbar.make(coordinatorLayoutView, "Escribe tu contraseña.", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(Color.CYAN)
                    //Color del texto de la acción
                    .setActionTextColor(Color.parseColor("#FFC107"))
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            txtPassword.requestFocus();
                            IMM.showSoftInput(txtPassword, InputMethodManager.SHOW_IMPLICIT);
                        }
                    })
                    .show();
            return;
        }

        View view = this.getCurrentFocus();
        IMM.hideSoftInputFromWindow(view.getWindowToken(), 0);

        //Verifico si hay Internet
        //Toast PopUpMensaje = Toast.makeText(actMainLogin.this, Boolean.toString(isOnlineNet()), Toast.LENGTH_LONG);
        //PopUpMensaje.show();

        //Si hay datos para buscar procedo a realizar la consutla al servidor:

        //Busco la direccion de correo asociada al telefono entre los usarios registrados
        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this);

        //Con esta URL verifico si se han creado registros con este usuario.
        String urlUser = "http://app.towpod.net/ws_get_login_data_customer.php?usr="+txtUserName.getText()+"&pwd="+txtPassword.getText();
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
                            try
                            {
                                SESSION_USER_MAIL = response.getJSONObject(0).getString("customer_email");

                                CheckBox chkKeepSession = (CheckBox) findViewById(R.id.chkKeepSession);

                                //Abrimos la base de datos 'DBUsuarios' en modo escritura
                                cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actMainLogin.this, "USUARIOSHM", null, 2);
                                SQLiteDatabase db = usdbh.getWritableDatabase();
                                //Elimino los registros locales para asgurarme de que solo existe un registro en la tabla
                                //Eliminar un registro
                                db.execSQL("DELETE FROM USUARIO");

                                String strCustomerName = response.getJSONObject(0).getString("customer_name") + " " + response.getJSONObject(0).getString("customer_lastname");

                                if (chkKeepSession.isChecked())
                                {
                                    txtUserName.setEnabled(false);
                                    txtPassword.setEnabled(false);

                                    //Inserto los datos del usuario en la BDD local para MANTENER ACTIVA LA SESIÓN
                                    db.execSQL("INSERT INTO USUARIO (user_id, user_name, user_email, user_password, user_keep_session) VALUES (" + response.getJSONObject(0).getInt("customer_id") + ", '" + strCustomerName + "', '" + txtUserName.getText() + "', '" + txtPassword.getText() +"', 1)");

                                    //Abro la actividad principal
                                    actMainLogin.this.finish();
                                    Intent actMain = new Intent(actMainLogin.this, actMainActivity.class);
                                    startActivity(actMain);
                                    return;
                                }
                                else
                                {
                                    txtUserName.setEnabled(false);
                                    txtPassword.setEnabled(false);

                                    //Inserto los datos del usuario en la BDD local
                                    db.execSQL("INSERT INTO USUARIO (user_id, user_name, user_email, user_password, user_keep_session) VALUES (" + response.getJSONObject(0).getInt("customer_id") + ", '" + strCustomerName + "', '" + txtUserName.getText() + "', '" + txtPassword.getText() +"', 0)");

                                    actMainLogin.super.finish();

                                    //Abro la actividad principal
                                    Intent actMain = new Intent(actMainLogin.this, actMainActivity.class);
                                    startActivity(actMain);
                                    return;
                                }

                            }
                            catch (JSONException e)
                            {
                                Toast popUpError = Toast.makeText(actMainLogin.this, e.getMessage() , Toast.LENGTH_LONG);
                                popUpError.show();
                            }
                        }
                        else
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(actMainLogin.this);

                            builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id) {
                                    txtUserName.requestFocus();
                                    InputMethodManager IMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    IMM.showSoftInput(txtUserName, InputMethodManager.SHOW_IMPLICIT);
                                }
                            });

                            AlertDialog alertDialog = builder.create();
                            alertDialog.setTitle("Oh, oh!!");
                            alertDialog.setMessage("Ha ocurrido un error de autenticación.\n\nVerfica que tu correo y contraseña estén bien escritos.");
                            alertDialog.show();

                            /*Snackbar.make(coordinatorLayoutView, "Ha ocurrido un error de autenticación.\nVerifica tu correo y tu contraseña.", Snackbar.LENGTH_LONG)
                                    //.setActionTextColor(Color.CYAN)
                                    //Color del texto de la acción
                                    .setActionTextColor(Color.parseColor("#FFC107"))
                                    .setAction("OK", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            txtUserName.requestFocus();
                                            InputMethodManager IMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            IMM.showSoftInput(txtUserName, InputMethodManager.SHOW_IMPLICIT);
                                        }
                                    })
                                    .show();*/
                            return;
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        MostrarDialogSinConexion();

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

    public void MostrarDialogSinConexion()
    {
        //Si recibimos un error, mostraremos la causa
        AlertDialog.Builder builder = new AlertDialog.Builder(actMainLogin.this);
        builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id) {
                actMainLogin.this.recreate();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setTitle("Oh, oh!!");
        alertDialog.setMessage("Ha ocurrido un problema con el acceso.\n\nVerfica que estés conectado a Internet.");
        alertDialog.show();
    }

    public void AbrirRegistro(View v)
    {
        Intent actRegister = new Intent(actMainLogin.this, actRegistration.class);
        startActivity(actRegister);
    }

    private boolean CampoVacio(EditText myeditText)
    {
        return myeditText.getText().toString().trim().length() == 0;
    }

    public Request setRetryPolicy(Request request)
    {
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }

    public Boolean isOnlineNet()
    {
        try
        {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");
            int val = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }

    public void ResetMyPass (View v)
    {

        Intent navegador = new Intent(Intent.ACTION_VIEW, Uri.parse("http://app.towpod.net/user-reset-pass.php"));
        startActivity(navegador);

    }


}




