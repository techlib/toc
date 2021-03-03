package cz.incad.ntk.toc_ntk;

import cz.incad.ntk.toc_ntk.index.SolrService;
import cz.incad.ntk.toc_ntk.morpho.MorphoToken;
import cz.incad.ntk.toc_ntk.Candidate.CandidateType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.noggit.JSONUtil;

/**
 * Methods to analyzing files using MorphoDiTa API
 *
 * @author alberto
 */
public class TocAnalizer {

  public static final Logger LOGGER = Logger.getLogger(TocAnalizer.class.getName());

  File tocFile;
  JSONObject tocFileData;
  String lang = "cs";

  public void processFolder(String path) {

  }

  /**
   * Find the keyword candidates for the Ordered list of tokens
   *
   * @param line
   * @return A list of candidates
   */
  public List<Candidate> findCandidates(TocLine line, int next_start_page, boolean solrTagger) {

    List<Candidate> candidates = new ArrayList<>();
    boolean skypSingle;
    try {
      skypSingle = Options.getInstance().getBoolean("skypSingleWordTokens", true);
    } catch (JSONException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return candidates;
    }

    //First case: we propose all nouns
    for (MorphoToken token : line.mtokens) {
      if (skypSingle && token.isSingleWord()) {
        continue;
      }
      if (token.getTag().isNoun()) {
        Candidate c;
        if (token.isProperNoun()) {
          c = new Candidate(token.getLemmaSimple(), CandidateType.PROPERNOUN, true, token.getTokenStart(), token.getTokenEnd());
        } else {
          c = new Candidate(token.getLemmaSimple(), CandidateType.NOUN, token.getTokenStart(), token.getTokenEnd());
        }
        c.addDeep(line.deep);
        c.addExtent(next_start_page - line.start_page);
        candidates.add(c);

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
        str += token.getToken() + " ";
        hasProperNoun = token.isProperNoun() || hasProperNoun;
        Candidate c = new Candidate(str, CandidateType.ADJETIVES_NOUN, hasProperNoun, token.getTokenStart(), token.getTokenEnd());
        candidates.add(c);
        c.addDeep(line.deep);
        c.addExtent(next_start_page - line.start_page);
        str = "";
        hasAdjective = false;
        hasProperNoun = false;
      } else if (token.isParenthesis()) {
        //Tady bychom mely dat dohromady vsechno v zavorkach
      } else {
        str += token.getToken() + " ";
        //hasAdjective = false;
        //hasProperNoun = false;
      }
    }

    //Nouns followed by words in genitive
    str = "";
    boolean hasNoun = false;
    hasProperNoun = false;
    boolean hasGenitive = false;
    int token_start = -1;
    int token_end = -1;
    for (MorphoToken token : line.mtokens) {
      token_start = token.getTokenStart();
      if (token.getTag().isNoun() && !hasNoun) {
        str = token.getToken();
        hasNoun = true;
        hasProperNoun = token.isProperNoun() || hasProperNoun;
      } else if (token.getTag().isGenitive() && hasNoun) {
        hasProperNoun = token.isProperNoun() || hasProperNoun;
        token_end = token.getTokenEnd();
        str += " " + token.getToken();
        hasGenitive = true;
      } else {
        if (str.length() > 0 && hasGenitive) {
          Candidate c = new Candidate(str, CandidateType.NOUN_GENITIVES, hasProperNoun, token.getTokenStart(), token.getTokenEnd());
          candidates.add(c);
          c.addDeep(line.deep);
          c.addExtent(next_start_page - line.start_page);

          hasGenitive = false;
        }
        str = "";
        hasNoun = false;
        hasProperNoun = false;
      }
    }
    if (str.length() > 0 && hasGenitive) {
      //candidates.add(new Candidate(str, CandidateType.NOUN_GENITIVES, hasProperNoun));
      Candidate c = new Candidate(str, CandidateType.NOUN_GENITIVES, hasProperNoun, token_start, token_end);
      candidates.add(c);
      c.addDeep(line.deep);
      c.addExtent(next_start_page - line.start_page);
    }

    if (!solrTagger) {
      addDictionaryWords(line, candidates, next_start_page);
    }
    return candidates;
  }

