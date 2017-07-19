/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk;

import static cz.incad.ntk.toc_ntk.TocAnalizer.LOGGER;
import cz.incad.utils.RESTHelper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class TocLine {

  public static final Logger LOGGER = Logger.getLogger(TocLine.class.getName());

  String raw;
  String deep;
  String text;
  int start_page;
  ArrayList<MorphoToken> mtokens;
  JSONArray jaTokens;

  public TocLine(JSONObject json) {
    this.raw = json.getString("raw");
    this.deep = json.getString("deep");
    this.text = json.getString("text");
    this.start_page = json.getInt("start_page");
    mtokens = new ArrayList();
    this.jaTokens = json.getJSONArray("mtokens");
    for (int i = 0; i < this.jaTokens.length(); i++) {
      mtokens.add(new MorphoToken(this.jaTokens.getJSONObject(i)));
    }
  }
  
  public JSONObject toJSON(){
    JSONObject json = new JSONObject();
    json.put("raw", this.raw);
    json.put("deep", this.deep);
    json.put("text", this.text);
    json.put("start_page", this.start_page);
    json.put("mtokens", this.jaTokens);
    return json;
  }
  
  public TocLine(String rawStr) {
    this.raw = rawStr;
    String str = rawStr.trim();
    if(str.codePointAt(0) == 65279){
      str = str.substring(1);
    }
    int page_pos = str.length() - 1;
    while (page_pos > -1 && Character.isDigit(str.charAt(page_pos))) {
      page_pos--;
    }

    try {
      this.start_page = Integer.parseInt(str.substring(page_pos + 1));
    } catch (NumberFormatException ex) {
      LOGGER.log(Level.INFO, "no page start for {0}", str);
    }
    
    if (Character.isDigit(str.charAt(0))) {
      int pos = str.indexOf("\t");
      if (pos > -1) {
        pos = Math.min(pos, Math.max(str.indexOf(" "), 0));
      } else {
        pos = str.indexOf(" ");
      }
      if (pos > -1) {
        this.deep = str.substring(0, pos);
        this.text = str.substring(pos, page_pos+1).trim();
      } else {
        this.text = str.substring(0, page_pos + 1).trim();
      }
    } else {
      this.text = str.substring(0, page_pos).trim();
    }
    getMorphoTokens();
  }
  
  
  

  private void getMorphoTokens() {
    mtokens = new ArrayList();
    try {
      Options opts = Options.getInstance();

      //LOGGER.log(Level.INFO, "requesting data {0}", line);

      String host = opts.getString("morpho.host");
      URI uri = new URIBuilder().setScheme("http")
              .setCharset(Charset.forName("UTF-8"))
              .setHost(host)
              .setPath("/tag")
              .setParameter("data", text)
              .setParameter("output", "json")
              .build();

      LOGGER.log(Level.INFO, "requesting url {0}", uri.toString());
      InputStream inputStream = RESTHelper.inputStream(uri.toString());

      String json = org.apache.commons.io.IOUtils.toString(inputStream, Charset.forName("UTF-8"));

      JSONObject js = new JSONObject(json);
      this.jaTokens = js.getJSONArray("result").getJSONArray(0);

      for (int i = 0; i < this.jaTokens.length(); i++) {
        mtokens.add(new MorphoToken(this.jaTokens.getJSONObject(i)));
      }
    } catch (IOException | JSONException | URISyntaxException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
  }
}
