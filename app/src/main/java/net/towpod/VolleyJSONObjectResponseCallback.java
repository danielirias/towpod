package net.towpod;

import com.android.volley.VolleyError;

import org.json.JSONObject;

/**
 * Created by Juan José Melero on 09/06/2015.
 */
public interface VolleyJSONObjectResponseCallback {

    public void onSuccess(JSONObject jsonArray);
    public void onFailure(VolleyError volleyError);
}
