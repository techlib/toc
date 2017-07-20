package cz.incad.ntk.toc_ntk;

import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * It represents one token from MorphoDiTa tag result
 *
 * @author alberto
 */
public class MorphoToken {

  public static final Logger LOGGER = Logger.getLogger(MorphoToken.class.getName());
  JSONObject mtoken;
  MorphoTag tag;
  String lemma;
  String lemmaSimple;

  public MorphoToken(JSONObject token) {
    this.mtoken = token;
    this.tag = new MorphoTag(this.mtoken.getString("tag"));
    this.lemma = this.mtoken.getString("lemma");
    int pos = lemma.indexOf("-");
    if (pos > -1) {
      lemmaSimple = lemma.substring(0, pos);
    } else {

      pos = lemma.indexOf("_");
      if (pos > -1) {
        lemmaSimple = lemma.substring(0, pos);
      } else {
        lemmaSimple = lemma;
      }
    }
  }

  public String getToken() {
    return this.mtoken.getString("token");
  }

  public String getLemma() {
    return this.lemma;
  }

  public String getLemmaSimple() {
    return this.lemmaSimple;
  }

  public MorphoTag getTag() {
    return tag;
  }

  public JSONObject getJson() {
    return mtoken;
  }
  
  public boolean isProperNoun(){
    return this.lemma.contains("_;Y") || this.lemma.contains("_;S");
  }

}
