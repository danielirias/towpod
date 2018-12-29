package net.towpod;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Juan José Melero on 13/09/2015.
 */
public class RequestQueueSingleton {

    private static RequestQueue requestQueue;

    private RequestQueueSingleton() {}

    public static RequestQueue getRequestQueue(Context context){
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());

        return requestQueue;

    }
}