  private void addDictionaryWords(TocLine line, List<Candidate> candidates, int next_start_page) {
    //MorphoDiTa split some abbreviations (acronyns). So we will 
    // search words distinct than tokens in our keywords dictionary
    String[] words = line.text.split(" ");
    for (String s : words) {
      //Cistime tecky, zavorky
      String word = s.replaceAll("\\.+", "").replaceAll("\\(", "").replaceAll("\\)", "");
      if (!hasWord(line, word)) {
        //Try to find
        SolrDocumentList docs = SolrService.findInDictionaries(word);
        if (!docs.isEmpty()) {
          Candidate c = new Candidate(word, CandidateType.DICTIONARY_WORD, -1, -1);
          c.addDeep(line.deep);
          c.addExtent(next_start_page - line.start_page);
          c.isMatched = true;
//          c.matched_text = docs.get(0).getFirstValue("key").toString();
//          c.dictionary = docs.get(0).getFirstValue("slovnik").toString();
          for (SolrDocument doc : docs) {

            c.matches.add(
                    new DictionaryMatch(doc.getFirstValue("slovnik").toString(),
                            doc.getFirstValue("key_cz").toString(),
                            new JSONObject(JSONUtil.toJSON(doc))));
          }

          candidates.add(c);
        }
      }
    }
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
    try {
      List<String> strlines = FileUtils.readLines(f, Charset.forName("UTF-8"));
      return getLines(strlines);
    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return new ArrayList<>();
//    } finally {
//      try {
//        if (in != null) {
//          in.close();
//        }
//      } catch (IOException ex) {
//        Logger.getLogger(TocAnalizer.class.getName()).log(Level.SEVERE, null, ex);
//      }
    }
  }

  public List<TocLine> getLines(String tocText) {
    String lines[] = tocText.split("\\r\\n|\\n|\\r");
    return getLines(Arrays.asList(lines));
  }

