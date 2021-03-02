/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk.index;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.beans.Field;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class PSHConcept {
  
  public static final Logger LOGGER = Logger.getLogger(PSHConcept.class.getName());
  
    @Field
    String id;
    
    @Field("dict")
    String dict = "PSH";

    @Field("csPrefLabel")
    String csPrefLabel;
    
    @Field("csAltLabel")
    List<String> csAltLabel = new ArrayList<>();

    @Field("enPrefLabel")
    String enPrefLabel;

    @Field("enAltLabel")
    List<String> enAltLabel = new ArrayList<>() ;

    @Field("broader")
    String broader;

    @Field("narrower")
    List<String> narrower = new ArrayList<>() ;
    
    @Field
    String path;
    
    @Field("path_cze")
    public String path_cze;
    
    @Field("path_eng")
    public String path_eng;
    
    JSONObject toJSON() {
      JSONObject ret = new JSONObject();
      ret.put("id", id);
      ret.put("broader", broader);
      ret.put("narrower", narrower);
      ret.put("csPrefLabel", csPrefLabel);
      ret.put("enPrefLabel", enPrefLabel);
      ret.put("path", path);
      ret.put("path_eng", path_eng);
      ret.put("path_cze", path_cze);
      return ret;
    }


  void setId(String id) {
    this.id = id;
  }
  

  void setAltLabel(String lang, String label) {
    if("en".equals(lang)){
      enAltLabel.add(label) ;
    } else {
      csAltLabel.add(label) ;
    }
  }

  void setPrefLabel(String lang, String label) {
    if("en".equals(lang)){
      enPrefLabel = label ;
    } else {
      csPrefLabel = label ;
    }
  }
  
}
