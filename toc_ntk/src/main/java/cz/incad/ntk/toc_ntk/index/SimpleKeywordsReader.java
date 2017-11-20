/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

/**
 *
 * @author alberto
 */
public class SimpleKeywordsReader {

  public static final Logger LOGGER = Logger.getLogger(SimpleKeywordsReader.class.getName());
  SolrClient client;
  
  final int ID_LENGTH = 9;
  final String separator = " ";
  
  String slovnik;
  
  SimpleKeywordsReader(String slovnik){
    this.slovnik = slovnik;
  }

  public void readFromTxt(InputStream is, SolrClient client) {
    
    this.client = client;
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
      for (String line; (line = br.readLine()) != null;) {
        processLine(line);
      }
      client.commit();
    } catch (IOException | SolrServerException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
  }
  
  private void processLine(String line) throws IOException, SolrServerException{
    SolrInputDocument doc = new SolrInputDocument();
   
    doc.addField("id", line.substring(0,ID_LENGTH));
    doc.addField("key", line.substring(ID_LENGTH + 1));
    doc.addField("slovnik", slovnik);
    client.add(doc);
  }

}
