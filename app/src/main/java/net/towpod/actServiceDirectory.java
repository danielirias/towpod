package net.towpod;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
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

public class actServiceDirectory extends AppCompatActivity {

    GridView ListaCategorias;
    ArrayAdapter AdaptadorCategorias;

    public static String EXTRA_ID_CATEGORIA = "extra.id.categoria";
    public static String EXTRA_NOMBRE_CATEGORIA = "extra.nombre.categoria";

    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case android.R.id.home:
                DrawerLayout drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            //...
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_service_directory);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Realizo el proceo en segundo plano y muestro el cuadro de espera
        ProgressDialog VentanaEspera = new ProgressDialog(this);
        VentanaEspera.setTitle("Obteniendo datos");
        VentanaEspera.setMessage("Espere un momento, por favor...");
        new actServiceDirectory.ProcesoSegundoPlano(VentanaEspera, this).execute();

    }

    private class ProcesoSegundoPlano extends AsyncTask<Void, Void, Void>
    {

        ProgressDialog progress;
        actServiceDirectory act;

        public ProcesoSegundoPlano(ProgressDialog progress, actServiceDirectory act) {
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
            VerificarUsuario();
            return null;
        }

    }

    public void VerificarUsuario()
    {
        try
        {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actServiceDirectory.this, "USUARIOSHM", null, 2);
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
                        Intent actMainLogin = new Intent(actServiceDirectory.this, actMainLogin.class);
                        startActivity(actMainLogin);
                        return;

                    }
                    else
                    {
                        //txtMainTitle.setText(CursorDB.getString(2));

                        //Si el USUARIO EXISTE LOCALMENTE voy a buscarlo al servidor web
                        //Busco la direccion de correo entre los usarios registrados
                        //Obtenemos la instancia única de la cola de peticiones
                        requestQueue = RequestQueueSingleton.getRequestQueue(this);

                        //Con esta URL verifico si se han creado registros con este usuario.
                        String urlUser = "http://app.towpod.net/ws_get_login_data_customer.php?usr="+CursorDB.getString(1)+"&pwd="+CursorDB.getString(2);
                        urlUser = urlUser.replaceAll(" ", "%20");

                        //Creamos la petición
                        JsonArrayRequest requestUser = new JsonArrayRequest(urlUser,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        if (response.length() > 0)
                                        {

                                            CargarCategorias();

                                        }
                                        else
                                        {
                                            //Si NO EXISTE LOCALMENTE NI EN LA WEB lo envio al Login para que inicie sesion o se registre
                                            Intent actMainLogin = new Intent(actServiceDirectory.this, actMainLogin.class);
                                            startActivity(actMainLogin);
                                            return;
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        RelativeLayout loNoSignal = (RelativeLayout) findViewById(R.id.loNoSignal);
                                        LinearLayout.LayoutParams medidasNoSignal = (LinearLayout.LayoutParams) loNoSignal.getLayoutParams();
                                        medidasNoSignal.height = 0;
                                        loNoSignal.setLayoutParams(medidasNoSignal);

                                        LinearLayout loCategorias = (LinearLayout) findViewById(R.id.loCategorias);
                                        LinearLayout.LayoutParams medidasCategorias = (LinearLayout.LayoutParams) loCategorias.getLayoutParams();
                                        medidasCategorias.height = medidasNoSignal.MATCH_PARENT;
                                        loCategorias.setLayoutParams(medidasCategorias);

                                        medidasNoSignal.height = medidasNoSignal.MATCH_PARENT;
                                        loNoSignal.setLayoutParams(medidasNoSignal);

                                        medidasCategorias.height = 0;
                                        loCategorias.setLayoutParams(medidasCategorias);
                                        //Si recibimos un error, mostraremos la causa
                                        Toast PopUpMensaje = Toast.makeText(actServiceDirectory.this, "ERRRROO"+error.getMessage(), Toast.LENGTH_LONG);
                                        //PopUpMensaje.show();
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
                    Toast popUpError = Toast.makeText(actServiceDirectory.this,  e.getMessage(), Toast.LENGTH_LONG);
                    //popUpError.show();
                }


                //db.execSQL("INSERT INTO USUARIO (user_email, user_password) VALUES ('" + txtUserName.getText() + "', '" + txtPassword.getText() +"')");

                //Cerramos la base de datos
                db.close();

            }
            else
            {
                Toast popUpError = Toast.makeText(actServiceDirectory.this,  "BDD inaccesible", Toast.LENGTH_LONG);
                //popUpError.show();
            }
        }
        catch (Exception e)
        {

            return;
        }
    }

    public void CargarCategorias()
    {

        //======================================================================
        //Cargar las categorias
        //======================================================================
        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this);

        //Instancia del GridView
        ListaCategorias = (GridView)findViewById(R.id.ListViewCategorias);

        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this);

        //URL de las categorías
        String url = "http://app.towpod.net/ws_get_category.php";
        url = url.replaceAll(" ", "%20");

        //Inicializar el adaptador con la fuente de datos
        List<clsCategoria> clsCategorias = new ArrayList<clsCategoria>();
        AdaptadorCategorias = new clsCategoriaArrayAdapter(this, clsCategorias);
        AdaptadorCategorias.clear();

        //Creamos la petición
        try
        {

        }
        catch (Exception e){

        }
        JsonArrayRequest request = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response)
                    {

                        RelativeLayout loNoSignal = (RelativeLayout) findViewById(R.id.loNoSignal);
                        LinearLayout.LayoutParams medidasNoSignal = (LinearLayout.LayoutParams) loNoSignal.getLayoutParams();
                        medidasNoSignal.height = 0;
                        loNoSignal.setLayoutParams(medidasNoSignal);

                        LinearLayout loCategorias = (LinearLayout) findViewById(R.id.loCategorias);
                        LinearLayout.LayoutParams medidasCategorias = (LinearLayout.LayoutParams) loCategorias.getLayoutParams();
                        medidasCategorias.height = medidasNoSignal.MATCH_PARENT;
                        loCategorias.setLayoutParams(medidasCategorias);


                        for (int i = 0; i < response.length(); i++){
                            try
                            {
                                AdaptadorCategorias.add(new clsCategoria(
                                        response.getJSONObject(i).getString("category_id"),
                                        response.getJSONObject(i).getString("category_name"),
                                        response.getJSONObject(i).getString("category_color"),
                                        response.getJSONObject(i).getString("category_icon")));

                            }
                            catch (JSONException e) {

                                medidasNoSignal.height = medidasNoSignal.MATCH_PARENT;
                                loNoSignal.setLayoutParams(medidasNoSignal);

                                medidasCategorias.height = 0;
                                loCategorias.setLayoutParams(medidasCategorias);

                                Toast popUpError = Toast.makeText(actServiceDirectory.this, e.toString() , Toast.LENGTH_LONG);
                                //popUpError.show();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        //Si recibimos un error, mostraremos la causa

                        RelativeLayout loNoSignal = (RelativeLayout) findViewById(R.id.loNoSignal);
                        LinearLayout.LayoutParams medidasNoSignal = (LinearLayout.LayoutParams) loNoSignal.getLayoutParams();
                        medidasNoSignal.height = medidasNoSignal.MATCH_PARENT;
                        loNoSignal.setLayoutParams(medidasNoSignal);

                        LinearLayout loCategorias = (LinearLayout) findViewById(R.id.loCategorias);
                        LinearLayout.LayoutParams medidasCategorias = (LinearLayout.LayoutParams) loCategorias.getLayoutParams();
                        medidasCategorias.height = 0;
                        loCategorias.setLayoutParams(medidasCategorias);

                        Toast popUpError = Toast.makeText(actServiceDirectory.this, error.getMessage() , Toast.LENGTH_LONG);
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
        AdaptadorCategorias = new clsCategoriaArrayAdapter(this, clsCategorias);

        //Relacionando la lista con el adaptador
        ListaCategorias.setAdapter(AdaptadorCategorias);

        //Creo el evento Clic para cada objeto de la lista
        ListaCategorias.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {
                clsCategoria CategoriaActual = (clsCategoria) pariente.getItemAtPosition(posicion);
                CharSequence texto = "Seleccionado: " + CategoriaActual.getIdCategoria() + "-" + CategoriaActual.getNombreCategoria();
                Toast toast = Toast.makeText(actServiceDirectory.this, texto, Toast.LENGTH_SHORT);
                //toast.show();

                Intent actListaProveedores = new Intent(actServiceDirectory.this, actListaProveedores.class);
                actListaProveedores.putExtra(EXTRA_ID_CATEGORIA, CategoriaActual.getIdCategoria());
                actListaProveedores.putExtra(EXTRA_NOMBRE_CATEGORIA, CategoriaActual.getNombreCategoria());
                startActivity(actListaProveedores);

            }
        });
        //======================================================================
    }

    //Le pone una política de 4 reintentos con 4 segundos entre cada uno y la devuelve.
    public Request setRetryPolicy(Request request)
    {
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }

    private Account getAccount(AccountManager accountManager)
    {
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account;
        if (accounts.length > 0)
        {
            account = accounts[0];
        }
        else
        {
            account = null;
        }
        return account;
    }

    public void SolicitarAsistencia(View v)
    {
        //Verifico que tenga vehiculos registrados
        try {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actServiceDirectory.this, "USUARIOSHM", null, 2);
            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Si hemos abierto correctamente la base de datos
            if (db != null) {
                String[] campos = new String[]{"user_id", "user_email", "user_password", "user_keep_session"};
                final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

                try {
                    CursorDB.moveToFirst();
                    if (CursorDB.getCount() == 0) {
                        //Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
                        Intent actMainLogin = new Intent(actServiceDirectory.this, actMainLogin.class);
                        startActivity(actMainLogin);
                        return;

                    }
                    else
                    {
                        //Obtenemos la instancia única de la cola de peticiones
                        requestQueue = RequestQueueSingleton.getRequestQueue(actServiceDirectory.this);
                        //URL del detalle del proveedor
                        String urlAuto = "http://app.towpod.net/ws_get_customer_car.php?idc=" + CursorDB.getString(0);
                        urlAuto = urlAuto.replaceAll(" ", "%20");

                        JsonArrayRequest requestAuto = new JsonArrayRequest(urlAuto,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray responseAuto) {

                                        if (responseAuto.length() >0 )
                                        {
                                            try
                                            {
                                                responseAuto.getJSONObject(0).getString("car_id");
                                                //MUESTRO LA DESCRIPCIÓN DEL PROBLEMA
                                                Intent actStep1 = new Intent(actServiceDirectory.this, actGetHelp.class);
                                                //Intent actStep1 = new Intent(actServiceDirectory.this, actTestLocation.class);
                                                startActivity(actStep1);
                                            }
                                            catch (JSONException e)
                                            {
                                                Toast popUpError = Toast.makeText(actServiceDirectory.this, e.getMessage(), Toast.LENGTH_LONG);
                                                popUpError.show();
                                            }
                                        }
                                        else
                                        {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(actServiceDirectory.this);
                                            LayoutInflater inflater = actServiceDirectory.this.getLayoutInflater();
                                            //builder.setView(inflater.inflate(R.layout.dialog_select_my_policy, null));

                                            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
                                            {
                                                public void onClick(DialogInterface dialog, int id)
                                                {

                                                }
                                            });

                                            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                            {
                                                public void onClick(DialogInterface dialog, int id)
                                                {
                                                    Intent actAddNewCar = new Intent(actServiceDirectory.this, actAddNewCar.class);
                                                    //Intent actStep1 = new Intent(actServiceDirectory.this, actTestLocation.class);
                                                    startActivity(actAddNewCar);
                                                }
                                            });

                                            AlertDialog alertDialogConfirmacion = builder.create();
                                            alertDialogConfirmacion.setTitle("Solicitud de asistencia");
                                            alertDialogConfirmacion.setMessage("Necesitas registrar un vehículo antes de solicitar asistencia.");
                                            alertDialogConfirmacion.show();
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
                } catch (Exception e) {
                    Toast popUpError = Toast.makeText(actServiceDirectory.this, e.getMessage(), Toast.LENGTH_LONG);
                    popUpError.show();
                }

                //Cerramos la base de datos
                db.close();

            }
        } catch (Exception e) {
            return;
        }
    }

    public void reiniciarActividad(View view)
    {
        this.recreate();
    }
}
