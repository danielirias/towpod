package net.towpod;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

public class actGetHelpWithCard extends AppCompatActivity {
    ListView ListaTarjetas;
    ArrayAdapter AdaptadorTarjetas;
    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_get_help_with_card);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Asistencia pagada");

        //ASISTENCIA PAGADA CON TARJETA
        //Abrimos la base de datos 'DBUsuarios' en modo escritura
        cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(this, "USUARIOSHM", null, 2);
        SQLiteDatabase db = usdbh.getWritableDatabase();
        String[] campos = new String[]{"user_id, user_email", "user_password", "user_keep_session"};
        final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

        try
        {
            //Si hemos abierto correctamente la base de datos
            if (db != null)
            {
                CursorDB.moveToFirst();
                //Cerramos la base de datos
                db.close();
            }
        }
        catch (Exception e)
        {
            return;
        }


        //==========================================================================================
        //=======CARGA DE TARJETAS DE LOS USUARIOS===============================================
        //==========================================================================================

        //Instancia del ListView
        ListaTarjetas = (ListView) findViewById(R.id.ListViewTarjetas);

        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this);

        //Inicializar el adaptador con la fuente de datos
        List<clsPoliza> Polizas = new ArrayList<clsPoliza>();
        AdaptadorTarjetas = new clsPolizaArrayAdapter(this, Polizas);
        AdaptadorTarjetas.clear();

        //URL del detalle del proveedor
        String urlPoliza = "http://app.towpod.net/ws_get_customer_policy.php?idc=" + CursorDB.getInt(0);
        urlPoliza = urlPoliza.replaceAll(" ", "%20");

        JsonArrayRequest requestPoliza = new JsonArrayRequest(urlPoliza,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray responsePoliza) {
                        for (int i = 0; i < responsePoliza.length(); i++){
                            try
                            {
                                AdaptadorTarjetas.add(new clsPoliza(
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
        AdaptadorTarjetas = new clsPolizaArrayAdapter(this, Polizas);

        //Relacionando la lista con el adaptador
        ListaTarjetas.setAdapter(AdaptadorTarjetas);

        //==========================================================================================
        //=======FIN DE CARGA DE POLIZAS DE LOS USUARIOS========================================
        //==========================================================================================

    }
    //Le pone una política de 4 reintentos con 4 segundos entre cada uno y la devuelve.
    public Request setRetryPolicy(Request request){
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }

}
