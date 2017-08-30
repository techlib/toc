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

  public DictionaryMatch(String name, String matched_text) {
    this.name = name;
    this.matched_text = matched_text;
  }
  
  @Override
  public String toString(){
    return new JSONObject().put("name", name).put("text", matched_text).toString();
  }
}
