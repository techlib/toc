/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

/**
 *
 * @author alberto
 */
public class SolrService {
  
  static final Logger LOGGER = Logger.getLogger(Candidate.class.getName());
  static final String urlString = "http://localhost:8983/solr";
  
  public static void addToBlackList(String key) throws SolrServerException, IOException{
    
      SolrClient solr = new HttpSolrClient.Builder(urlString).build();
      SolrInputDocument idoc = new SolrInputDocument();
      idoc.addField("key", key);
      LOGGER.info(idoc.toString());
      solr.add("blacklist", idoc, 100);
  }
  
  
  
  public static void addToNerizene(String key) throws SolrServerException, IOException{
    
      SolrClient solr = new HttpSolrClient.Builder(urlString).build();
      SolrInputDocument idoc = new SolrInputDocument();
      idoc.addField("key", key);
      LOGGER.info(idoc.toString());
      solr.add("nerizene", idoc, 100);
  }
  
  public static boolean isInBlackList(String text){
    SolrDocumentList docs = new SolrDocumentList();
    try {
      SolrClient solr = new HttpSolrClient.Builder(urlString).build();
      SolrQuery query = new SolrQuery();
      
      //Search in blacklist
      query.setQuery("\"" + text.toLowerCase() + "\"");
      query.set("defType", "edismax");
      query.set("qf", "key");
      query.set("mm", "100%");
      QueryResponse response = solr.query("blacklist", query);
      if(response.getResults().getNumFound() > 0){
        return true;
      }
      
    } catch (SolrServerException | IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      
    }
    return false;
  }
  
  public static SolrDocumentList findInDictionaries(String text) {
      SolrDocumentList docs = new SolrDocumentList();
    try {
      SolrClient solr = new HttpSolrClient.Builder(urlString).build();
      SolrQuery query = new SolrQuery();
      
      //Search in simple dictionaries
      query.setQuery("\"" + text.toLowerCase() + "\"");
      query.set("defType", "edismax");
      if(text.indexOf(" ") > 0){
        query.set("qf", "key_cz");
      } else {
        query.set("qf", "key_lower");
      }
      query.set("mm", "80%");
      QueryResponse response = solr.query("slovnik", query);
      if(response.getResults().getNumFound() > 0){
        docs.add(response.getResults().get(0));
      }
      
      
      //Search in psh
      query = new SolrQuery();
      query.setQuery("\"" + text.toLowerCase() + "\"");
      query.set("defType", "edismax");
      if(text.indexOf(" ") > 0){
        query.set("qf", "key_cz");
      } else {
        query.set("qf", "key_cz");
      }
      query.set("mm", "80%");
      QueryResponse pshResp = solr.query("psh", query);
      
      if(pshResp.getResults().getNumFound() > 0){
        docs.add(pshResp.getResults().get(0));
      }
      
      //Search in konspekt
      query = new SolrQuery();
      query.setQuery("\"" + text.toLowerCase() + "\"");
      query.set("defType", "edismax");
      if(text.indexOf(" ") > 0){
        query.set("qf", "key_cz");
      } else {
        query.set("qf", "key_lower");
      }
      query.set("mm", "100%");
      response = solr.query("konspekt", query);
      
      if(response.getResults().getNumFound() > 0){
        docs.add(response.getResults().get(0));
      }
      
    } catch (SolrServerException | IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
    return docs;
  }
  
  
}
