/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk.morpho;

import cz.cuni.mff.ufal.morphodita.Forms;
import cz.cuni.mff.ufal.morphodita.TaggedLemma;
import cz.cuni.mff.ufal.morphodita.TaggedLemmas;
import cz.cuni.mff.ufal.morphodita.TokenRange;
import cz.cuni.mff.ufal.morphodita.TokenRanges;
import cz.cuni.mff.ufal.morphodita.Tokenizer;
import cz.incad.ntk.toc_ntk.InitServlet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class MorphoTagger {

  public static final Logger LOGGER = Logger.getLogger(MorphoTagger.class.getName());

  public static JSONObject getTags(String text, String lang) {
    JSONObject ret = new JSONObject();
    Forms forms = new Forms();
    TaggedLemmas lemmas = new TaggedLemmas();
    TokenRanges tokens = new TokenRanges();
//    Scanner reader = new Scanner(System.in);
    Tokenizer tokenizer = InitServlet.tagger.get(lang).newTokenizer();
    if (tokenizer == null) {
      LOGGER.log(Level.SEVERE, "No tokenizer is defined for the supplied model!");
      ret.put("error", "No tokenizer is defined for the supplied model!");
    } else {

      // Tokenize and tag
      tokenizer.setText(text);
      int t = 0;
      while (tokenizer.nextSentence(forms, tokens)) {
        

        InitServlet.tagger.get(lang).tag(forms, lemmas);

        for (int i = 0; i < lemmas.size(); i++) {
          JSONObject jotoken = new JSONObject();
          TaggedLemma lemma = lemmas.get(i);
          TokenRange token = tokens.get(i);
          int token_start = (int) token.getStart(), token_end = token_start + (int) token.getLength();
          String space = "";
          if(i + 1 < lemmas.size()){
            space = text.substring(token_end, (int)tokens.get(i+1).getStart());
          }
          jotoken.put("lemma", lemma.getLemma())
                  .put("tag", lemma.getTag())
                  .put("space", space)
                  .put("token", text.substring(token_start, token_end))
                  .put("token_start", token_start)
                  .put("token_end", token_end);
          t = token_end;
          ret.append("result", jotoken);
          
        }

      }
      forms.clear();
      lemmas.clear();
      tokens.clear();
      tokenizer.delete();
      tokenizer = null;
      //ret.put("tokens", jatokens);
//      out.print(encodeEntities(text.substring(t)));
    }
    return ret;
  }
}
