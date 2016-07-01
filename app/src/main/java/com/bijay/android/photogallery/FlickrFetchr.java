package com.bijay.android.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bsoren on 06-Jun-16.
 */
public class FlickrFetchr {

    private static final String TAG = FlickrFetchr.class.getSimpleName();
    private static final String API_KEY = "ab636210b4c6bbb4deab63c52c06747b";
    private static final String FLICKR_REST_URI = "https://api.flickr.com/services/rest/";

    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";

    /**
     * https://api.flickr.com/services/rest/
     * ?method=flickr.photos.getRecent
     * &api_key=ab636210b4c6bbb4deab63c52c06747b&format=json&nojsoncallback=1
     */

    private static final Uri ENDPOINT = Uri
            .parse(FLICKR_REST_URI)
            .buildUpon()
            .appendQueryParameter("api_key",API_KEY)
            .appendQueryParameter("format","json")
            .appendQueryParameter("nojsoncallback","1")
            .appendQueryParameter("extras","url_s")
            .build();


    public byte[] getUrlBytes(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream bout =  new ByteArrayOutputStream();
            InputStream is = connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage()+ " with "+ urlString);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];

            while((bytesRead = is.read(buffer)) > 0){
                bout.write(buffer,0,bytesRead);
            }
            bout.close();
            return bout.toByteArray();

        } finally {
            connection.disconnect();
        }

    }



    public String getUrlString(String urlString) throws IOException {

        return new String(getUrlBytes(urlString));
    }


    /**
     * https://api.flickr.com/services/rest/
     * ?method=flickr.photos.getRecent
     * &api_key=ab636210b4c6bbb4deab63c52c06747b&format=json&nojsoncallback=1
     */



    private List<GalleryItem> downloadGalleryItems(String url){

        List<GalleryItem> items = new ArrayList<>();

        try {
            String jsonString =  getUrlString(url);
            Log.i(TAG,"Received JSON :" + jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);
            parseItems(items,jsonObject);
        } catch (IOException e) {
            Log.e(TAG, " IOException "+ e.getMessage());
        }catch(JSONException je){
            Log.e(TAG," Error in parsing json object : " + je.getMessage());
        }

        return items;
    }

    private String buildUrl(String method, String query){
        Uri.Builder uriBuilder =  ENDPOINT.buildUpon()
                .appendQueryParameter("method",method);

        if(method.equals(SEARCH_METHOD)){
            uriBuilder.appendQueryParameter("text",query);
        }

        return uriBuilder.build().toString();

    }

    public List<GalleryItem> fetchRecentPhotos(){
        String url = buildUrl(FETCH_RECENTS_METHOD, null);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String query){
        String url =  buildUrl(SEARCH_METHOD,query);
        return downloadGalleryItems(url);
    }


    private void parseItems(List<GalleryItem> items, JSONObject jsonObject) throws JSONException{

        JSONObject jsonPhotos = jsonObject.getJSONObject("photos");
        JSONArray jsonPhotoArray = jsonPhotos.getJSONArray("photo");

        Log.d(TAG,"jsonPhotoArray : "+jsonPhotoArray.length());

        for(int i=0;i<jsonPhotoArray.length();i++){
            JSONObject jsonPhoto = (JSONObject) jsonPhotoArray.get(i);
            GalleryItem item = new GalleryItem();
            item.setId(jsonPhoto.getString("id"));
            item.setCaption(jsonPhoto.getString("title"));

            if(!jsonPhoto.has("url_s")){
                continue;
            }

            item.setUrl(jsonPhoto.getString("url_s"));
            item.setOwner(jsonPhoto.getString("owner"));
           // Log.i(TAG,"ITEM : "+item);
            items.add(item);
        }

    }
}
