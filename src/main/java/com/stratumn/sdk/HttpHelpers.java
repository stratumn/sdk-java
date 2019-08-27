/*
Copyright 2017 Stratumn SAS. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.stratumn.sdk;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Map;

import com.stratumn.chainscript.Constants;

public class HttpHelpers {

    private HttpURLConnection con;

    private String body = null;

    public HttpHelpers(HttpURLConnection con) {
        this.con = con;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public HttpURLConnection getConnection() {
        return this.con;
    }

    public void setConnection(HttpURLConnection con) {
        this.con = con;
    }

    public void sendData() throws IOException {

        if (this.body == null) {
            return;
        }

        DataOutputStream wr = null;

        try {
            wr = new DataOutputStream(con.getOutputStream());

            wr.writeBytes(this.body);
            wr.flush();
            wr.close();

        } catch (IOException exception) {
            throw exception;
        } finally {
            this.closeQuietly(wr);
        }
    }

    public String read() throws IOException {
        BufferedReader in = null;
        String inputLine;
        StringBuilder body;
        try {
            InputStream ins = con.getInputStream();
            in = new BufferedReader(new InputStreamReader(ins));

            body = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                body.append(inputLine);
            }
            in.close();

            return body.toString();
        } catch (IOException ioe) {
            throw ioe;
        } finally {
            this.closeQuietly(in);
        }
    }

    public String readError() {
        BufferedReader br = null;
        String inputLine;
        StringBuilder body;
        try {
            InputStream ins = this.con.getErrorStream();
            br = new BufferedReader(new InputStreamReader(ins));
            body = new StringBuilder();

            while ((inputLine = br.readLine()) != null) {
                body.append(inputLine);
            }
            br.close();
            return body.toString();
        } catch (Exception ioe) {
            return ioe.getMessage();
            // throw ioe;
        } finally {
            this.closeQuietly(br);
        }
    }

    public void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ex) {

        }
    }

    /**
     * Adding Request Parameters. e.g. param1=value&amp;param2=value
     * 
     * @throws IOException
     */
    public void setParams(Map<String, String> parameters) throws IOException {
        DataOutputStream out = new DataOutputStream(this.con.getOutputStream());
        out.writeBytes(HttpHelpers.getParamsString(parameters));
        out.flush();
        out.close();
    }

    public static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {

        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), Constants.UTF8.name()));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), Constants.UTF8.name()));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
    }
}
