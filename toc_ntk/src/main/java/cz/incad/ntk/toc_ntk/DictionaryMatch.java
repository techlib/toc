/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk;

import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class DictionaryMatch {

  String name;
  String matched_text;
  JSONObject payload;

  public DictionaryMatch(String name, String matched_text, JSONObject payload) {
    this.name = name;
    this.matched_text = matched_text;
    this.payload = payload;
  }
  
  public JSONObject toJSON() {
    JSONObject ret = new JSONObject();
    ret.put("text", matched_text).put("name", name).put("payload", payload);
    return ret;
  }
  
  @Override
  public String toString(){
    return new JSONObject().put("name", name).put("text", matched_text).put("payload", payload).toString();
  }
}
