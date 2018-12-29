package net.towpod;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class actMyAccountStores extends AppCompatActivity {

    public static String SESSION_USER_MAIL = "";

    ListView ProveedoresRegistrados;
    ArrayAdapter AdaptadorProveedores;

    public ArrayAdapter<String> Adaptador;
    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menuMyAccount:
                Intent actMyAccount = new Intent(this, actMyAccount.class);
                startActivity(actMyAccount);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_my_account_stores);

        setTitle("Establecimientos registrados");

        try {
            //Abrimos la base de datos 'DBUsuarios' en modo escritura
            cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(this, "USUARIOSHM", null, 2);
            SQLiteDatabase db = usdbh.getWritableDatabase();

            //Si hemos abierto correctamente la base de datos
            if (db != null) {
                String[] campos = new String[] {"user_id", "user_email", "user_password"};
                final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

                try {
                    CursorDB.moveToFirst();
                    if (CursorDB.getCount() == 0) {
                        //Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
                        Intent actMainLogin = new Intent(this, actMainLogin.class);
                        startActivity(actMainLogin);
                        return;

                    } else {
                        //SESSION_USER_MAIL = CursorDB.getString(0);
                        CargarInfoUsuario(Integer.parseInt(CursorDB.getString(0)));
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

    public void CargarInfoUsuario(Integer intUsrID)
    {
        //Instancia del ListView
        ProveedoresRegistrados = (ListView)findViewById(R.id.ListView_Proveedores);

        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this);

        AccountManager MiCuenta = AccountManager.get(this);
        Account Cuenta = getAccount(MiCuenta);


        String url = "http://app.towpod.net/ws_get_owned_provider.php?usr="+intUsrID;
        url = url.replaceAll(" ", "%20");

        //Inicializar el adaptador con la fuente de datos
        List<clsProveedorRegistrado> Proveedores = new ArrayList<clsProveedorRegistrado>();
        AdaptadorProveedores = new clsProveedorRegistradoArrayAdapter(this, Proveedores);
        AdaptadorProveedores.clear();

        AdaptadorProveedores.clear();
        //Creamos la petición
        JsonArrayRequest request = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length()==0)
                        {
                            Toast popUpError = Toast.makeText(actMyAccountStores.this, "No encontramos registros asociados a tu cuenta." , Toast.LENGTH_LONG);
                            popUpError.show();
                            return;
                        }
                        else
                        {
                            for (int i = 0; i < response.length(); i++){
                                try {
                                    //txtDescripcion.setText(response.getJSONObject(0).getString("category_description"));
                                    AdaptadorProveedores.add(new clsProveedorRegistrado(
                                            response.getJSONObject(i).getString("provider_id"),
                                            response.getJSONObject(i).getString("provider_name"),
                                            response.getJSONObject(i).getString("provider_address"),
                                            response.getJSONObject(i).getString("provider_city"),
                                            response.getJSONObject(i).getString("category_icon"),
                                            response.getJSONObject(i).getString("category_color"),
                                            response.getJSONObject(i).getInt("main_category_id")));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Si recibimos un error, mostraremos la causa
                        Toast popUpError = Toast.makeText(actMyAccountStores.this, error.getMessage() , Toast.LENGTH_LONG);
                        popUpError.show();
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

        //Inicializar el adaptador con la fuente de datos
        AdaptadorProveedores = new clsProveedorRegistradoArrayAdapter(this, Proveedores);

        //Relacionando la lista con el adaptador
        ProveedoresRegistrados.setAdapter(AdaptadorProveedores);

        //Creo el evento Clic para cada objeto de la lista
        ProveedoresRegistrados.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id)
            {

            }
        });
    }

    private static Account getAccount(AccountManager accountManager) {
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
    public Request setRetryPolicy(Request request){
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }

    public class AsyncTaskLoadImage  extends AsyncTask<String, String, Bitmap> {
        private final static String TAG = "AsyncTaskLoadImage";
        private ImageView imageView;
        public AsyncTaskLoadImage(ImageView imageView) {
            this.imageView = imageView;
        }
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            try {
                URL url = new URL(params[0]);
                bitmap = BitmapFactory.decodeStream((InputStream) url.getContent());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            return bitmap;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }

}
