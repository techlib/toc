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

/**
 *
 * @author alberto
 */
public class SolrService {
  
  public static SolrDocument match(String text) {
    try {
      String urlString = "http://localhost:8983/solr/slovnik";
      SolrClient solr = new HttpSolrClient.Builder(urlString).build();
      SolrQuery query = new SolrQuery();
      query.setQuery("\"" + text.toLowerCase() + "\"");
      query.set("defType", "edismax");
      if(text.indexOf(" ") > 0){
        query.set("qf", "key_cz");
      } else {
        query.set("qf", "key_lower");
      }
      query.set("mm", "80%");
      QueryResponse response = solr.query(query);
      if(response.getResults().getNumFound() > 0){
        return response.getResults().get(0);
      }
    } catch (SolrServerException | IOException ex) {
      Logger.getLogger(Candidate.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }
}
