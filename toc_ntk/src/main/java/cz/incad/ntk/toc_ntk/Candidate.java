package cz.incad.ntk.toc_ntk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class Candidate {
  
  static final Logger LOGGER = Logger.getLogger(Candidate.class.getName());
  
  
  public enum CandidateType {
    NOUN, PROPERNOUN, NOUN_GENITIVES, ADJETIVES_NOUN, DICTIONARY_WORD
  }

  String text;
  boolean isMatched;
//  String matched_text;
  List<DictionaryMatch> matches = new ArrayList<>();
  CandidateType type;
  boolean hasProperNoun;
  float score;
  int found;

  public Candidate(String text, CandidateType type) {
    this(text, type, false);
  }

  public Candidate(String text, CandidateType type, boolean hasProperNoun) {
    this.text = text.trim();
    this.type = type;
    this.found = 1;
    this.hasProperNoun = hasProperNoun;
  }
  
  
  public JSONObject toJSON(){
    JSONObject ret = new JSONObject();
    ret.put("text", text);
    ret.put("isMatched", isMatched);
//    ret.put("matched_text", matched_text);
for(DictionaryMatch dm : matches){
    ret.append("dictionaries", dm.name);
}
    ret.put("type", type);
    ret.put("hasProperNoun", hasProperNoun);
    ret.put("score", score);
    ret.put("found", found);
    return ret;
  }
  
  
  
  public void match() {
    if(type == CandidateType.DICTIONARY_WORD){
      return;
    }
    try {
      SolrDocumentList docs = SolrService.findInDictionaries(this.text);
        if (!docs.isEmpty()) {
          
          this.isMatched = true;
          for (SolrDocument doc : docs) {
            this.matches.add(
                    new DictionaryMatch(doc.getFirstValue("slovnik").toString(),
                            doc.getFirstValue("key_cz").toString()));
          }
        }
        
//      String urlString = "http://localhost:8983/solr/slovnik";
//      SolrClient solr = new HttpSolrClient.Builder(urlString).build();
//      SolrQuery query = new SolrQuery();
//      query.setQuery("\"" + this.text.toLowerCase() + "\"");
//      query.set("defType", "edismax");
//      if(this.text.indexOf(" ") > 0){
//        query.set("qf", "key_cz");
//      } else {
//        query.set("qf", "key_lower");
//      }
//      query.set("mm", "80%");
//      QueryResponse response = solr.query(query);
//      if(response.getResults().getNumFound() > 0){
//        this.isMatched = true;
//        SolrDocument doc = response.getResults().get(0);
//        
//        this.matches.add(
//                new DictionaryMatch(doc.getFirstValue("slovnik").toString(), 
//                        doc.getFirstValue("key").toString()));
//      }
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
    
  }
  
  public float score(ScoreConfig sc){
    this.score = this.found * sc.found;
    if (isMatched) {
      this.score = this.score * sc.matched;
      if (text.split(" ").length > 1) {
        this.score = this.score * sc.multiple;
      }
    }
    if(hasProperNoun){
      this.score = this.score * sc.hasProperNoun;
    }
    
    if(type == CandidateType.DICTIONARY_WORD){
      this.score = this.score * sc.isDictionaryWord;
    }
    
    return this.score;
  }
}
