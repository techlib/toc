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
public class ScoreConfig {
  float found = 1.0f;
  float matched = 2.0f;
  float multiple = 3.0f;
  float hasProperNoun = 5.0f;
  float isDictionaryWord = 2.0f;
  
  public void fromJSON(JSONObject json){
    found = (float) json.getDouble("found");
    matched = (float) json.getDouble("matched");
    multiple = (float) json.getDouble("multiple");
    hasProperNoun = (float) json.getDouble("hasProperNoun");
    isDictionaryWord = (float) json.getDouble("isDictionaryWord");
  }
}
