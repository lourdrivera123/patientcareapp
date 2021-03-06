package com.beta.zem.patientcareapp.Network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.beta.zem.patientcareapp.ConfigurationModule.Helpers;
import com.beta.zem.patientcareapp.Controllers.UpdateController;
import com.beta.zem.patientcareapp.Interface.ErrorListener;
import com.beta.zem.patientcareapp.Interface.RespondListener;

import org.json.JSONException;
import org.json.JSONObject;

import static android.util.Log.d;

public class GetRequest {

    public static void getJSONobj(final Context c, final String q, final String table_name, final String table_id, final RespondListener<JSONObject> listener, final ErrorListener<VolleyError> errorlistener) {
        RequestQueue queue;
        final Helpers helpers;
        final UpdateController upc;

        queue = VolleySingleton.getInstance().getRequestQueue();
        helpers = new Helpers();
        upc = new UpdateController(c);
        final String url = helpers.get_url(q, table_name);

        JsonObjectRequest jsonrequest = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    d("url_getrequest",url);
                    d("has_contents", response.getBoolean("has_contents") + "");
                    //condition here must not be success cause it still return 1, it shuold be something like if the table name
                    //is not on response json array
                    if(response.getBoolean("has_contents")){
                        Sync sync = new Sync();
                        sync.init(c, table_name, table_id, response);
                            upc.updateLastUpdatedTable(table_name, response.getString("latest_updated_at"));

                    } else {
                        System.out.print("<"+url+">Response is good but there is no need to update sqlite columns since there are no records - dafuq im still missing delete sqlite records !");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                listener.getResult(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                errorlistener.getError(error);
                d("error <GetRequest> ", error + "");
            }
        });
        queue.add(jsonrequest);
    }
}
