package com.example.appproyecto.api;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RestService {

    public interface ResponseListener {
        void onResponse(String response);
        void onError(String error);
    }

    private static final String SERVER_URL = "https://personalwebdb.000webhostapp.com/";
    private static final String USER_SERVICE_URL = "validar_usu.php";
    private static final String DOCUMENT_SERVICE_URL = "validacion.php";

    private static RestService instance = null;

    RequestQueue requestQueue;

    public RestService(Context context){
        requestQueue = Volley.newRequestQueue(context);
    }

    public static synchronized RestService getInstance(Context context){
        if (instance == null){
            instance = new RestService(context);
        }
        return instance;
    }

    public void validarUsuario(final String usuario, final String password, final ResponseListener responseListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SERVER_URL + USER_SERVICE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                responseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseListener.onError(error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros=new HashMap<String,String>();
                parametros.put("usuario", usuario);
                parametros.put("password", password);
                return parametros;
            }
        };

        requestQueue.add(stringRequest);
    }

    public void documentValidation(String nroRuc, String nroDoc, final ResponseListener responseListener){
        String urlParams = "?nro_ruc=" + nroRuc + "&nro_doc=" + nroDoc;
        JsonArrayRequest jsonArrayRequest= new JsonArrayRequest(SERVER_URL + DOCUMENT_SERVICE_URL + urlParams, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++) {
                    try {
                        jsonObject = response.getJSONObject(i);
                        responseListener.onResponse(jsonObject.getString("estado"));
                    } catch (JSONException e) {
                        responseListener.onError(e.getMessage());
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseListener.onError("NO SE ENCONTRÓ DOCUMENTO");
            }
        });

        requestQueue.add(jsonArrayRequest);
    }

}
