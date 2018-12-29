package net.towpod;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
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

import net.towpod.fragment_cuenta.fragment_cuenta_auto;
import net.towpod.fragment_cuenta.fragment_cuenta_poliza;
import net.towpod.fragment_cuenta.fragment_cuenta_tarjeta;

public class actMyAccount extends AppCompatActivity {

    public static String SESSION_USER_MAIL = "";

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    ListView ListaPolizas;
    ArrayAdapter AdaptadorPolizas;
    GridView ListaAutos;
    ArrayAdapter AdaptadorAutos;
    ListView ListaTarjetas;
    ArrayAdapter AdaptadorTarjetas;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {

            case R.id.menuMyRecord:
                Intent actRegistros = new Intent(this, actMyAccountStores.class);
                startActivity(actRegistros);
                return true;

            case R.id.menuAbout:
                Intent actAbout = new Intent(this, actAbout.class);
                startActivity(actAbout);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_my_account);
    }

    protected void onStart()
    {
        super.onStart();

        try {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(this, "USUARIOSHM", null, 2);
            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Si hemos abierto correctamente la base de datos
            if (db != null) {
                String[] campos = new String[]{"user_email", "user_password", "user_keep_session"};
                final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

                try {
                    CursorDB.moveToFirst();
                    if (CursorDB.getCount() == 0) {
                        //Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
                        Intent actMainLogin = new Intent(this, actMainLogin.class);
                        startActivity(actMainLogin);
                        return;

                    } else {
                        SESSION_USER_MAIL = CursorDB.getString(0);
                        CargarInfoUsuario();
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

    public void CargarInfoUsuario()
    {
        final Bundle bundle = new Bundle();

        try
        {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actMyAccount.this, "USUARIOSHM", null, 2);
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
                        Intent actMainLogin = new Intent(actMyAccount.this, actMainLogin.class);
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
                                            try {

                                                bundle.putString("prmNombreAsegurado", response.getJSONObject(0).getString("customer_name").toString() + " " + response.getJSONObject(0).getString("customer_lastname").toString());
                                                TextView txtNombreAsegurado = (TextView) findViewById(R.id.txtNombreProveedor);
                                                txtNombreAsegurado.setText(response.getJSONObject(0).getString("customer_name").toString() + " " + response.getJSONObject(0).getString("customer_lastname").toString());

                                                bundle.putInt("prmIDCustomer", response.getJSONObject(0).getInt("customer_id"));
                                                //TextView txtIDCustomer = (TextView) findViewById(R.id.txtIDCustomer);
                                                //txtIDCustomer.setText(String.valueOf(response.getJSONObject(0).getInt("customer_id")));

                                                CargarPolizas(response.getJSONObject(0).getInt("customer_id"));
                                                CargarAutos(response.getJSONObject(0).getInt("customer_id"));
                                                //CargarTarjetas(response.getJSONObject(0).getInt("customer_id"));

                                            }
                                            catch (JSONException e) {
                                                Toast popUpError = Toast.makeText(actMyAccount.this, e.getMessage(), Toast.LENGTH_LONG);
                                                popUpError.show();
                                            }
                                        }
                                        else
                                        {
                                            //Si NO EXISTE LOCALMENTE NI EN LA WEB lo envio al Login para que inicie sesion o se registre
                                            Intent actMainLogin = new Intent(actMyAccount.this, actMainLogin.class);
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
                    Toast popUpError = Toast.makeText(actMyAccount.this,  e.getMessage(), Toast.LENGTH_LONG);
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


        //============Inicio de construccion de los tabs===================================
        viewPager = (ViewPager) findViewById(R.id.pager);
        actMyAccount.ViewPagerAdapter adapter = new actMyAccount.ViewPagerAdapter(getSupportFragmentManager());


        fragment_cuenta_poliza fragCuentaPolizas = new fragment_cuenta_poliza();
        fragCuentaPolizas.setArguments(bundle);
        adapter.addFrag(fragCuentaPolizas, "Mis pólizas");

        fragment_cuenta_auto fragCuentaAutos = new fragment_cuenta_auto();
        fragCuentaAutos.setArguments(bundle);
        adapter.addFrag(fragCuentaAutos, "Mis autos");

        fragment_cuenta_tarjeta fragCuentaTarjeta = new fragment_cuenta_tarjeta();
        fragCuentaTarjeta.setArguments(bundle);
        adapter.addFrag(fragCuentaTarjeta, "Mis tarjetas");


        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) findViewById(R.id.MyTabs);
        tabLayout.setupWithViewPager(viewPager);//Esto hace que el TabLayout se enlace al viewPager y cambien de Tab al mismo tiempo

        tabLayout.getTabAt(0).setIcon(R.drawable.policy);
        tabLayout.getTabAt(1).setIcon(R.drawable.cariconwhite);
        tabLayout.getTabAt(2).setIcon(R.drawable.creditcard);
        //tabLayout.getTabAt(2).setIcon(R.drawable.map);
        //tabLayout.getTabAt(3).setIcon(R.drawable.chat);

    }

    class ViewPagerAdapter extends FragmentPagerAdapter
    {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private static Account getAccount(AccountManager accountManager)
    {
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account;
        if (accounts.length > 0) {
            account = accounts[0];
        } else {
            account = null;
        }
        return account;
    }

    //Le pone una política de 4 reintentos con 4 segundos entre cada uno y la devuelve.
    public Request setRetryPolicy(Request request)
    {
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }

    public void CargarPolizas(Integer prmIDCustomer)
    {
        //==========================================================================================
        //=======CARGA DE POLIZAS DE LOS USUARIOS===============================================
        //==========================================================================================

        //Instancia del ListView
        ListaPolizas = (ListView) findViewById(R.id.ListViewPolizas);

        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this);

        //Inicializar el adaptador con la fuente de datos
        List<clsPoliza> Polizas = new ArrayList<clsPoliza>();
        AdaptadorPolizas = new clsPolizaArrayAdapter(this, Polizas);
        AdaptadorPolizas.clear();

        //URL del detalle del proveedor
        String urlPoliza = "http://app.towpod.net/ws_get_customer_policy.php?idc=" + prmIDCustomer;
        urlPoliza = urlPoliza.replaceAll(" ", "%20");

        JsonArrayRequest requestPoliza = new JsonArrayRequest(urlPoliza,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray responsePoliza) {
                        for (int i = 0; i < responsePoliza.length(); i++){
                            try
                            {
                                AdaptadorPolizas.add(new clsPoliza(
                                        responsePoliza.getJSONObject(i).getString("car_id"),
                                        responsePoliza.getJSONObject(i).getString("car_brand"),
                                        responsePoliza.getJSONObject(i).getString("car_model"),
                                        responsePoliza.getJSONObject(i).getInt("car_year"),
                                        responsePoliza.getJSONObject(i).getString("policy_number"),
                                        responsePoliza.getJSONObject(i).getString("policy_type_name"),
                                        responsePoliza.getJSONObject(i).getString("policy_type_description"),
                                        responsePoliza.getJSONObject(i).getString("card_number"),
                                        responsePoliza.getJSONObject(i).getDouble("policy_value"),
                                        responsePoliza.getJSONObject(i).getString("bank_name")));
                            }
                            catch (JSONException e)
                            {
                                //Toast popUpError = Toast.makeText(actProviderData.this, e.toString() , Toast.LENGTH_LONG);
                                //popUpError.show();
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
        requestPoliza.setTag(JSON_REQUEST);
        //Al ponerle la política de reintento, le hacemos casting, ya que el metodo recibe un objeto de la clase padre
        requestPoliza = (JsonArrayRequest) setRetryPolicy(requestPoliza);
        //Iniciamos la petición añadiéndola a la cola
        requestQueue.add(requestPoliza);

        //Inicializar el adaptador con la fuente de datos
        AdaptadorPolizas = new clsPolizaArrayAdapter(this, Polizas);

        //Relacionando la lista con el adaptador
        ListaPolizas.setAdapter(AdaptadorPolizas);

        //Creo el evento Clic para cada objeto de la lista
        ListaPolizas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> pariente, View view, int posicion, long id) {
                final clsPoliza polizaSeleccionada = (clsPoliza) pariente.getItemAtPosition(posicion);

                AlertDialog.Builder builder = new AlertDialog.Builder(actMyAccount.this);
                LayoutInflater inflater = actMyAccount.this.getLayoutInflater();
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
                        EliminarPolizaRegistrada(polizaSeleccionada.get_car_id());
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.setTitle("¿Deseas eliminar la póliza de seguro seleccionada?");
                alertDialog.setMessage(polizaSeleccionada.get_policy_name() + "\n \nAsignada a: \n" + polizaSeleccionada.get_car_brand() + ", " + polizaSeleccionada.get_car_model() + " (" + polizaSeleccionada.get_car_year() + ")\n" + polizaSeleccionada.get_car_id());
                alertDialog.show();

                return false;

            }

        });

        //==========================================================================================
        //=======FIN DE CARGA DE POLIZAS DE LOS USUARIOS========================================
        //==========================================================================================
    }

    public void CargarAutos(Integer prmIDCustomer)
    {
        //==========================================================================================
        //=======CARGA DE AUTOS DE LOS USUARIOS===============================================
        //==========================================================================================

        //Instancia del ListView
        ListaAutos = (GridView) findViewById(R.id.ListViewAutos);

        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this);

        //Inicializar el adaptador con la fuente de datos
        List<clsAuto> Autos = new ArrayList<clsAuto>();
        AdaptadorAutos = new clsAutoArrayAdapter(this, Autos);
        AdaptadorAutos.clear();

        //URL del detalle del proveedor
        String urlPoliza = "http://app.towpod.net/ws_get_customer_car.php?idc=" + prmIDCustomer;
        urlPoliza = urlPoliza.replaceAll(" ", "%20");

        JsonArrayRequest requestAuto = new JsonArrayRequest(urlPoliza,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray responseAutos) {
                        for (int i = 0; i < responseAutos.length(); i++){
                            try
                            {
                                AdaptadorAutos.add(new clsAuto(
                                        responseAutos.getJSONObject(i).getString("car_id"),
                                        responseAutos.getJSONObject(i).getString("car_brand"),
                                        responseAutos.getJSONObject(i).getString("car_model"),
                                        responseAutos.getJSONObject(i).getInt("car_year")));
                            }
                            catch (JSONException e)
                            {
                                //Toast popUpError = Toast.makeText(actProviderData.this, e.toString() , Toast.LENGTH_LONG);
                                //popUpError.show();
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
        requestAuto.setTag(JSON_REQUEST);
        //Al ponerle la política de reintento, le hacemos casting, ya que el metodo recibe un objeto de la clase padre
        requestAuto = (JsonArrayRequest) setRetryPolicy(requestAuto);
        //Iniciamos la petición añadiéndola a la cola
        requestQueue.add(requestAuto);

        //Inicializar el adaptador con la fuente de datos
        AdaptadorAutos = new clsAutoArrayAdapter(this, Autos);

        //Relacionando la lista con el adaptador
        ListaAutos.setAdapter(AdaptadorAutos);

        //Creo el evento Clic para cada objeto de la lista
        ListaAutos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> pariente, View view, int posicion, long id) {
                final clsAuto autoSeleccionado = (clsAuto) pariente.getItemAtPosition(posicion);

                AlertDialog.Builder builder = new AlertDialog.Builder(actMyAccount.this);
                LayoutInflater inflater = actMyAccount.this.getLayoutInflater();
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
                        EliminarAutoRegistrado(autoSeleccionado.get_car_id());
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.setTitle("¿Deseas eliminar el vehículo seleccionado?");
                alertDialog.setMessage("Esta acción tambien eliminará cualquier póliza de seguro asignada al vehículo.\n\n" + autoSeleccionado.get_car_brand() + ", " + autoSeleccionado.get_car_model() + " - " + autoSeleccionado.get_car_year() + "\n[" + autoSeleccionado.get_car_id() + "]");
                alertDialog.show();

                return false;

            }

        });


        //==========================================================================================
        //=======FIN DE CARGA DE Autos DE LOS USUARIOS========================================
        //==========================================================================================
    }

    public void CargarTarjetas(Integer prmIDCustomer)
    {
        //==========================================================================================
        //=======CARGA DE Tarjetas DE LOS USUARIOS===============================================
        //==========================================================================================

        //Instancia del ListView
        ListaTarjetas = (ListView) findViewById(R.id.ListViewTarjetas);

        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this);

        //Inicializar el adaptador con la fuente de datos
        List<clsTarjeta> Tarjetas = new ArrayList<clsTarjeta>();
        AdaptadorTarjetas = new clsTarjetaArrayAdapter(this, Tarjetas);
        AdaptadorTarjetas.clear();

        //URL del detalle del proveedor
        String urlTarjeta = "http://app.towpod.net/ws_get_customer_card.php?idc=" + prmIDCustomer;
        urlTarjeta = urlTarjeta.replaceAll(" ", "%20");

        JsonArrayRequest requestTarjeta = new JsonArrayRequest(urlTarjeta,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray responseTarjeta) {
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
        AdaptadorTarjetas = new clsTarjetaArrayAdapter(this, Tarjetas);

        //Relacionando la lista con el adaptador
        ListaTarjetas.setAdapter(AdaptadorTarjetas);

        //==========================================================================================
        //=======FIN DE CARGA DE Tarjetas DE LOS USUARIOS========================================
        //==========================================================================================
    }

    public void ContratarPoliza (View view)
    {
        final View coordinatorLayoutView = findViewById(R.id.snackbarPosition);

        //1 -- Verifico si hay autos registrados

        //Abrimos la base de datos 'DBUsuarios' en modo escritura
        cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actMyAccount.this, "USUARIOSHM", null, 2);
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
                    Intent actMainLogin = new Intent(actMyAccount.this, actMainLogin.class);
                    startActivity(actMainLogin);
                    return;

                }
                else
                {
                    //Obtenemos la instancia única de la cola de peticiones
                    requestQueue = RequestQueueSingleton.getRequestQueue(this);

                    //URL del detalle del proveedor
                    String urlMyCar = "http://app.towpod.net/ws_get_customer_car.php?idc=" + CursorDB.getString(0);
                    urlMyCar = urlMyCar.replaceAll(" ", "%20");

                    JsonArrayRequest requestAuto = new JsonArrayRequest(urlMyCar,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray responseAutos) {
                                    if (responseAutos.length() > 0)
                                    {
                                        try
                                        {
                                            responseAutos.getJSONObject(0).getString("car_id");

                                            // --VERIFICO SI HAY TARJETAS REGISTRADAS
                                            //Obtenemos la instancia única de la cola de peticiones
                                            requestQueue = RequestQueueSingleton.getRequestQueue(actMyAccount.this);

                                            //URL del detalle del proveedor
                                            String urlTarjeta = "http://app.towpod.net/ws_get_customer_card.php?idc=" + CursorDB.getString(0);
                                            urlTarjeta = urlTarjeta.replaceAll(" ", "%20");

                                            JsonArrayRequest requestTarjeta = new JsonArrayRequest(urlTarjeta,
                                                    new Response.Listener<JSONArray>() {
                                                        @Override
                                                        public void onResponse(JSONArray responseTarjeta) {
                                                            if (responseTarjeta.length() > 0)
                                                            {
                                                                try
                                                                {
                                                                    Intent actNuevaPoliza = new Intent(actMyAccount.this, actPolicyType.class);
                                                                    startActivity(actNuevaPoliza);
                                                                    responseTarjeta.getJSONObject(0).getString("card_number");
                                                                }
                                                                catch (JSONException e)
                                                                {
                                                                    Toast popUpError = Toast.makeText(actMyAccount.this, e.toString() , Toast.LENGTH_LONG);
                                                                    popUpError.show();
                                                                }
                                                            }
                                                            else
                                                            {
                                                                Snackbar.make(coordinatorLayoutView, "Debes registrar un método de pago antes de contratar una póliza de seguro.", Snackbar.LENGTH_LONG)
                                                                        //.setActionTextColor(Color.CYAN)
                                                                        //Color del texto de la acción
                                                                        .setActionTextColor(Color.parseColor("#FFC107"))
                                                                        .setAction("Aceptar", new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View view)
                                                                            {
                                                                                Intent actNuevaTarjeta = new Intent(actMyAccount.this, actAddMyCard.class);
                                                                                startActivity(actNuevaTarjeta);
                                                                            }
                                                                        })
                                                                        .show();
                                                                return;
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

                                        }
                                        catch (JSONException e)
                                        {
                                            Toast popUpError = Toast.makeText(actMyAccount.this, e.toString() , Toast.LENGTH_LONG);
                                            popUpError.show();
                                        }
                                    }
                                    else
                                    {
                                        Snackbar.make(coordinatorLayoutView, "Debes registrar un vehículo antes de contratar una póliza de seguro.", Snackbar.LENGTH_LONG)
                                                //.setActionTextColor(Color.CYAN)
                                                //Color del texto de la acción
                                                .setActionTextColor(Color.parseColor("#FFC107"))
                                                .setAction("Aceptar", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view)
                                                    {
                                                        Intent actNuevoAuto = new Intent(actMyAccount.this, actAddNewCar.class);
                                                        startActivity(actNuevoAuto);
                                                    }
                                                })
                                                .show();
                                        return;

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
            }
            catch (Exception e)
            {
                Toast popUpError = Toast.makeText(actMyAccount.this,  e.getMessage(), Toast.LENGTH_LONG);
                popUpError.show();
                return;
            }
            //Cerramos la base de datos
            db.close();
        }

    }

    public void RegistrarAuto (View view)
    {
        Intent actAddNewCar = new Intent(this, actAddNewCar.class);
        startActivity(actAddNewCar);
    }

    public void RegistrarTarjeta (View view)
    {
        Intent actAddMyCard = new Intent(this, actAddMyCard.class);
        startActivity(actAddMyCard);
    }

    public void EliminarPolizaRegistrada(String strKeyValue)
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
                        String url = "http://app.towpod.net/ws_drop_from_my_account.php?type=p&usr_id=" + CursorDB.getString(0) + "&key_value=" + strKeyValue;
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

                        Toast popUpMensaje = Toast.makeText(this, "Tu póliza ha sido eliminada.", Toast.LENGTH_LONG);
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

                        CargarTarjetas(Integer.parseInt(CursorDB.getString(0)));


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

}
