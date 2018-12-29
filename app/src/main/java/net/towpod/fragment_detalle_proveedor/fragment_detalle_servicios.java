package net.towpod.fragment_detalle_proveedor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
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

import net.towpod.R;
import net.towpod.RequestQueueSingleton;
import net.towpod.clsCategoria;
import net.towpod.clsCategoriaRelacionadaArrayAdapter;

public class fragment_detalle_servicios extends Fragment
{
    GridView ListaCategorias;
    ArrayAdapter AdaptadorCategorias;



    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    public fragment_detalle_servicios() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        final View MyView = inflater.inflate(R.layout.fragment_detalle_servicios, container, false);


        //MÉTODOS DE PAGO
        if (getArguments().getString("prmPayCard") == "1")
        {
            RelativeLayout loCard = (RelativeLayout) MyView.findViewById(R.id.loCard);
            loCard.setVisibility(View.VISIBLE);
        }
        if (getArguments().getString("prmPayCash") == "1")
        {
            RelativeLayout loCash = (RelativeLayout) MyView.findViewById(R.id.loCash);
            loCash.setVisibility(View.VISIBLE);
        }
        if (getArguments().getString("prmPayCheck") == "1")
        {
            RelativeLayout loCheck = (RelativeLayout) MyView.findViewById(R.id.loCheck);
            loCheck.setVisibility(View.VISIBLE);
        }


        //======================================================================
        //Instancia del GridView
        ListaCategorias = (GridView) MyView.findViewById(R.id.ListViewCategorias);

        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(getContext());

        //URL de las categorías
        String url = "http://app.towpod.net/ws_get_category_related.php?idp=" + getArguments().getString("prmIDProveedor");
        url = url.replaceAll(" ", "%20");

        //Inicializar el adaptador con la fuente de datos
        List<clsCategoria> clsCategorias = new ArrayList<clsCategoria>();
        AdaptadorCategorias = new clsCategoriaRelacionadaArrayAdapter(getContext(), clsCategorias);
        AdaptadorCategorias.clear();

        //Creamos la petición
        JsonArrayRequest request = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response)
                    {
                        for (int i = 0; i < response.length(); i++){
                            try {
                                AdaptadorCategorias.add(new clsCategoria(
                                        response.getJSONObject(i).getString("category_id"),
                                        response.getJSONObject(i).getString("category_name"),
                                        response.getJSONObject(i).getString("category_color"),
                                        response.getJSONObject(i).getString("category_icon")));

                            } catch (JSONException e)
                            {
                                Toast popUpError = Toast.makeText(getContext(), e.toString() , Toast.LENGTH_LONG);
                                popUpError.show();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Toast popUpError = Toast.makeText(getContext(), error.getMessage() , Toast.LENGTH_LONG);
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
        AdaptadorCategorias = new clsCategoriaRelacionadaArrayAdapter(getContext(), clsCategorias);

        //Relacionando la lista con el adaptador
        ListaCategorias.setAdapter(AdaptadorCategorias);

        //Creo el evento Clic para cada objeto de la lista
        ListaCategorias.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {

            }
        });
        //======================================================================
        return MyView;
    }

    //Le pone una política de 4 reintentos con 4 segundos entre cada uno y la devuelve.
    public Request setRetryPolicy(Request request){
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }
}