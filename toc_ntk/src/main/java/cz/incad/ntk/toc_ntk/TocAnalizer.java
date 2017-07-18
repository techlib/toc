package cz.incad.ntk.toc_ntk;

import cz.incad.utils.RESTHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Methods to analyzing files using MorphoDiTa API
 *
 * @author alberto
 */
public class TocAnalizer {

  public static final Logger LOGGER = Logger.getLogger(TocAnalizer.class.getName());

  public void processFolder(String path) {

  }

  public ArrayList<MorphoToken> analyzeLine(String line) {
    ArrayList mtokens = new ArrayList();
    try {
      Options opts = Options.getInstance();

      LOGGER.log(Level.INFO, "requesting data {0}", line);

      String host = opts.getString("morpho.host");
      URI uri = new URIBuilder().setScheme("http")
              .setCharset(Charset.forName("UTF-8"))
              .setHost(host)
              .setPath("/tag")
              .setParameter("data", line)
              .setParameter("output", "json")
              .build();

      LOGGER.log(Level.INFO, "requesting url {0}", uri.toString());
      InputStream inputStream = RESTHelper.inputStream(uri.toString());

      String json = org.apache.commons.io.IOUtils.toString(inputStream, Charset.forName("UTF-8"));

      JSONObject js = new JSONObject(json);
      JSONArray tokens = js.getJSONArray("result").getJSONArray(0);

      for (int i = 0; i < tokens.length(); i++) {
        mtokens.add(new MorphoToken(tokens.getJSONObject(i)));
      }
    } catch (IOException | JSONException | URISyntaxException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
    return mtokens;
  }

  /**
   * Find the keyword candidates for the Ordered list of tokens
   *
   * @param tokens
   * @return A list of candidates
   */
  public List<Candidate> findCandidates(ArrayList<MorphoToken> tokens) {

    List<Candidate> candidates = new ArrayList<>();

    //First case: we propose all nouns
    for (MorphoToken token : tokens) {
      if (token.getTag().isNoun()) {
        candidates.add(new Candidate(token.getLemmaSimple()));
      }
    }

    //All adjectives followed by nouns
    String str = "";
    boolean hasAdjective = false;
    for (MorphoToken token : tokens) {
      if (token.getTag().isAdjective()) {
        str += token.getToken() + " ";
        hasAdjective = true;
      } else if (token.getTag().isNoun() && hasAdjective) {
        str += token.getToken();
        candidates.add(new Candidate(str));
        str = "";
        hasAdjective = false;
      } else {
        str = "";
        hasAdjective = false;
      }
    }

    //Nouns followed by words in genitive
    str = "";
    boolean hasNoun = false;
    for (MorphoToken token : tokens) {
      if (token.getTag().isNoun() && !hasNoun) {
        str = token.getToken();
        hasNoun = true;
      } else if (token.getTag().isGenitive() && hasNoun) {
        str += " " + token.getToken();
      } else {
        if (str.length() > 0) {
          candidates.add(new Candidate(str));
        }
        str = "";
        hasNoun = false;
      }
    }
    if (str.length() > 0) {
      candidates.add(new Candidate(str));
    }

    return candidates;
  }

  //Parse file and get lines.
  //Line should starts and ends with digit
  //In other case we join the lines 
  public List<TocLine> getLines(File f) {
    FileReader in = null;
    List<TocLine> lines = new ArrayList<>();
    try {
      String previous = "";
      in = new FileReader(f);
      BufferedReader br = new BufferedReader(in);
      String line = br.readLine();
      while (line != null) {
        line = line.trim();
        if (Character.isDigit(line.charAt(line.length() - 1))) {
          //This is the end
          line = previous + " " + line;
          lines.add(new TocLine(line.trim()));
          previous = "";
        } else if (Character.isDigit(line.charAt(0))) {
          // This is the begining or we are continuing
          if (previous.equals("")) {
            // new line
            previous = line;
          } else {
            // continuing
            previous += line;
          }
        } else {
          previous += line;
//          line = lines.get(lines.size() - 1) + line;
//          lines.set(lines.size() - 1, line);
        }
        line = br.readLine();
      }
      if (!previous.equals("")) {
        lines.add(new TocLine(previous));
      }
      in.close();
    } catch (FileNotFoundException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (IOException ex) {
        Logger.getLogger(TocAnalizer.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return lines;
  }

  public Map<String, Candidate> analyze(File f) {
    Map<String, Candidate> candidates = new HashMap<>();
    List<TocLine> lines = getLines(f);
    for (TocLine line : lines) {
      ArrayList<MorphoToken> tokens = analyzeLine(line.text);
      for (MorphoToken token : tokens) {

      }
      for (Candidate c : findCandidates(tokens)) {
        if(candidates.containsKey(c.text)){
          candidates.get(c.text).found++;
        } else {
          candidates.put(c.text, c);
        }
      }
    }
    return candidates;
  }
}
