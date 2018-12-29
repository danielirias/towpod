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
import android.widget.TextView;

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
import net.towpod.clsPoliza;
import net.towpod.clsPolizaArrayAdapter;


public class fragment_cuenta_poliza extends  Fragment
{

    ListView ListaPolizas;
    ArrayAdapter AdaptadorPolizas;
    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    public fragment_cuenta_poliza()
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
        View MyView = inflater.inflate(R.layout.fragment_cuenta_polizas, container, false);

        TextView txtNombreAsegurado = (TextView) MyView.findViewById(R.id.txtNombreProveedor);
        txtNombreAsegurado.setText(getArguments().getString("prmNombreAsegurado"));

        //TextView txtIDCustomer = (TextView) MyView.findViewById(R.id.txtIDCustomer);
        //txtIDCustomer.setText(String.valueOf(getArguments().getInt("prmIDCustomer")));

        //==========================================================================================
        //=======CARGA DE POLIZAS DE LOS USUARIOS===============================================
        //==========================================================================================

        //Instancia del ListView
        ListaPolizas = (ListView) MyView.findViewById(R.id.ListViewPolizas);

        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this.getActivity());

        //Inicializar el adaptador con la fuente de datos
        List<clsPoliza> Polizas = new ArrayList<clsPoliza>();
        AdaptadorPolizas = new clsPolizaArrayAdapter(this.getActivity(), Polizas);
        AdaptadorPolizas.clear();

        //URL del detalle del proveedor
        String urlPoliza = "http://app.towpod.net/ws_get_customer_policy.php?idc=" + getArguments().getInt("prmIDCustomer");
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
        AdaptadorPolizas = new clsPolizaArrayAdapter(this.getActivity(), Polizas);

        //Relacionando la lista con el adaptador
        ListaPolizas.setAdapter(AdaptadorPolizas);

        //Creo el evento Clic para cada objeto de la lista
        ListaPolizas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> pariente, View view, int posicion, long id) {
                final clsPoliza polizaActual = (clsPoliza) pariente.getItemAtPosition(posicion);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                        ((actMyAccount)getActivity()).EliminarPolizaRegistrada(polizaActual.get_car_id());
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.setTitle("¿Deseas eliminar la póliza de seguro seleccionada?");
                alertDialog.setMessage(polizaActual.get_policy_name() + "\n \nAsignada a: \n" + polizaActual.get_car_brand() + ", " + polizaActual.get_car_model() + " (" + polizaActual.get_car_year() + ")\n" + polizaActual.get_car_id());
                alertDialog.show();

                return false;
            }

        });
        //==========================================================================================
        //=======FIN DE CARGA DE POLIZAS DE LOS USUARIOS========================================
        //==========================================================================================

        return MyView;
    }

    //Le pone una política de 4 reintentos con 4 segundos entre cada uno y la devuelve.
    public Request setRetryPolicy(Request request){
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }



}