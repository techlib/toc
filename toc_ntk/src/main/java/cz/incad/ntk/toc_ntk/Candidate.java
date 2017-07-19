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
public class Candidate {
  
  
  public enum CandidateType {
    NOUN, NOUN_GENITIVES, ADJETIVES_NOUN
  }

  String text;
  boolean isMatched;
  String matched_text;
  String dictionary;
  CandidateType type;
  int score;
  int found;

  public Candidate(String text, CandidateType type) {
    this.text = text.trim();
    this.type = type;
    this.found = 1;
  }

  public void match() {
    try {
      String urlString = "http://localhost:8983/solr/slovnik";
      SolrClient solr = new HttpSolrClient.Builder(urlString).build();
      SolrQuery query = new SolrQuery();
      query.setQuery("\"" + this.text.toLowerCase() + "\"");
      query.set("defType", "edismax");
      if(this.text.indexOf(" ") > 0){
        query.set("qf", "key_cz");
      } else {
        query.set("qf", "key_lower");
      }
      query.set("mm", "80%");
      QueryResponse response = solr.query(query);
      if(response.getResults().getNumFound() > 0){
        this.isMatched = true;
        SolrDocument doc = response.getResults().get(0);
        this.matched_text = doc.getFirstValue("key").toString();
        this.dictionary = doc.getFirstValue("slovnik").toString();
      }
    } catch (SolrServerException ex) {
      Logger.getLogger(Candidate.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(Candidate.class.getName()).log(Level.SEVERE, null, ex);
    }
    
  }
  
  public int score(){
    this.score = this.found;
    if (isMatched) {
      this.score += 5;
      if (text.split(" ").length > 1) {
        this.score += 10;
      }
    }
    
    return this.score;
  }
}
