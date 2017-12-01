/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk;

import cz.incad.ntk.toc_ntk.morpho.MorphoTagger;
import cz.incad.ntk.toc_ntk.morpho.MorphoToken;
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
  Integer deep = 0;
  String deep_str;
  String text;
  int start_page = 0;
  ArrayList<MorphoToken> mtokens;
  JSONArray jaTokens;

  public TocLine(JSONObject json) {
    this.raw = json.getString("raw");
    this.deep = json.optInt("deep");
    this.deep_str = json.optString("deep_str");
    this.text = json.getString("text");
    this.start_page = json.optInt("start_page");
    mtokens = new ArrayList();
    this.jaTokens = json.optJSONArray("mtokens");
    if(this.jaTokens != null){
      for (int i = 0; i < this.jaTokens.length(); i++) {
        mtokens.add(new MorphoToken(this.jaTokens.getJSONObject(i)));
      }
    }
  }
  
  public JSONObject toJSON(){
    JSONObject json = new JSONObject();
    json.put("raw", this.raw);
    json.put("deep", this.deep);
    json.put("deep_str", this.deep_str);
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
      LOGGER.log(Level.FINE, "pos: {0}", pos);
      if (pos > -1) {
        // Zatim deep pro farmat 1.2.3
        //Musime zpracovat tabulatory
        this.deep_str = str.substring(0, pos);
        this.deep = this.deep_str.split("\\.").length;
        
        this.text = str.substring(pos, page_pos+1).trim();
      } else {
        this.text = str.substring(0, page_pos + 1).trim();
      }
    } else {
      int pos = str.indexOf("\t");
      if (pos > -1) {
        // Treba II.
        this.deep_str = str.substring(0, pos);
        this.deep = this.deep_str.split("\\.").length;
      }
      this.text = str.substring(0, page_pos).trim();
    }
    
    //Clean multiple dots 
    this.text = this.text.replaceAll("\\.+", "\\.").replaceAll("\\s+", " ");
    getMorphoTokens();
  }
  
  /**
   * Clean the text removing problematic characters
   * Morphodita handles better without -
   * Rovnice Rankineova - Hugoniotova => Rovnice Rankineova Hugoniotova
   * @param s
   * @return 
   */
  private String cleanText(String s){
    
    return s.replaceAll(" - ", " ");
  }
  
  private void getMorphoTokens() {
    mtokens = new ArrayList();
    try {
      JSONObject js;
      if(Options.getInstance().getBoolean("cleanBeforeMorphoTag", true)){
        String s = cleanText(text);
        LOGGER.log(Level.FINE, s);
        js = MorphoTagger.getTags(s);
      } else {
        js = MorphoTagger.getTags(text);
      }
      LOGGER.log(Level.FINE, js.toString());
      if(js.has("result")){
        this.jaTokens = js.getJSONArray("result");
        for (int i = 0; i < this.jaTokens.length(); i++) {
          mtokens.add(new MorphoToken(this.jaTokens.getJSONObject(i)));
        }
      }
    } catch (IOException | JSONException ex) {
      LOGGER.log(Level.SEVERE, null,ex);
    }
  }
  
  private void getMorphoTokensRest() {
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
