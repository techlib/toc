/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;

/**
 *
 * @author alberto
 */
public class KonspektReader {

  public static final Logger LOGGER = Logger.getLogger(KonspektReader.class.getName());
  SolrClient client;
  KospektRecord currentRecord;
  
  final String collection = "dictionaries";
  final String TAG_CS      = "290   ";
  final String TAG_EN      = "490 0 ";
  final String TAG_BROADER = "5909  ";

  public void readFromTxt(InputStream is, SolrClient client) {
    
    this.client = client;
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
      for (String line; (line = br.readLine()) != null;) {
        processLine(line);
      }
      addCurrentRecord();
      client.commit(collection);
      // line is not visible here.
    } catch (IOException | SolrServerException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
  }
  
  private void addCurrentRecord() throws IOException, SolrServerException{
    if(currentRecord != null){
      currentRecord.path = "/" + currentRecord.id;
      currentRecord.path_cze = "/" + currentRecord.csPrefLabel;
      currentRecord.path_eng = "/" + currentRecord.enPrefLabel;
      if (currentRecord.broader != null) {
        currentRecord.path = "/" + currentRecord.broaderId + currentRecord.path;
        currentRecord.path_cze = "/" + currentRecord.broaderId + currentRecord.path_cze;
        currentRecord.path_eng = "/" + currentRecord.broaderId + currentRecord.path_eng;
      }
      client.addBean(collection, currentRecord);
    }
    currentRecord = new KospektRecord();
  }
  
  private void processLine(String line) throws IOException, SolrServerException{
    if(line.startsWith("##")){
      //New record
      addCurrentRecord();
    } else {
      //Get line tag
      String tag = line.substring(0, 6);
      if("001   ".equals(tag)){
        currentRecord.setId("konspekt" + line.substring(6));
      } else if(TAG_CS.equals(tag)){
        currentRecord.csPrefLabel = getCleanValue(line.substring(6));
      } else if(TAG_EN.equals(tag)){
        currentRecord.enPrefLabel = getCleanValue(line.substring(6));
      } else if(TAG_BROADER.equals(tag)){
        currentRecord.broader = getCleanValue(line.substring(6));
        currentRecord.broaderId = getCleanValue(line);
      } 
    }
  }
  
  private String getCleanValue(String raw){
    //raw => $$k10$$a*Chemie. Krystalografie. Mineralogické vědy$$2Konspekt
    // return Chemie. Krystalografie. Mineralogické vědy
    
    try{
      int begin = raw.indexOf("$$a");
      String ret = raw.substring(begin + 3);
      int end = ret.indexOf("$$");
      if(ret.startsWith("*")){
        return ret.substring(1, end);
      } else {
        return ret.substring(0, end);
      }
      
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "Error procesing {0}", raw);
      LOGGER.log(Level.SEVERE, null, ex);
      return null;
    }
  }

}
