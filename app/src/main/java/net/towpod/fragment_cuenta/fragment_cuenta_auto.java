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

import android.widget.GridView;

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
import net.towpod.clsAuto;
import net.towpod.clsAutoArrayAdapter;


public class fragment_cuenta_auto extends  Fragment
{

    GridView ListaAutos;
    ArrayAdapter AdaptadorAutos;
    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    public fragment_cuenta_auto()
    {
        //Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View MyView = inflater.inflate(R.layout.fragment_cuenta_autos, container, false);

        //==========================================================================================
        //=======CARGA DE AUTOS DE LOS USUARIOS===============================================
        //==========================================================================================
        //TextView txtIDCustomer = (TextView) MyView.findViewById(R.id.txtIDCustomer);
        //txtIDCustomer.setText(String.valueOf(getArguments().getInt("prmIDCustomer")));

        //Instancia del ListView
        ListaAutos = (GridView) MyView.findViewById(R.id.ListViewAutos);

        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this.getActivity());

        //Inicializar el adaptador con la fuente de datos
        List<clsAuto> Autos = new ArrayList<clsAuto>();
        AdaptadorAutos = new clsAutoArrayAdapter(this.getActivity(), Autos);
        AdaptadorAutos.clear();

        //URL del detalle del proveedor
        String urlMyCar = "http://app.towpod.net/ws_get_customer_car.php?idc=" + getArguments().getInt("prmIDCustomer");
        urlMyCar = urlMyCar.replaceAll(" ", "%20");

        JsonArrayRequest requestAuto = new JsonArrayRequest(urlMyCar,
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
        AdaptadorAutos = new clsAutoArrayAdapter(this.getActivity(), Autos);

        //Relacionando la lista con el adaptador
        ListaAutos.setAdapter(AdaptadorAutos);

        //Creo el evento Clic para cada objeto de la lista
        ListaAutos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> pariente, View view, int posicion, long id) {
                final clsAuto autoSeleccionado = (clsAuto) pariente.getItemAtPosition(posicion);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                final LayoutInflater inflater = getActivity().getLayoutInflater();
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
                        ((actMyAccount)getActivity()).EliminarAutoRegistrado(autoSeleccionado.get_car_id());
                        CargarAutos(inflater, container, savedInstanceState);
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

        return MyView;

    }


    public View CargarAutos(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View MyView = inflater.inflate(R.layout.fragment_cuenta_autos, container, false);

        //==========================================================================================
        //=======CARGA DE AUTOS DE LOS USUARIOS===============================================
        //==========================================================================================
        //TextView txtIDCustomer = (TextView) MyView.findViewById(R.id.txtIDCustomer);
        //txtIDCustomer.setText(String.valueOf(getArguments().getInt("prmIDCustomer")));

        //Instancia del ListView
        ListaAutos = (GridView) MyView.findViewById(R.id.ListViewAutos);

        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this.getActivity());

        //Inicializar el adaptador con la fuente de datos
        List<clsAuto> Autos = new ArrayList<clsAuto>();
        AdaptadorAutos = new clsAutoArrayAdapter(this.getActivity(), Autos);
        AdaptadorAutos.clear();

        //URL del detalle del proveedor
        String urlMyCar = "http://app.towpod.net/ws_get_customer_car.php?idc=" + getArguments().getInt("prmIDCustomer");
        urlMyCar = urlMyCar.replaceAll(" ", "%20");

        JsonArrayRequest requestAuto = new JsonArrayRequest(urlMyCar,
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
        AdaptadorAutos = new clsAutoArrayAdapter(this.getActivity(), Autos);

        //Relacionando la lista con el adaptador
        ListaAutos.setAdapter(AdaptadorAutos);

        //Creo el evento Clic para cada objeto de la lista
        ListaAutos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> pariente, View view, int posicion, long id) {
                final clsAuto autoSeleccionado = (clsAuto) pariente.getItemAtPosition(posicion);

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
                        ((actMyAccount)getActivity()).EliminarAutoRegistrado(autoSeleccionado.get_car_id());
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

        return MyView;
    }


    //Le pone una política de 4 reintentos con 4 segundos entre cada uno y la devuelve.
    public Request setRetryPolicy(Request request){
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }


}