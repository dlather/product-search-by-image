/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.ml.md.java.productsearch;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.md.java.objectdetection.DetectedObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** A fake search engine to help simulate the complete work flow. */
public class SearchEngine {

  private static final String TAG = "SearchEngine";

  public interface SearchResultListener {
    void onSearchCompleted(DetectedObject object, List<Product> productList);
  }

  private final RequestQueue searchRequestQueue;
  private final ExecutorService requestCreationExecutor;

  public SearchEngine(Context context) {
    searchRequestQueue = Volley.newRequestQueue(context);
    requestCreationExecutor = Executors.newSingleThreadExecutor();
  }

  public void search(DetectedObject object, SearchResultListener listener) {
    // Crops the object image out of the full image is expensive, so do it off the UI thread.
    Tasks.call(requestCreationExecutor, () -> createRequest(object,listener))
        .addOnSuccessListener(productRequest -> searchRequestQueue.add(productRequest.setTag(TAG)))
        .addOnFailureListener(
            e -> {
              Log.e(TAG, "Failed to create product search request!", e);
              // Remove the below dummy code after your own product search backed hooked up.
              List<Product> productList = new ArrayList<>();
              for (int i = 0; i < 8; i++) {
                productList.add(
                    new Product(/* imageUrl= */ "", "Product title " + i, "Product subtitle " + i));
              }
              listener.onSearchCompleted(object, productList);
            });
  }

  private static JsonObjectRequest createRequest(DetectedObject searchingObject,SearchResultListener listener) throws Exception {
    byte[] objectImageData = searchingObject.getImageData();
    if (objectImageData == null) {
      throw new Exception("Failed to get object image data!");
    }

    String base64 = Base64.encodeToString(objectImageData, Base64.DEFAULT);
    Log.i("mine", "Image Base64 : " + base64);


//    URL url = new URL("https://vision.googleapis.com/v1/images:annotate?key=AIzaSyADfqpVsPI3m3aFZe39Fcsj1dRhnG06KrU");
//    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//    connection.setRequestMethod("POST");
//    connection.setRequestProperty("Content-Type","application/json;charset=UTF-8");
//
      JSONObject j1 = new JSONObject();
      j1.put("content",base64);

      JSONObject j3 = new JSONObject();
      j3.put("type","PRODUCT_SEARCH");
//      j3.put("maxResults",1);

      JSONArray j7 = new JSONArray();
      j7.put(j3);


      JSONObject j5 = new JSONObject();
      j5.put("productSet","projects/spark-36581/locations/asia-east1/productSets/demo");

      JSONArray j15 = new JSONArray();
      j15.put("general-v1");

      j5.put("productCategories",j15);

      JSONObject j8 = new JSONObject();
      j8.put("productSearchParams",j5);

      JSONObject j9 = new JSONObject();
      j9.put("imageContext",j8);
      j9.put("image", j1);
      j9.put("features",j7);

      JSONArray j16 = new JSONArray();
      j16.put(j9);

      JSONObject j10 = new JSONObject();
      j10.put("requests",j16);
//
//      Log.i("mine",j10.toString());
//      DataOutputStream os = new DataOutputStream(connection.getOutputStream());
//      os.writeBytes(j10.toString());
//
//      os.flush();
//      os.close();
//
//      Log.i("mine",String.valueOf(connection.getResponseCode()));
//      Log.i("mine",connection.getResponseMessage());
//      Log.i("mine",connection.getContentType());
//      Log.i("mine",connection.getContent().toString());
//
//      connection.disconnect();

      JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "https://vision.googleapis.com/v1/images:annotate?key=AIzaSyADfqpVsPI3m3aFZe39Fcsj1dRhnG06KrU", j10,
              new Response.Listener<JSONObject>() {
                  @Override
                  public void onResponse(JSONObject response) {
                      try {
                          JSONArray j20 = new JSONArray();
                          j20 = response.getJSONArray("responses");
                          JSONObject j21 = new JSONObject();
                          j21 = j20.getJSONObject(0);
                          JSONObject j22 = new JSONObject();
                          j22 = j21.getJSONObject("productSearchResults");
                          JSONArray j23 = new JSONArray();
                          j23 = j22.getJSONArray("results");
                          JSONObject j24 = new JSONObject();
                          j24 = j23.getJSONObject(0);
                          JSONObject j25 = new JSONObject();
                          //String imageUrl = j24.getString("image");
                          j25 = j24.getJSONObject("product");
                          String title = j25.getString("displayName");
                          Log.i("mine",title);
                          List<Product> productList = new ArrayList<>();
                          productList.add(new Product("",title,""));
                          listener.onSearchCompleted(searchingObject, productList);

                      } catch (JSONException e) {
                          e.printStackTrace();
                      }
//                              [0]["productSearchResults"]["results"]["product"]["displayName"]);

                  }
              }, new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
              Log.i("mine","Failed");
          }
      });

      return jsonObjectRequest;

  }

  public void shutdown() {
    searchRequestQueue.cancelAll(TAG);
    requestCreationExecutor.shutdown();
  }


}
