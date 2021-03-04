/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk.index;

import java.util.ArrayList;
import java.util.List;
import org.apache.solr.client.solrj.beans.Field;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
class KospektRecord {
  
    @Field
    String id;
    
    @Field("dict")
    String dict = "konspekt";

    @Field("prefLabel_cze")
    String csPrefLabel;
    
    @Field("altLabel_cze")
    List<String> csAltLabel = new ArrayList<>();

    @Field("prefLabel_eng")
    String enPrefLabel;

    @Field("altLabel_eng")
    List<String> enAltLabel = new ArrayList<>() ;

    @Field("broader")
    String broader;
    String broaderId;

    @Field("narrower")
    List<String> narrower = new ArrayList<>() ;
    
    @Field
    String path;
    
    @Field("path_cze")
    public String path_cze;
    
    @Field("path_eng")
    public String path_eng;

  void setId(String id) {
    this.id = id;
  }
    
    JSONObject toJSON() {
      JSONObject ret = new JSONObject();
      ret.put("id", id);
      ret.put("broader", broader);
      ret.put("narrower", narrower);
      ret.put("prefLabel_cze", csPrefLabel);
      ret.put("prefLabel_eng", enPrefLabel);
      ret.put("path", path);
      ret.put("path_eng", path_eng);
      ret.put("path_cze", path_cze);
      return ret;
    }
  
}
