package cz.incad.ntk.toc_ntk;

import cz.incad.ntk.toc_ntk.index.SolrService;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
  
  
  
  //Keeps when candidate was found in title
  boolean inTitle;
  
  //Keeps when candidate was found in blacklist
  boolean blackListed;
  
  //Keeps when candidate was found in dictionary
  boolean isMatched;
  
  //List of dictionaries in which candidate was found;
  List<DictionaryMatch> matches = new ArrayList<>();
  
  CandidateType type;
  boolean hasProperNoun;
  float score;
  
  //Keeps how many times the canddate was found in the ToC 
  int found;
  
  //List of deeps in which the candidate was found
  List<Integer> deeps = new ArrayList<>();
  
  //Pages extents
  List<Integer> extents = new ArrayList<>();

  public Candidate(String text, CandidateType type) {
    this(text, type, false);
  }

  public Candidate(String text, CandidateType type, boolean hasProperNoun) {
    this.text = text.trim();
    this.type = type;
    this.found = 1;
    this.hasProperNoun = hasProperNoun;
  }
  
  public void addDeep(Integer deep){
    this.deeps.add(deep);
  }
  
  public void addExtent(Integer extent){
    this.extents.add(extent);
  }

  public JSONObject toJSON() {
    JSONObject ret = new JSONObject();
    ret.put("text", text);
    ret.put("blacklisted", blackListed);
    ret.put("inTitle", inTitle);
    ret.put("isMatched", isMatched);
//    ret.put("matched_text", matched_text);
    for (DictionaryMatch dm : matches) {
      ret.append("dictionaries", dm.toJSON());
    }
    for (Integer deep : deeps) {
      ret.append("deeps", deep);
    }
    for (Integer extent : extents) {
      ret.append("extents", extent);
    }
    ret.put("type", type);
    ret.put("hasProperNoun", hasProperNoun);
    ret.put("score", score);
    ret.put("found", found);
    return ret;
  }
  
  public void setBlackListed(){
    this.blackListed = SolrService.isInBlackList(this.text);;
  }

  public void match() {
    if (type == CandidateType.DICTIONARY_WORD) {
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
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }

  }

  public float score(ScoreConfig sc) {
    if(this.blackListed){
      return 0;
    }
    this.score = this.found * sc.found;
    if (isMatched) {

      this.score = this.score * sc.matched;
      if (text.split(" ").length > 1) {
        this.score = this.score * sc.multiple;
      }

      for (DictionaryMatch dm : this.matches) {
        if (sc.dictionaries.containsKey(dm.name)) {
          this.score = (float) (this.score * sc.dictionaries.get(dm.name));
        }
      }
    }
    if (hasProperNoun) {
      this.score = this.score * sc.hasProperNoun;
    }

    if (type == CandidateType.DICTIONARY_WORD) {
      this.score = this.score * sc.isDictionaryWord;
    }
    
    this.score += this.extents.get(0) * sc.extent;

    return this.score;
  }
}
