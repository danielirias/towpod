package net.towpod;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.math.BigDecimal;


public class actMainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_FOR_DIRECTORY = 101;
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
        setContentView(R.layout.act_main_activity_navigation_drawer);

        //=============================================================================
        //ASIGNO EL CONTENIDO QUE DESEO MOSTRAR
        //Drawer
        //DrawerLayout Contenedor = (DrawerLayout)findViewById(R.id.drawer_layout);
        //Contenido de la Activity
        //View loContenido = getLayoutInflater().inflate(R.layout.act_main_activity, null);
        //drawerLayout.addView(loContenido);
        //=============================================================================


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Realizo el proceo en segundo plano y muestro el cuadro de espera
        ProgressDialog VentanaEspera = new ProgressDialog(this);
        VentanaEspera.setTitle("Obteniendo datos");
        VentanaEspera.setMessage("Espere un momento, por favor...");
        new actMainActivity.ProcesoSegundoPlano(VentanaEspera, this).execute();




        final DrawerLayout drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        NavigationView navView = (NavigationView)drawerLayout.findViewById(R.id.nav_view);

        navView.setNavigationItemSelectedListener
        (
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    switch (menuItem.getItemId())
                    {
                        case R.id.menu_service_directory:
                            //============================================================================================================================================
                            // Verifico si se tiene acceso a la ubicación. Este es un permiso calificado como riesgoso por Google y debo solicitarlo al usuario
                            //SI NO TIENE PERMISOS
                            if (ContextCompat.checkSelfPermission(actMainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                            {

                                //Indíco y explico al ususario por qué necesita el permiso
                                if (ActivityCompat.shouldShowRequestPermissionRationale(actMainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                                    // Show an expanation to the user *asynchronously* -- don't block
                                    // this thread waiting for the user's response! After the user
                                    // sees the explanation, try again to request the permission.
                                    AlertDialog.Builder builder = new AlertDialog.Builder(actMainActivity.this);
                                    LayoutInflater inflater = actMainActivity.this.getLayoutInflater();
                                    //builder.setView(inflater.inflate(R.layout.dialog_select_my_policy, null));

                                    builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id)
                                        {

                                        }
                                    });

                                    builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                            ActivityCompat.requestPermissions(actMainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_FOR_DIRECTORY);
                                        }
                                    });

                                    AlertDialog alertDialogConfirmacion = builder.create();
                                    alertDialogConfirmacion.setTitle("Acceso a la ubicación");
                                    alertDialogConfirmacion.setMessage("Towpod necesita acceder a tu ubicación para poder brindarte la asistencia que buscas.");
                                    alertDialogConfirmacion.show();

                                }
                                else
                                {

                                    // No explanation needed, we can request the permission.

                                    ActivityCompat.requestPermissions(actMainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an app-defined int constant. The callback method gets the result of the request.
                                }
                            }
                            else
                            {
                                //Si tiene los permisos
                                Intent actServiceDirectory = new Intent(actMainActivity.this, actServiceDirectory.class);
                                startActivity(actServiceDirectory);
                            }
                            //============================================================================================================================================

                            break;

                        case R.id.menu_my_account_policy:
                            Intent actMyAccountPolicy = new Intent(actMainActivity.this, actMyAccountPolicy.class);
                            startActivity(actMyAccountPolicy);
                            break;

                        case R.id.menu_my_account_cars:
                            Intent actMyAccountCars = new Intent(actMainActivity.this, actMyAccountCars.class);
                            startActivity(actMyAccountCars);
                            break;

                        case R.id.menu_my_account_all_cards:
                            Intent actMyAccountMyCards = new Intent(actMainActivity.this, actMyAccountMyCards.class);
                            startActivity(actMyAccountMyCards);
                            break;

                        case R.id.menu_my_account_history:
                            Intent actMyAccountHistory = new Intent(actMainActivity.this, actMyAccountHistory.class);
                            startActivity(actMyAccountHistory);
                            break;
                        case R.id.menu_my_account_parts_history:
                            Intent actQuotationList = new Intent(actMainActivity.this, actQuotationList.class);
                            startActivity(actQuotationList);
                            break;

                        case R.id.menu_my_account_my_record:
                            Intent actRegistros = new Intent(actMainActivity.this, actMyAccountStores.class);
                            startActivity(actRegistros);
                            break;

                        case R.id.menu_my_account_logout:
                            AlertDialog.Builder DialogLogOut = new AlertDialog.Builder(actMainActivity.this);
                            DialogLogOut.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    //Sin acciones para ejecutar. Solo se cierra la ventana de dialogo.
                                }
                            });

                            DialogLogOut.setPositiveButton("Cerrar sesión", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    //Abrimos la base de datos 'DBUsuarios' en modo escritura
                                    cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actMainActivity.this, "USUARIOSHM", null, 2);
                                    SQLiteDatabase db = usdbh.getWritableDatabase();
                                    //Elimino los registros locales para asgurarme de que solo existe un registro en la tabla
                                    //Eliminar un registro
                                    db.execSQL("DELETE FROM USUARIO");

                                    actMainActivity.this.finish();
                                    //Abro la actividad login
                                    Intent actMain = new Intent(actMainActivity.this, actMainLogin.class);
                                    startActivity(actMain);
                                }
                            });

                            AlertDialog dialog = DialogLogOut.create();
                            dialog.setTitle("Cerrar sesión");
                            dialog.setMessage("Deberás escribir tu correo y tu contraseña para poder ingresar nuevamente a esta App.");
                            dialog.show();
                            break;

                        case R.id.menu_help_law:
                            Intent actLaw = new Intent(actMainActivity.this, actLaw.class);
                            startActivity(actLaw);
                            break;

                        case R.id.menu_help_about:
                            Intent actAbout = new Intent(actMainActivity.this, actAbout.class);
                            startActivity(actAbout);
                            break;
                    }

                    drawerLayout.closeDrawers();

                    return true;
                }
            }
        );



	    Button btnGetHelp = (Button) findViewById(R.id.btnGetHelp);
	    btnGetHelp.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View view) {
			    VerificarPermisoUbicacion();
		    }
	    });

        Button btnPay = (Button) findViewById(R.id.btnPay);
        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //Enviar parametros a Paypal e iniciar su servicio
                Intent paypalActivity = new Intent(actMainActivity.this, actPaypalPayment.class);
                startActivity(paypalActivity);

            }
        });


    }

    private class ProcesoSegundoPlano extends AsyncTask<Void, Void, Void>
    {

        ProgressDialog progress;
        actMainActivity act;

        public ProcesoSegundoPlano(ProgressDialog progress, actMainActivity act) {
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
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actMainActivity.this, "USUARIOSHM", null, 2);
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
                        Intent actMainLogin = new Intent(actMainActivity.this, actMainLogin.class);
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
                                            TextView lblUserName = (TextView) findViewById(R.id.lblDrawerUserName);
                                            lblUserName.setText(CursorDB.getString(0));

                                            TextView lblUserMail = (TextView) findViewById(R.id.lblDrawerUserMail);
                                            lblUserMail.setText(CursorDB.getString(1));

                                        }
                                        else
                                        {
                                            //Si NO EXISTE LOCALMENTE NI EN LA WEB lo envio al Login para que inicie sesion o se registre
                                            Intent actMainLogin = new Intent(actMainActivity.this, actMainLogin.class);
                                            startActivity(actMainLogin);
                                            return;
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error)
                                    {
                                        Toast PopUpMensaje = Toast.makeText(actMainActivity.this, error.getMessage(), Toast.LENGTH_LONG);
                                        //PopUpMensaje.show();

                                        RelativeLayout loCategorias = (RelativeLayout) findViewById(R.id.loCategorias);
                                        loCategorias.setVisibility(View.GONE);

                                        RelativeLayout loNoSignal = (RelativeLayout) findViewById(R.id.loNoSignal);
                                        loNoSignal.setVisibility(View.VISIBLE);

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
                    Toast popUpError = Toast.makeText(actMainActivity.this,  e.getMessage(), Toast.LENGTH_LONG);
                    //popUpError.show();
                }


                //db.execSQL("INSERT INTO USUARIO (user_email, user_password) VALUES ('" + txtUserName.getText() + "', '" + txtPassword.getText() +"')");

                //Cerramos la base de datos
                db.close();

            }
            else
            {
                Toast popUpError = Toast.makeText(actMainActivity.this,  "BDD inaccesible", Toast.LENGTH_LONG);
                //popUpError.show();
            }
        }
        catch (Exception e)
        {

            return;
        }
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


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    CargarCategoriasDeAsistencia();
                   

                }
                else
                {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_FOR_DIRECTORY:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    Intent actServiceDirectory = new Intent(actMainActivity.this, actServiceDirectory.class);
                    startActivity(actServiceDirectory);

                }
                else
                {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void VerificarPermisoUbicacion()
    {
        //============================================================================================================================================
        // Verifico si se tiene acceso a la ubicación. Este es un permiso calificado como riesgoso por Google y debo solicitarlo al usuario
        //SI NO TIENE PERMISOS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            //Indíco y explico al ususario por qué necesita el permiso
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder builder = new AlertDialog.Builder(actMainActivity.this);
                LayoutInflater inflater = actMainActivity.this.getLayoutInflater();
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
                        ActivityCompat.requestPermissions(actMainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }
                });

                AlertDialog alertDialogConfirmacion = builder.create();
                alertDialogConfirmacion.setTitle("Acceso a la ubicación");
                alertDialogConfirmacion.setMessage("Towpod necesita acceder a tu ubicación para poder brindarte la asistencia que buscas.");
                alertDialogConfirmacion.show();

            }
            else
            {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an app-defined int constant. The callback method gets the result of the request.
            }
        }
        else
        {
            //Si tiene los permisos
            CargarCategoriasDeAsistencia();
        }
        //============================================================================================================================================


    }

    public void CargarCategoriasDeAsistencia()
    {
        //Verifico que tenga vehiculos registrados
        try {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actMainActivity.this, "USUARIOSHM", null, 2);
            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Si hemos abierto correctamente la base de datos
            if (db != null) {
                String[] campos = new String[]{"user_id", "user_email", "user_password", "user_keep_session"};
                final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

                try {
                    CursorDB.moveToFirst();
                    if (CursorDB.getCount() == 0) {
                        //Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
                        Intent actMainLogin = new Intent(actMainActivity.this, actMainLogin.class);
                        startActivity(actMainLogin);
                        return;

                    }
                    else
                    {
                        //Obtenemos la instancia única de la cola de peticiones
                        requestQueue = RequestQueueSingleton.getRequestQueue(actMainActivity.this);
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
                                                Intent actStep1 = new Intent(actMainActivity.this, actGetHelp.class);
                                                //Intent actStep1 = new Intent(actMainActivity.this, actTestLocation.class);
                                                startActivity(actStep1);
                                            }
                                            catch (JSONException e)
                                            {
                                                Toast popUpError = Toast.makeText(actMainActivity.this, e.getMessage(), Toast.LENGTH_LONG);
                                                popUpError.show();
                                            }
                                        }
                                        else
                                        {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(actMainActivity.this);
                                            LayoutInflater inflater = actMainActivity.this.getLayoutInflater();
                                            //builder.setView(inflater.inflate(R.layout.dialog_select_my_policy, null));

                                            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id)
                                                {

                                                }
                                            });

                                            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id)
                                                {
                                                    Intent actAddNewCar = new Intent(actMainActivity.this, actAddNewCar.class);
                                                    //Intent actStep1 = new Intent(actMainActivity.this, actTestLocation.class);
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
                    Toast popUpError = Toast.makeText(actMainActivity.this, e.getMessage(), Toast.LENGTH_LONG);
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

    public void TaxiRequest(View view)
    {
        Intent actCurrentLocation = new Intent(actMainActivity.this, actCurrentLocation.class);
        startActivity(actCurrentLocation);
    }
}
