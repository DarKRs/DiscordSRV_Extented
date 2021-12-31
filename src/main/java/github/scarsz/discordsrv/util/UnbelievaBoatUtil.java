/*-
 * LICENSE
 * DiscordSRV
 * -------------
 * Copyright (C) 2016 - 2021 Austin "Scarsz" Shapiro
 * -------------
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * END
 */

package github.scarsz.discordsrv.util;

import com.github.kevinsawicki.http.HttpRequest;
import github.scarsz.discordsrv.DiscordSRV;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.bstats.json.JsonObjectBuilder;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.*;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class UnbelievaBoatUtil {

    private static String Token = DiscordSRV.config().getString("UnbelievaBoatToken");
    private static String Guild = DiscordSRV.getPlugin().getMainGuild().getId();
    private static String BaseUrl = "https://unbelievaboat.com/api/v1/guilds/" + Guild + "/users/";

    public static String GetUserBalance(String UserID){
        try {
            URL url = new URL(BaseUrl+UserID);
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestProperty("Authorization", Token);
            String result = IOUtils.toString(http.getInputStream(), StandardCharsets.UTF_8);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  "";
    }

    public  static boolean PutUserBalance(String UserID, String CashOrBank, String sum){
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(CashOrBank, sum);
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPut httpPut = new HttpPut(new URI(BaseUrl+UserID));
            httpPut.addHeader("Accept", "application/json");
            httpPut.addHeader("Authorization", Token);
            httpPut.addHeader("Content-Type", "application/x-www-form-urlencoded");
            StringEntity data = new StringEntity(jsonObj.toString());
            httpPut.setEntity(data);
            CloseableHttpResponse response = httpClient.execute(httpPut);
            if(response.toString().contains("200")){
                return true;
            }else{return false;}
        }catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean PatchUserBalance(String UserID, String sum){
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("bank", sum);

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPatch httpPatch = new HttpPatch(new URI(BaseUrl+UserID));
            httpPatch.addHeader("Accept", "application/json");
            httpPatch.addHeader("Authorization", Token);
            httpPatch.addHeader("Content-Type", "application/x-www-form-urlencoded");
            StringEntity data = new StringEntity(jsonObj.toString());
            httpPatch.setEntity(data);
            CloseableHttpResponse response = httpClient.execute(httpPatch);
            if(response.toString().contains("200")){
                return true;
            }else{return false;}
        }catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean PatchUserBalance(String UserID, String sum, String CashOrBank){
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(CashOrBank, sum);

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPatch httpPatch = new HttpPatch(new URI(BaseUrl+UserID));
            httpPatch.addHeader("Accept", "application/json");
            httpPatch.addHeader("Authorization", Token);
            httpPatch.addHeader("Content-Type", "application/x-www-form-urlencoded");
            StringEntity data = new StringEntity(jsonObj.toString());
            httpPatch.setEntity(data);
            CloseableHttpResponse response = httpClient.execute(httpPatch);
            if(response.toString().contains("200")){
                return true;
            }else{return false;}
        }catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }




}
