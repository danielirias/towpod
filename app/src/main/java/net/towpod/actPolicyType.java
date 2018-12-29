package net.towpod;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class actPolicyType extends AppCompatActivity {

    ListView ListaTipoPolizas;
    ArrayAdapter AdaptadorTipoPolizas;

    ListView ListaPolizas;
    ArrayAdapter AdaptadorPolizas;
    GridView ListaAutos;
    ArrayAdapter AdaptadorAutos;
    ListView ListaTarjetas;
    ArrayAdapter AdaptadorTarjetas;

    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    public String strCarID = "";
    public String strCardNumber = "";
    public Integer strNewPolicyTypeID;
    public String strNewPolicyName = "";
    public Double strNewPolicyValue;
    public String strPolicyNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_policy_type);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Contratar servicio");

        //======================================================================
        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this);

        //Instancia del GridView
        ListaTipoPolizas = (ListView) findViewById(R.id.ListViewTipoPoliza);

        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this);

        //URL de las categorías
        String url = "http://app.towpod.net/ws_get_policy_type.php";
        url = url.replaceAll(" ", "%20");

        //Inicializar el adaptador con la fuente de datos
        List<clsTipoPoliza> clsTipoPoliza = new ArrayList<clsTipoPoliza>();
        AdaptadorTipoPolizas = new clsTipoPolizaArrayAdapter(this, clsTipoPoliza);
        AdaptadorTipoPolizas.clear();

        //Creamos la petición
        JsonArrayRequest request = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response)
                    {
                        for (int i = 0; i < response.length(); i++){
                            try {
                                AdaptadorTipoPolizas.add(new clsTipoPoliza(
                                        response.getJSONObject(i).getInt("policy_type_id"),
                                        response.getJSONObject(i).getString("policy_type_name"),
                                        response.getJSONObject(i).getString("policy_type_description"),
                                        response.getJSONObject(i).getDouble("policy_type_month_price"),
                                        response.getJSONObject(i).getString("bank_name")));
                            }
                            catch (JSONException e)
                            {
                                Toast popUpError = Toast.makeText(actPolicyType.this, e.toString() , Toast.LENGTH_LONG);
                                popUpError.show();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Si recibimos un error, mostraremos la causa
                        Toast popUpError = Toast.makeText(actPolicyType.this, error.getMessage() , Toast.LENGTH_LONG);
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
        AdaptadorTipoPolizas = new clsTipoPolizaArrayAdapter(this, clsTipoPoliza);

        //Relacionando la lista con el adaptador
        ListaTipoPolizas.setAdapter(AdaptadorTipoPolizas);

        //Creo el evento Clic para cada objeto de la lista
        ListaTipoPolizas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id)
            {
                clsTipoPoliza MyItemSelected = (clsTipoPoliza) pariente.getItemAtPosition(posicion);

                strNewPolicyTypeID = MyItemSelected.get_policy_type_id();
                strNewPolicyName = MyItemSelected.get_policy_type_name();
                strNewPolicyValue = MyItemSelected.get_policy_type_month_price();

                final AlertDialog.Builder builder = new AlertDialog.Builder(actPolicyType.this);
                LayoutInflater inflater = actPolicyType.this.getLayoutInflater();
                builder.setView(inflater.inflate(R.layout.dialog_set_policy, null));

                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id) {
                        //Nada para hacer aquí
                    }
                });

                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id) {
                        DialogoMostrarTarjetasDisponibles();
                    }
                });

                AlertDialog alertDialogPrice = builder.create();
                //alertDialog.setTitle("Contratar póliza");
                alertDialogPrice.show();

                TextView pName = (TextView) alertDialogPrice.findViewById(R.id.txtPolicyName);
                pName.setText(MyItemSelected.get_policy_type_name());

                TextView pDescription = (TextView) alertDialogPrice.findViewById(R.id.txtPolicyDescription);
                pDescription.setText(MyItemSelected.get_policy_type_description());

                DecimalFormat Formato = new DecimalFormat("#,###.00");
                String precioFormateado = Formato.format(MyItemSelected.get_policy_type_month_price());

                TextView pPrice = (TextView) alertDialogPrice.findViewById(R.id.txtPolicyPrice);
                pPrice.setText("HNL "+String.valueOf(precioFormateado) +"/mes");


                Button theButton = alertDialogPrice.getButton(DialogInterface.BUTTON_POSITIVE);
                //theButton.setOnClickListener(new actPolicyType.CustomListenerMostrarTarjetasDisponibles(alertDialogPrice));


            }
        });
        //======================================================================


    }

    public void DialogoMostrarTarjetasDisponibles()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(actPolicyType.this);
        LayoutInflater inflater = actPolicyType.this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_select_my_card, null));

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {

            }
        });

        /*builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {

            }
        });*/

        final AlertDialog alertDialogCard = builder.create();
        alertDialogCard.show();

        final RadioGroup grupoTarjetas = (RadioGroup) alertDialogCard.findViewById(R.id.grupoTarjetas);

            /*grupoTarjetas.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    RadioButton optSelecccionada = (RadioButton) grupoTarjetas.getChildAt(checkedId);

                    strCardNumber = optSelecccionada.getText().toString();
                    String[] parts = strCardNumber.split("\\ - "); // escape
                    strCardNumber = parts[0];

                    strCardNumber = strCardNumber.toString().trim();

                    Toast popUp = Toast.makeText(actPolicyType.this, strCardNumber.toString(), Toast.LENGTH_LONG);
                }
            });*/

        try {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actPolicyType.this, "USUARIOSHM", null, 2);
            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Si hemos abierto correctamente la base de datos
            if (db != null) {
                String[] campos = new String[]{"user_id", "user_email", "user_password", "user_keep_session"};
                final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

                try {
                    CursorDB.moveToFirst();
                    if (CursorDB.getCount() == 0) {
                        //Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
                        Intent actMainLogin = new Intent(actPolicyType.this, actMainLogin.class);
                        startActivity(actMainLogin);
                        return;

                    }
                    else
                    {
                        //==========================================================================================
                        //=======CARGA DE Tarjetas DE LOS USUARIOS===============================================
                        //==========================================================================================

                        //Instancia del ListView
                        ListaTarjetas = (ListView) alertDialogCard.findViewById(R.id.ListViewTarjetas);

                        //Obtenemos la instancia única de la cola de peticiones
                        requestQueue = RequestQueueSingleton.getRequestQueue(actPolicyType.this);

                        //Inicializar el adaptador con la fuente de datos
                        List<clsTarjeta> Tarjetas = new ArrayList<clsTarjeta>();
                        AdaptadorTarjetas = new clsTarjetaArrayAdapter(actPolicyType.this, Tarjetas);
                        AdaptadorTarjetas.clear();

                        //URL del detalle del proveedor
                        String urlTarjeta = "http://app.towpod.net/ws_get_customer_card.php?idc=" + CursorDB.getString(0);
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
                        AdaptadorTarjetas = new clsTarjetaArrayAdapter(actPolicyType.this, Tarjetas);

                        //Relacionando la lista con el adaptador
                        ListaTarjetas.setAdapter(AdaptadorTarjetas);

                        //Creo el evento Clic para cada objeto de la lista
                        ListaTarjetas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {
                                final clsTarjeta tarjetaSeleccionada = (clsTarjeta) pariente.getItemAtPosition(posicion);
                                strCardNumber = tarjetaSeleccionada.get_card_number();
                                alertDialogCard.dismiss();
                                DialogoMostrarAutosDisponibles();
                            }

                        });

                        //==========================================================================================
                        //=======FIN DE CARGA DE Tarjetas DE LOS USUARIOS========================================
                        //==========================================================================================
                    }
                }
                catch (Exception e)
                {
                    Toast popUpError = Toast.makeText(actPolicyType.this, e.getMessage(), Toast.LENGTH_LONG);
                    popUpError.show();
                }

                //Cerramos la base de datos
                db.close();

            }
        } catch (Exception e) {
            return;
        }

        Button theButton = alertDialogCard.getButton(DialogInterface.BUTTON_POSITIVE);
        //theButton.setOnClickListener(new actPolicyType.CustomListenerAutos(alertDialogCard));

    }

    public void DialogoMostrarAutosDisponibles()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(actPolicyType.this);
        LayoutInflater inflater = actPolicyType.this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_select_car, null));

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {

            }
        });

        /*builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {

            }
        });*/

        final AlertDialog alertDialogMyCar = builder.create();
        alertDialogMyCar.show();

        try {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actPolicyType.this, "USUARIOSHM", null, 2);
            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Si hemos abierto correctamente la base de datos
            if (db != null) {
                String[] campos = new String[]{"user_id", "user_email", "user_password", "user_keep_session"};
                final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

                try {
                    CursorDB.moveToFirst();
                    if (CursorDB.getCount() == 0) {
                        //Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
                        Intent actMainLogin = new Intent(actPolicyType.this, actMainLogin.class);
                        startActivity(actMainLogin);
                        return;

                    }
                    else
                    {
                        //Instancia del ListView
                        ListaAutos = (GridView) alertDialogMyCar.findViewById(R.id.ListViewAutos);

                        //Obtenemos la instancia única de la cola de peticiones
                        requestQueue = RequestQueueSingleton.getRequestQueue(actPolicyType.this);

                        //Inicializar el adaptador con la fuente de datos
                        List<clsAuto> Autos = new ArrayList<clsAuto>();
                        AdaptadorAutos = new clsAutoArrayAdapter(actPolicyType.this, Autos);
                        AdaptadorAutos.clear();

                        //URL del detalle del proveedor
                        String urlAuto = "http://app.towpod.net/ws_get_customer_car.php?idc=" + CursorDB.getString(0);
                        urlAuto = urlAuto.replaceAll(" ", "%20");

                        JsonArrayRequest requestAuto = new JsonArrayRequest(urlAuto,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray responseAuto) {
                                        for (int i = 0; i < responseAuto.length(); i++){
                                            try
                                            {
                                                responseAuto.getJSONObject(i).getString("car_id");
                                                responseAuto.getJSONObject(i).getString("car_brand");
                                                responseAuto.getJSONObject(i).getString("car_model");
                                                responseAuto.getJSONObject(i).getString("car_year");

                                                RadioButton opcionAuto = new RadioButton(actPolicyType.this);
                                                LinearLayout.LayoutParams params = new RadioGroup.LayoutParams(
                                                        RadioGroup.LayoutParams.WRAP_CONTENT,
                                                        RadioGroup.LayoutParams.WRAP_CONTENT);

                                                opcionAuto.setLayoutParams(params);
                                                opcionAuto.setPadding(0, 0, 0, 15);
                                                opcionAuto.setId(i);
                                                opcionAuto.setText(responseAuto.getJSONObject(i).getString("car_brand") + ", " + responseAuto.getJSONObject(i).getString("car_model") + " (" + responseAuto.getJSONObject(i).getString("car_year") +")\n[" + responseAuto.getJSONObject(i).getString("car_id") + "]");
                                                opcionAuto.setTag(responseAuto.getJSONObject(i).getString("car_id"));

                                                //grupoAutos.addView(opcionAuto);

                                                AdaptadorAutos.add(new clsAuto(
                                                        responseAuto.getJSONObject(i).getString("car_id"),
                                                        responseAuto.getJSONObject(i).getString("car_brand"),
                                                        responseAuto.getJSONObject(i).getString("car_model"),
                                                        responseAuto.getJSONObject(i).getInt("car_year")));


                                                if (i == 0)
                                                {
                                                    opcionAuto.setChecked(true);
                                                }

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
                        AdaptadorAutos = new clsAutoArrayAdapter(actPolicyType.this, Autos);

                        //Relacionando la lista con el adaptador
                        ListaAutos.setAdapter(AdaptadorAutos);

                        //Creo el evento Clic para cada objeto de la lista
                        ListaAutos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {
                                final clsAuto autoSeleccionado = (clsAuto) pariente.getItemAtPosition(posicion);
                                strCarID = autoSeleccionado.get_car_id();
                                alertDialogMyCar.dismiss();
                                VerificarPoliza();

                            }

                        });

                    }
                } catch (Exception e) {
                    Toast popUpError = Toast.makeText(actPolicyType.this, e.getMessage(), Toast.LENGTH_LONG);
                    popUpError.show();
                }

                //Cerramos la base de datos
                db.close();

            }
        } catch (Exception e) {
            return;
        }

        Button theButton = alertDialogMyCar.getButton(DialogInterface.BUTTON_POSITIVE);
        //theButton.setOnClickListener(new actPolicyType.CustomListenerAutos(alertDialogCard));

    }

    public void VerificarPoliza()
    {
        //Verifico si el vehiculo tiene alguna poliza asignada
        try {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actPolicyType.this, "USUARIOSHM", null, 2);
            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Si hemos abierto correctamente la base de datos
            if (db != null) {
                String[] campos = new String[]{"user_id", "user_email", "user_password", "user_keep_session"};
                final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

                try {
                    CursorDB.moveToFirst();
                    if (CursorDB.getCount() == 0) {
                        //Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
                        Intent actMainLogin = new Intent(actPolicyType.this, actMainLogin.class);
                        startActivity(actMainLogin);
                        return;
                    }
                    else
                    {
                        //URL del detalle del proveedor
                        String urlAuto = "http://app.towpod.net/ws_get_customer_policy_by_car.php?idc=" + CursorDB.getString(0) + "&idcar=" + strCarID;
                        urlAuto = urlAuto.replaceAll(" ", "%20");

                        JsonArrayRequest requestAuto = new JsonArrayRequest(urlAuto,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(final JSONArray responseAuto)
                                    {
                                        if (responseAuto.length() > 0)
                                        {
                                            try
                                            {

                                                AlertDialog.Builder builder = new AlertDialog.Builder(actPolicyType.this);
                                                LayoutInflater inflater = actPolicyType.this.getLayoutInflater();
                                                builder.setView(inflater.inflate(R.layout.dialog_policy_replace, null));

                                                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
                                                {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //Nada para hacer aquí
                                                    }
                                                });

                                                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                                {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //El código se movió a la clase CustomListener
                                                        try {
                                                            EliminarPolizaRegistrada(responseAuto.getJSONObject(0).getString("policy_number"));
                                                            InsertarPoliza();
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });

                                                AlertDialog alertDialog = builder.create();
                                                alertDialog.show();

                                                TextView txtNombrePolizaExistente = (TextView) alertDialog.findViewById(R.id.txtNombrePolizaExistente);
                                                txtNombrePolizaExistente.setText(responseAuto.getJSONObject(0).getString("policy_type_name"));

                                                TextView txtNuevaPoliza = (TextView) alertDialog.findViewById(R.id.txtNuevaPoliza);
                                                txtNuevaPoliza.setText(strNewPolicyName);

                                                Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                                                //theButton.setOnClickListener(new actPolicyType.CustomListenerAsignarPoliza(alertDialog));

                                            }
                                            catch (JSONException e)
                                            {
                                                //Toast popUpError = Toast.makeText(actProviderData.this, e.toString() , Toast.LENGTH_LONG);
                                                //popUpError.show();
                                            }
                                        }
                                        else
                                        {
                                            InsertarPoliza();
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
                    Toast popUpError = Toast.makeText(actPolicyType.this, e.getMessage(), Toast.LENGTH_LONG);
                    popUpError.show();
                }

                //Cerramos la base de datos
                db.close();

            }
        } catch (Exception e) {
            return;
        }



    }

    private void InsertarPoliza()
    {
        try {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actPolicyType.this, "USUARIOSHM", null, 2);
            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Si hemos abierto correctamente la base de datos
            if (db != null) {
                String[] campos = new String[]{"user_id", "user_email", "user_password", "user_keep_session"};
                final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

                try {
                    CursorDB.moveToFirst();
                    if (CursorDB.getCount() == 0) {
                        //Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
                        Intent actMainLogin = new Intent(actPolicyType.this, actMainLogin.class);
                        startActivity(actMainLogin);
                        return;
                    }
                    else
                    {
                        String strIsoName = this.getResources().getConfiguration().locale.getCountry();
                        strPolicyNumber = strIsoName + "-" + String.valueOf(Calendar.getInstance().get(Calendar.YEAR)) + String.valueOf(Calendar.getInstance().get(Calendar.MONTH)+1) + String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                                + "-" + String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) + String.valueOf(Calendar.getInstance().get(Calendar.MINUTE)) + String.valueOf(Calendar.getInstance().get(Calendar.SECOND))
                                + "-" + String.valueOf(CursorDB.getString(0));

                        //URL del detalle del proveedor
                        String url = "http://app.towpod.net/ws_add_my_policy.php?pt_id=" +
                                strNewPolicyTypeID +
                                "&p_no=" + strPolicyNumber +
                                "&p_value=" + strNewPolicyValue +
                                "&car_id=" + strCarID +
                                "&usr_id=" + CursorDB.getString(0) +
                                "&card_number=" + strCardNumber;

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

                        Toast popUpMensaje = Toast.makeText(actPolicyType.this, "Tu póliza ha sido adquirida con éxito", Toast.LENGTH_LONG);
                        popUpMensaje.show();

                        actPolicyType.super.finish();


                        //Le ponemos un tag que servirá para identificarla si la queremos cancelar
                        requestNewPolicy.setTag(JSON_REQUEST);
                        //Al ponerle la política de reintento, le hacemos casting, ya que el metodo recibe un objeto de la clase padre
                        requestNewPolicy = (JsonArrayRequest) setRetryPolicy(requestNewPolicy);
                        //Iniciamos la petición añadiéndola a la cola
                        requestQueue.add(requestNewPolicy);
                    }
                } catch (Exception e) {
                    Toast popUpError = Toast.makeText(actPolicyType.this, e.getMessage(), Toast.LENGTH_LONG);
                    popUpError.show();
                }

                //Cerramos la base de datos
                db.close();

            }
        } catch (Exception e) {
            return;
        }
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

                        Toast popUpMensaje = Toast.makeText(this, "Tu póliza ha actualizada.", Toast.LENGTH_LONG);
                        //popUpMensaje.show();

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

    //Le pone una política de 4 reintentos con 4 segundos entre cada uno y la devuelve.
    public Request setRetryPolicy(Request request){
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }


}
