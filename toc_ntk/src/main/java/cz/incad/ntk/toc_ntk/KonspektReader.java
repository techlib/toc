/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk;

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

  public void readFromTxt(InputStream is, SolrClient client) {
    
    this.client = client;
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
      for (String line; (line = br.readLine()) != null;) {
        processLine(line);
      }
      addCurrentRecord();
      // line is not visible here.
    } catch (IOException | SolrServerException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
  }
  
  private void addCurrentRecord() throws IOException, SolrServerException{
    if(currentRecord != null){
      client.addBean(currentRecord, 100);
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
        currentRecord.setId(line.substring(6));
      } else if("290   ".equals(tag)){
        currentRecord.setKeyCs(getCleanValue(line.substring(6)));
      } else if("490 0 ".equals(tag)){
        currentRecord.setKeyEn(getCleanValue(line.substring(6)));
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
