package net.towpod;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Juan Jos� Melero on 12/06/2015.
 */
public class VolleyUtils {

    public static RequestQueue requestQueue;


    /**Returns an instance of Volley {@code RequestQueue}.
     * @param context
     * @return A {@code RequestQueue} object to make request.
     * */
    public static RequestQueue getRequestQueue(Context context){
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());

        return requestQueue;
    }


    /**Enqueues a GET request for a {@code JSONArray} ({@code JsonArrayResponse}) and uses a
     * {@code VolleyResponseCallback} to get the response or a {@code VolleyError}.
     * @param context Used to get a singleton instance of {@code RequestQueue}.
     * @param url URL address with already with its parameters. E.g.:
     *            "http://www.example.es?parameter=value". Spaces will be replaced with '%20'.
     * @param tag Any {@code Object} acting as a tag. Must be comparable using ==.
     * @param callback {@code VolleyResponseCallback} to return a {@code JSONArray} if the service
     *                 is correctly called or a {@code VolleyError} otherwise. You can get to know
     *                 the error's cause by calling {@toString()} upon it.
     * */
    public static void makeJsonRequest(Context context, String url, Object tag, final VolleyJSONObjectResponseCallback callback){

        //Replaces spaces
        url = url.replaceAll(" ", "%20");
        JsonObjectRequest request = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {callback.onSuccess(response);
            }
                },
                new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure(error);
            }
        }
        );

        addToQueue(context, request, tag);
    }


    static class JSONHeaderRequest extends JsonObjectRequest{

        private HashMap<String, String> params;

        public JSONHeaderRequest(String url, HashMap<String, String> bodyParams, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
            super(url, listener, errorListener);
            this.params = bodyParams;
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            return params;
        }
    }


    /**Sends a {@code JSONObjectRequest} using params in body params as headers for the requests.
     * @param context
     * @param url URL to make the request to.
     * @param tag Tag to identify the request in case we need to cancel it.
     * @param bodyParams Headers to be added to the request in the method {@code getHeaders()}.
     * @param callback A callback to receive the answer or the error produced by the request.*/
    public static void makeJsonWithHeadersRequest(Context context, String url, String tag,
                                                  HashMap<String, String> bodyParams,
                                                  final VolleyJSONObjectResponseCallback callback){

        url = url.replaceAll(" ", "%20");

        JSONHeaderRequest jsonHeaderRequest = new JSONHeaderRequest(url, bodyParams, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure(error);
            }
        });

        addToQueue(context, jsonHeaderRequest, tag);
    }

    /**Enqueues a request using the provided tag.
     * May set a retry policy of three 3 tries waiting for 2.5 seconds ({@code DefaultRetryPolicy.DEFAULT_TIMEOUT_MS})
     * between each try.
     * @param context Preferably an {@code Activity}, since we won0t be able to tell one from the
     *                others if using the application context.
     * @param request {@code Request} to be enqueued.
     * */
    public static void addToQueue(Context context, Request request, Object tag){

        request.setTag(tag);
        //Retry policy
//        request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue(context).add(request);
    }


    /**Cancels every {@code Request} for a given {@code Activity}.
     * @param context {@code Activity} from which the request was enqueued.*/
    public static void cancelActivityQueue(Context context){
        getRequestQueue(context).cancelAll(context);
    }


    /**Cancels every request with the given {@code tag} started by the given {@code context}.
     * @param context {@code Activity} from which the request was enqueued.
     *  @param tag {@code Object} used as tag for the {@code Request}.*/
    public static void cancelActivityQueueByTag(Context context, Object tag){
        getRequestQueue(context).cancelAll(tag);
    }


    /**Enqueues a request for a Bitmap in the given URL Obtains a picture from the given URL.
     * @param context Preferably an {@code Activity} in order to be able to cancel it.
     * @param url The URL for the picture.
     * @param tag The tag to be */
    public static void makeImageRequest(Context context, String url, String tag, final VolleyBitmapCallback callback){
        ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                callback.onSuccess(response);
            }
        }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure(error);
            }
        }
        );

        addToQueue(context, imageRequest, tag);
    }
}