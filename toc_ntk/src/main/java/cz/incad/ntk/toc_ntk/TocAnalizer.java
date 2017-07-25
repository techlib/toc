package cz.incad.ntk.toc_ntk;

import cz.incad.ntk.toc_ntk.Candidate.CandidateType;
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
import org.apache.commons.io.FileUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
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

  File tocFile;
  JSONObject tocFileData;

  public void processFolder(String path) {

  }

  /**
   * Find the keyword candidates for the Ordered list of tokens
   *
   * @param tokens
   * @return A list of candidates
   */
  public List<Candidate> findCandidates(TocLine line) {

    List<Candidate> candidates = new ArrayList<>();

    //First case: we propose all nouns
    for (MorphoToken token : line.mtokens) {
      if (token.getTag().isNoun()) {
        if (token.isProperNoun()) {
          candidates.add(new Candidate(token.getLemmaSimple(), CandidateType.PROPERNOUN, true));
        } else {
          candidates.add(new Candidate(token.getLemmaSimple(), CandidateType.NOUN));
        }
      }
    }

    //All adjectives followed by nouns
    String str = "";
    boolean hasAdjective = false;
    boolean hasProperNoun = false;
    for (MorphoToken token : line.mtokens) {
      if (token.getTag().isAdjective()) {
        str += token.getToken() + " ";
        hasAdjective = true;
        hasProperNoun = token.isProperNoun() || hasProperNoun;
      } else if (token.getTag().isNoun() && hasAdjective) {
        str += token.getToken();
        hasProperNoun = token.isProperNoun() || hasProperNoun;
        candidates.add(new Candidate(str, CandidateType.ADJETIVES_NOUN, hasProperNoun));
        str = "";
        hasAdjective = false;
        hasProperNoun = false;
      } else {
        str = "";
        hasAdjective = false;
        hasProperNoun = false;
      }
    }

    //Nouns followed by words in genitive
    str = "";
    boolean hasNoun = false;
    hasProperNoun = false;
    boolean hasGenitive = false;
    for (MorphoToken token : line.mtokens) {
      if (token.getTag().isNoun() && !hasNoun) {
        str = token.getToken();
        hasNoun = true;
        hasProperNoun = token.isProperNoun() || hasProperNoun;
      } else if (token.getTag().isGenitive() && hasNoun) {
        hasProperNoun = token.isProperNoun() || hasProperNoun;
        str += " " + token.getToken();
        hasGenitive = true;
      } else {
        if (str.length() > 0 && hasGenitive) {
          candidates.add(new Candidate(str, CandidateType.NOUN_GENITIVES, hasProperNoun));
          hasGenitive = false;
        }
        str = "";
        hasNoun = false;
        hasProperNoun = false;
      }
    }
    if (str.length() > 0 && hasGenitive) {
      candidates.add(new Candidate(str, CandidateType.NOUN_GENITIVES, hasProperNoun));
    }

    //MorphoDiTa split some abbreviations (acronyns). So we will 
    // search words distinct than tokens in our keywords dictionary
    String[] words = line.text.split(" ");
    for (String s : words) {
      String word = s.replaceAll("\\.+", "\\.");
      if (!hasWord(line, word)) {
        //Try to find
        SolrDocumentList docs = SolrService.findInDictionaries(word);
        if (!docs.isEmpty()) {
          Candidate c = new Candidate(word, CandidateType.DICTIONARY_WORD);
          
          c.matched_text = docs.get(0).getFirstValue("key").toString();
          c.dictionary = docs.get(0).getFirstValue("slovnik").toString();
          candidates.add(c);
        }
      }
    }
    return candidates;
  }

  private boolean hasWord(TocLine line, String word) {

    for (MorphoToken token : line.mtokens) {
      if (word.equals(token.getToken())) {
        return true;
      }
    }
    return false;
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

  public List<TocLine> getLinesFromSaved() {
    List<TocLine> lines = new ArrayList<>();
    try {
      String t = FileUtils.readFileToString(tocFile, Charset.forName("UTF-8"));
      tocFileData = new JSONObject(t);
      JSONArray jalines = tocFileData.getJSONArray("lines");
      for (int i = 0; i < jalines.length(); i++) {
        lines.add(new TocLine(jalines.getJSONObject(i)));
      }

    } catch (FileNotFoundException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
    return lines;
  }

  private boolean loadFromSaved(String filename) {
    try {
      String dir = Options.getInstance().getString("saved_tocs_dir");
      tocFile = new File(dir + filename);
      return tocFile.exists();
    } catch (IOException | JSONException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
    return false;
  }

  private void save(List<TocLine> lines) {
    try {
      tocFileData = new JSONObject();
      for (TocLine line : lines) {
        tocFileData.append("lines", line.toJSON());
      }
      FileUtils.writeStringToFile(tocFile, tocFileData.toString(2), Charset.forName("UTF-8"));
    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
  }

  public void analyze(File f, Map<String, Candidate> candidates) {
    List<TocLine> lines;
    if (loadFromSaved(f.getName())) {
      lines = getLinesFromSaved();
    } else {
      lines = getLines(f);
      save(lines);
    }
    for (TocLine line : lines) {
      for (MorphoToken token : line.mtokens) {

      }
      for (Candidate c : findCandidates(line)) {
        if (candidates.containsKey(c.text)) {
          candidates.get(c.text).found++;
        } else {
          candidates.put(c.text, c);
          c.match();
        }
      }
    }
  }

  public Map<String, Candidate> analyzeFolder(String foldername) {
    Map<String, Candidate> candidates = new HashMap<>();
    File dir = new File(foldername);
    String[] extensions = new String[]{"txt"};
    List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, false);
    for (File f : files) {
      analyze(f, candidates);
    }
    return candidates;
  }
}
