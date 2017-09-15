/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
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
  Map<String, Double> dictionaries = new HashMap<>();

  public void fromJSON(JSONObject json) {
    found = (float) json.getDouble("found");
    matched = (float) json.getDouble("matched");
    multiple = (float) json.getDouble("multiple");
    hasProperNoun = (float) json.getDouble("hasProperNoun");
    isDictionaryWord = (float) json.getDouble("isDictionaryWord");

    if (json.has("dictionaries")) {
      JSONObject jo = json.getJSONObject("dictionaries");

      Iterator<String> keysItr = jo.keys();
      while (keysItr.hasNext()) {
        String key = keysItr.next();
        dictionaries.put(key, jo.getDouble(key));
      }
    }
  }
}
