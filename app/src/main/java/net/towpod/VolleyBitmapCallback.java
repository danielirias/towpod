package net.towpod;

import android.graphics.Bitmap;

import com.android.volley.VolleyError;

/**
 * Created by Juan José Melero on 09/06/2015.
 */
public interface VolleyBitmapCallback {

    public void onSuccess(Bitmap bitmap);
    public void onFailure(VolleyError volleyError);
}
