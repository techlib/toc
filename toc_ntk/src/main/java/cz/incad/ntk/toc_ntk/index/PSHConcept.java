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

/**
 *
 * @author alberto
 */
public class PSHConcept {
  
  public static final Logger LOGGER = Logger.getLogger(PSHConcept.class.getName());
  
    @Field
    String id;
    
    @Field
    String uri;
    
    @Field("dict")
    String dict = "PSH";

    @Field("csPrefLabel")
    List<String> csPrefLabel = new ArrayList<>();
    
    @Field("csAltLabel")
    List<String> csAltLabel = new ArrayList<>();

    @Field("enPrefLabel")
    List<String> enPrefLabel = new ArrayList<>();

    @Field("enAltLabel")
    List<String> enAltLabel = new ArrayList<>() ;

    @Field("broader")
    String broader;

    @Field("narrower")
    List<String> narrower = new ArrayList<>() ;
    
    @Field
    String path;

  void setUri(String uri) {
    this.uri = uri;
  }

  void setId(String id) {
    this.id = id;
  }
  

  void addAltLabel(String lang, String label) {
    if("en".equals(lang)){
      enAltLabel.add(label) ;
    } else {
      csAltLabel.add(label) ;
    }
  }

  void addPrefLabel(String lang, String label) {
    if("en".equals(lang)){
      enPrefLabel.add(label) ;
    } else {
      csPrefLabel.add(label) ;
    }
  }
  
}
