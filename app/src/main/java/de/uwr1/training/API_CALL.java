package de.uwr1.training;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by f00f on 27.06.2014.
 */
public class API_CALL extends AsyncTask<String, Integer, String> {
    private Exception exception;
    private OnApiCallCompletedListener onCompleteListener;
    private String method;
    private String url;
    private int httpStatusCode;

    public API_CALL(OnApiCallCompletedListener onCompleteListener) {
        this(onCompleteListener, "GET");
    }

    public API_CALL(OnApiCallCompletedListener onCompleteListener, String method) {
        this.onCompleteListener = onCompleteListener;
        this.method = method;
    }

    protected String doInBackground(String... params) {
        try {
            httpStatusCode = -1;
            url = new URL(params[0]).toString();
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpRequestBase httpReq = null;
            if (method.equals("GET")) {
                httpReq = new HttpGet(url);
            } else if (method.equals("POST")) {
                HttpPost httpPost = new HttpPost(url);
                /*
                // create a list to store HTTP variables and their values
                List nameValuePairs = new ArrayList<>();
                // add an HTTP variable and value pair
                nameValuePairs.add(new BasicNameValuePair("myHttpData", valueIwantToSend));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                */
                httpReq = httpPost;
                throw new MethodNotSupportedException("POST requests are yet to be implemented.");
            }
            try {
                HttpResponse response = client.execute(httpReq);
                StatusLine statusLine = response.getStatusLine();
                httpStatusCode = statusLine.getStatusCode();
                if (httpStatusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                } else {
                    this.exception = new IOException("Server returned status code other than 200: " + statusLine.getReasonPhrase());
                    return null;
                    //Log.e(ParseJSON.class.toString(), "Failed to download file");
                }
            } catch (ClientProtocolException e) {
                this.exception = e;
            } catch (IOException e) {
                this.exception = e;
            }
            return builder.toString();
        } catch (Exception e) {
            this.exception = e;
            return null;
        }
    }

    protected void onPostExecute(String result) {
        if (null != onCompleteListener) {
            // check this.exception and pass it on
            if (null != this.exception) {
                try {
                    JSONStringer js = new JSONStringer();
                    js.object();
                    js.key("err");
                    js.value(this.httpStatusCode);
                    js.key("msg");
                    js.value(this.exception.getMessage());
                    js.endObject();
                    result = js.toString();
                }
                catch (JSONException e) {
                    result = "{\"err\":"+this.httpStatusCode+",\"msg\":\"An error occured while serializing a now unknown exception.\"}";
                }
            }
            onCompleteListener.onApiCallCompleted(url, result);
        }
    }
}
