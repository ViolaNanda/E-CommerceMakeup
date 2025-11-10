package com.example.e_commerce_5.adapters;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class VolleyMultipartRequest extends Request<NetworkResponse> {
    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "apiclient-" + System.currentTimeMillis();

    private Response.Listener<NetworkResponse> mListener;
    private Response.ErrorListener mErrorListener;
    private Map<String, String> mHeaders;

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
    }

    public VolleyMultipartRequest(String url, Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        this(Method.POST, url, listener, errorListener);
    }

    public static class DataPart {
        public String fileName;
        public byte[] data;
        public String type;

        /**
         * Default data part
         *
         * @param name The name of the part.
         * @param data The data.
         */
        public DataPart(String fileName, byte[] data) {
            this.fileName = fileName;
            this.data = data;
            this.type = null;
        }

        /**
         * Custom data part.
         *
         * @param name The name of the part.
         * @param data The data.
         * @param type The mime type.
         */
        public DataPart(String fileName, byte[] data, String type) {
            this.fileName = fileName;
            this.data = data;
            this.type = type;
        }
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return (mHeaders != null) ? mHeaders : super.getHeaders();
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // Write form fields
            Map<String, String> params = getParams();
            if (params != null && params.size() > 0) {
                paramToBytes(bos, params);
            }

            // Write files
            Map<String, DataPart> data = getByteData();
            if (data != null && data.size() > 0) {
                dataToBytes(bos, data);
            }

            // Write closing boundary
            bos.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream: %s", e.getMessage());
        }
        return bos.toByteArray();
    }

    /**
     * Convert parameters to byte array.
     */
    private void paramToBytes(ByteArrayOutputStream bos, Map<String, String> params) throws IOException {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            buildTextPart(bos, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Convert data parts (files) to byte array.
     */
    private void dataToBytes(ByteArrayOutputStream bos, Map<String, DataPart> data) throws IOException {
        for (Map.Entry<String, DataPart> entry : data.entrySet()) {
            buildDataPart(bos, entry.getValue(), entry.getKey());
        }
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, getCacheEntry());
    }

    protected Map<String, String> getParams() throws AuthFailureError {
        return null;
    }

    protected Map<String, DataPart> getByteData() throws AuthFailureError {
        return null;
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }

    private void buildTextPart(ByteArrayOutputStream data, String parameterName, String parameterValue) throws IOException {
        data.write((twoHyphens + boundary + lineEnd).getBytes());
        data.write(("Content-Disposition: form-data; name=\"" + parameterName + "\"" + lineEnd).getBytes());
        data.write(("Content-Type: text/plain; charset=UTF-8" + lineEnd).getBytes());
        data.write(lineEnd.getBytes());
        data.write(parameterValue.getBytes("UTF-8"));
        data.write(lineEnd.getBytes());
    }

    private void buildDataPart(ByteArrayOutputStream data, DataPart dataPart, String inputName) throws IOException {
        data.write((twoHyphens + boundary + lineEnd).getBytes());
        data.write(("Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\""
                + dataPart.fileName + "\"" + lineEnd).getBytes());
        if (dataPart.type != null && !dataPart.type.trim().isEmpty()) {
            data.write(("Content-Type: " + dataPart.type + lineEnd).getBytes());
        } else {
            data.write(("Content-Type: application/octet-stream" + lineEnd).getBytes());
        }
        data.write(lineEnd.getBytes());
        data.write(dataPart.data);
        data.write(lineEnd.getBytes());
    }
}