package net.towpod.fragment_cuenta;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import net.towpod.R;
import net.towpod.RequestQueueSingleton;
import net.towpod.actMyAccount;
import net.towpod.clsTarjeta;
import net.towpod.clsTarjetaArrayAdapter;


public class fragment_cuenta_tarjeta extends  Fragment
{

    ListView ListaTarjetas;
    ArrayAdapter AdaptadorTarjetas;
    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    public fragment_cuenta_tarjeta()
    {
        //Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View MyView = inflater.inflate(R.layout.fragment_cuenta_card, container, false);

        //==========================================================================================
        //=======CARGA DE Tarjetas DE LOS USUARIOS===============================================
        //==========================================================================================

        //Instancia del ListView
        ListaTarjetas = (ListView) MyView.findViewById(R.id.ListViewTarjetas);

        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this.getActivity());

        //Inicializar el adaptador con la fuente de datos
        List<clsTarjeta> Tarjetas = new ArrayList<clsTarjeta>();
        AdaptadorTarjetas = new clsTarjetaArrayAdapter(this.getActivity(), Tarjetas);
        AdaptadorTarjetas.clear();

        //URL del detalle del proveedor
        String urlTarjeta = "http://app.towpod.net/ws_get_customer_card.php?idc=" + getArguments().getInt("prmIDCustomer");
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
        AdaptadorTarjetas = new clsTarjetaArrayAdapter(this.getActivity(), Tarjetas);

        //Relacionando la lista con el adaptador
        ListaTarjetas.setAdapter(AdaptadorTarjetas);

        //Creo el evento Clic para cada objeto de la lista
        ListaTarjetas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> pariente, View view, int posicion, long id) {
                final clsTarjeta tarjetaSeleccionada = (clsTarjeta) pariente.getItemAtPosition(posicion);

                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                LayoutInflater inflater = getActivity().getLayoutInflater();
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
                        ((actMyAccount)getActivity()).EliminarTarjetaRegistrada(tarjetaSeleccionada.get_card_number());
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.setTitle("¿Deseas eliminar la tarjeta seleccionada?");
                alertDialog.setMessage("Esta acción también eliminará cualquier póliza de seguro que tengas asociada a la tarjeta:\n\n" + tarjetaSeleccionada.get_card_name() + "\n" + tarjetaSeleccionada.get_card_number());
                alertDialog.show();

                return false;

            }

        });

        //==========================================================================================
        //=======FIN DE CARGA DE Tarjetas DE LOS USUARIOS========================================
        //==========================================================================================

        return MyView;
    }


    //Le pone una política de 4 reintentos con 4 segundos entre cada uno y la devuelve.
    public Request setRetryPolicy(Request request){
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }


}