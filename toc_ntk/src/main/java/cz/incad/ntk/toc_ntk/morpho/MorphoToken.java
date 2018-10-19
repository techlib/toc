package cz.incad.ntk.toc_ntk.morpho;

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
  String token;
  int token_start;
  int token_end;
  String lemmaSimple;

  public MorphoToken(JSONObject token) {
    this.mtoken = token;
    this.tag = new MorphoTag(this.mtoken.getString("tag"));
    this.lemma = this.mtoken.getString("lemma");
    this.token = this.mtoken.getString("token");
    this.token_start = this.mtoken.getInt("token_start");
    this.token_end = this.mtoken.getInt("token_end");
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
  
  public boolean hasSpace(){
    return this.mtoken.getString("space").equals(" ");
  }
  
  public boolean isParenthesis(){
    return this.token.equals("(");
  }
  
  public boolean isSingleWord(){
    return this.token.length() < 2;
  }

  public String getToken() {
    return this.mtoken.getString("token");
  }

  public int getTokenStart() {
    return this.mtoken.getInt("token_start");
  }

  public int getTokenEnd() {
    return this.mtoken.getInt("token_end");
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
    return this.lemma.contains("_;Y") || 
            this.lemma.contains("_;S") ||
            this.lemma.contains("_;K") ||
            this.lemma.contains("_;R") ||
            this.lemma.contains("_;m") ||
            this.lemma.contains("_;G");
  }

}