  public List<TocLine> getLines(List<String> strlines) {
    List<TocLine> lines = new ArrayList<>();
    // List<String> strlines = FileUtils.readLines(f, Charset.forName("UTF-8"));
    String previous = "";
    int lineNumber = 0;
    for (String line : strlines) {
      // if (!line.trim().contains(" ") && !line.trim().contains("\t")) {
      if ("".equals(line.trim())) {
        // Only spaces line
        LOGGER.log(Level.FINE, "Skiping line {0}", line);
        continue;
      }
      if (!line.trim().contains(" ") && !line.trim().contains("\t")) {
        // One word line
        LOGGER.log(Level.INFO, "One word line {0}, {1}", new String[]{previous, line});
        previous += " " + line;
//        if (previous.equals("")) {
//          // new line
//          
//          lines.add(new TocLine(line, ++lineNumber, lang));
//        }
        continue;
      }
      if (Character.isDigit(line.trim().charAt(line.length() - 1))) {
        //This is the end
        line = previous + " " + line;
        lines.add(new TocLine(line, ++lineNumber, lang));

        previous = "";
      } else if (Character.isDigit(line.charAt(0))) {
        // This is the begining or we are continuing
        if (previous.equals("")) {
          // new line
          previous = line;
        } else {
          // continuing
          previous += " " + line;
        }
      } else {
        previous += " " + line;
//          line = lines.get(lines.size() - 1) + line;
//          lines.set(lines.size() - 1, line);
      }
//        line = br.readLine();
    }
    if (!previous.equals("")) {
      lines.add(new TocLine(previous, ++lineNumber, lang));
    }
//      in.close();

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

  private void setTocFile(String filename) throws IOException {
    String dir = Options.getInstance().getString("saved_morpho_dir");
    tocFile = new File(dir + filename);
  }

  private boolean loadFromSaved(String filename) {
    try {
      setTocFile(filename);
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

  /**
   * *
   *
   * @param f the file to analize
   * @param candidates Map of candidates to fill
   * @return the total pages extent
   */
  public int analyzeFile(File f, Map<String, Candidate> candidates, boolean solrTagger) {
    List<TocLine> lines;
    try {
      if (Options.getInstance().getBoolean("useCacheForTocLines", true)) {
        if (loadFromSaved(f.getName())) {
          lines = getLinesFromSaved();
        } else {
          lines = getLines(f);
          save(lines);
        }
      } else {
        lines = getLines(f);
        if (Options.getInstance().getBoolean("saveTocLines", true)) {
          setTocFile(f.getName());
          save(lines);
        }
      }
      return analyzeLines(lines, candidates, solrTagger);
    } catch (IOException | JSONException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return 0;
    }
  }
  
  public int analyzeText(String text, Map<String, Candidate> candidates, boolean solrTagger) {
    List<TocLine> lines = getLines(text);
    return analyzeLines(lines, candidates, solrTagger);
  }

  public int analyzeLines(List<TocLine> lines, Map<String, Candidate> candidates, boolean solrTagger) {
    int next_start_page = 0;
    try {

      //for (TocLine line : lines) {
      for (int i = 0; i < lines.size(); i++) {
        TocLine line = lines.get(i);
        if (i + 1 < lines.size()) {
          next_start_page = lines.get(i + 1).start_page;
        } else {
          //TODO: total pages
          next_start_page = lines.get(i).start_page;
        }
        for (MorphoToken token : line.mtokens) {

        }
        for (Candidate c : findCandidates(line, next_start_page, solrTagger)) {
          if (candidates.containsKey(c.text.toLowerCase())) {
            candidates.get(c.text.toLowerCase()).found++;
          } else {
            candidates.put(c.text.toLowerCase(), c);
            if (!solrTagger) {
              c.setBlackListed();
              if (!c.blackListed) {
                c.match();
              }
            }

          }
        }
      }
    } catch (JSONException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
    return next_start_page;
  }
  
  public void setLang(String lang) {
    this.lang = lang;
  }

  /**
   *
   * @param foldername Name of the folder containing ocr
   * @param info: JSON returned from XServer
   * @return Map of candidates
   */
  public Map<String, Candidate> analyzeFolder(String sysno, String foldername, JSONObject info, boolean solrTagger) {
    Map<String, Candidate> candidates = new HashMap<>();
    lang = XServer.getLanguage(info);
    if (lang == null) {
      lang = "cze";
    }
    
    int totalPages = 0;
    if (solrTagger) {
      String text = FileService.getRawToc(sysno);
      totalPages = analyzeText(text, candidates, solrTagger);
    } else {
      
      File dir = new File(foldername);
      String[] extensions = new String[]{"txt"};
      List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, false);
      for (File f : files) {
        totalPages = analyzeFile(f, candidates, solrTagger);
      }
    }
    
    addCandidatesFromInfo(candidates, info, totalPages, solrTagger);
    return candidates;
  }

  private void addCandidatesFromInfo(Map<String, Candidate> candidates, JSONObject info, int totalPages, boolean solrTagger) {
    String title = XServer.getTitle(info); 
    TocLine tc = new TocLine(title, 0, lang);

    for (Candidate c : findCandidates(tc, totalPages, solrTagger)) {
      if (candidates.containsKey(c.text.toLowerCase())) {
        candidates.get(c.text.toLowerCase()).found++;
        candidates.get(c.text.toLowerCase()).inTitle = true;
        candidates.get(c.text.toLowerCase()).setExtent(totalPages);
      } else {
        c.inTitle = true;
        candidates.put(c.text.toLowerCase(), c);
        if (!solrTagger) {
          c.setBlackListed();
          if (!c.blackListed) {
            c.match();
          }
        }
      }
    }
  }

}
