/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk;

import org.apache.solr.client.solrj.beans.Field;

/**
 *
 * @author alberto
 */
class KospektRecord {
  
    @Field
    String id;
    
    @Field
    String key_en;
    
    @Field
    String key_cs;

  void setId(String id) {
    this.id = id;
  }

  void setKeyEn(String key) {
    this.key_en = key;
  }

  void setKeyCs(String key) {
    this.key_cs = key;
  }
  
}
