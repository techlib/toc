/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk.index;

import cz.incad.ntk.toc_ntk.FileService;
import cz.incad.ntk.toc_ntk.Options;
import cz.incad.ntk.toc_ntk.TagCandidate;
import cz.incad.ntk.toc_ntk.XServer;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.NoOpResponseParser;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.common.util.NamedList;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class SolrTaggerAnalyzer {

  static final Logger LOGGER = Logger.getLogger(SolrTaggerAnalyzer.class.getName());
  public static String COLLECTION = "dictionaries";

  public static JSONObject getTagsJSON(String sysno, String field) {
    JSONObject ret = new JSONObject();
    try (SolrClient solr = new HttpSolrClient.Builder(Options.getInstance().getString("solr.host", "http://localhost:8983/solr/")).build()) {
      SolrQuery query = new SolrQuery();
      query.setRequestHandler("/tag");
      query.set("overlaps", "NO_SUB")
              .set("wt", "json")
              .set("indent", "on")
              .set("field", field)
              .set("matchText", true)
              .set("skipAltTokens", true)
              .set("fl", "*,score")
              .set("tagsLimit", "5000");
      String text = FileService.getRawToc(sysno);

      ContentStreamUpdateRequest req = new ContentStreamUpdateRequest("");
      req.addContentStream(new ContentStreamBase.StringStream(text));
      req.setMethod(SolrRequest.METHOD.POST);
      NoOpResponseParser rawJsonResponseParser = new NoOpResponseParser();
      rawJsonResponseParser.setWriterType("json");
      req.setResponseParser(rawJsonResponseParser);
      req.setPath("/tag");
      req.setParams(query);
      UpdateResponse rsp = req.process(solr, COLLECTION);
      NamedList nlr = rsp.getResponse();

      ret = new JSONObject((String) nlr.get("response"));
      solr.close();
    } catch (IOException | SolrServerException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      ret.put("error", ex);
    }
    return ret;
  }

  public static JSONObject getTags(String sysno) {
    JSONObject ret = new JSONObject();
    try (SolrClient solr = new HttpSolrClient.Builder(Options.getInstance().getString("solr.host", "http://localhost:8983/solr/")).build()) {

      JSONObject marc = XServer.find(sysno);
      // ret.put("marc", marc);
      String lang = XServer.getLanguage(marc);

      List<String> supportedLangs = new ArrayList<>();
      supportedLangs.add("eng");
      supportedLangs.add("cze");
      String fallBack = "eng";
      if (!supportedLangs.contains(lang)) {
        lang = fallBack;
      }
      ret.put("lang", lang);
      String field = "key_tagger_" + lang;
      SolrQuery query = new SolrQuery();
      query.setRequestHandler("/tag");
      query.set("overlaps", "NO_SUB")
              .set("wt", "json")
              .set("indent", "on")
              .set("field", field)
              .set("matchText", true)
              .set("skipAltTokens", true)
              .set("fl", "*,score")
              .set("tagsLimit", "5000");

      Map<String, TagCandidate> candidates = new HashMap<>();

      String title = XServer.getTitle(marc);
      QueryRequest queryRequestTitle = new SolrTaggerRequest(query, title);
      NamedList nlr2 = solr.request(queryRequestTitle, COLLECTION);
      JSONObject jsTitle = processResponse(nlr2, lang, candidates, true);
      jsTitle.put("title", title);
      jsTitle.put("author", XServer.getAuthor(marc));
      ret.put("info", jsTitle);

      String text = FileService.getRawToc(sysno);
      QueryRequest queryRequest = new SolrTaggerRequest(query, text);
      NamedList nlr = solr.request(queryRequest, COLLECTION);
      ret.put("body", processResponse(nlr, lang, candidates, false));

//      
//      TocAnalizer ta = new TocAnalizer();
//      ta.setLang(lang);
//      List<TocLine> lines =  ta.getLines(text);
//      JSONObject tocLines = new JSONObject();
//      for (TocLine line : lines) {
//        tocLines.append("lines", line.toJSON());
//      }
//      ret.put("lines", tocLines);
      List<SimpleEntry<String, Integer>> titleThemes = new ArrayList<>();
      List<SimpleEntry<String, Integer>> tocThemes = new ArrayList<>();

      JSONObject t = ret.getJSONObject("info").getJSONObject("themes");
      for (Object theme : t.keySet()) {
        titleThemes.add(new SimpleEntry<>((String) theme, t.getInt((String) theme)));
      }

      JSONObject tb = ret.getJSONObject("body").getJSONObject("themes");
      for (Object theme : tb.keySet()) {
        tocThemes.add(new SimpleEntry<>((String) theme, tb.getInt((String) theme)));
      }

      List<TagCandidate> list = new ArrayList<>(candidates.values());
      for (TagCandidate c : list) {
        c.setScore(titleThemes, tocThemes);
      }

      list.sort(new Comparator<TagCandidate>() {
        @Override
        public int compare(TagCandidate lhs, TagCandidate rhs) {
          // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
          return lhs.getScore() > rhs.getScore() ? -1 : (lhs.getScore() < rhs.getScore()) ? 1 : 0;
        }
      });
      ret.put("candidates", list);

      solr.close();
    } catch (IOException | SolrServerException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      ret.put("error", ex);
    }

    return ret;
  }

  private static JSONObject processResponse(NamedList nlr, String lang, Map<String, TagCandidate> candidates, boolean isInTitle) {
    JSONObject ret = new JSONObject();
    ret.put("tagsCount", nlr.get("tagsCount"));
    ArrayList tags = (ArrayList) nlr.get("tags");

    Map<String, Integer> ids = new HashMap<>();
    for (Iterator it = tags.iterator(); it.hasNext();) {
      NamedList tag = (NamedList) it.next();
      // String tagIds = ((ArrayList<String>) tag.get("ids"));
      for (String id : ((ArrayList<String>) tag.get("ids"))) {
        if (ids.containsKey(id)) {
          ids.put(id, ids.get(id) + 1);
        } else {
          ids.put(id, 1);
        }
      }
      ret.append("tags", tag.asShallowMap());
    }
    JSONObject broaders = new JSONObject();
    ret.put("broaders", new JSONObject());

    // List of top themes
    JSONObject themes = new JSONObject();
    ret.put("themes", themes);

    SolrDocumentList docs = (SolrDocumentList) nlr.get("response");
    for (Iterator it = docs.iterator(); it.hasNext();) {
      SolrDocument doc = (SolrDocument) it.next();
      String docId = (String) doc.getFirstValue("id");
      String label = (String) doc.getFirstValue("prefLabel_" + lang);
      TagCandidate c = candidates.get(docId);
      if (c == null) {
        c = new TagCandidate();
        c.id = docId;
        c.text = label;
        c.text_eng = (String) doc.getFirstValue("prefLabel_eng");
        c.text_cze = (String) doc.getFirstValue("prefLabel_cze");
        for (Object val : doc.getFieldValues("path_" + lang)) {
          c.path.add((String) val);
        }
      }
      c.count = ids.get(docId) + c.count;
      c.isInTitle = isInTitle || c.isInTitle;
      candidates.put(docId, c);
      String[] path = ((String) doc.getFirstValue("path_" + lang)).split("/");
      broaders = ret.getJSONObject("broaders");
      for (int i = 0; i < path.length - 1; i++) {
        String broader = path[i];
        if (!"".equals(broader)) {
          if (i == 1) {
            // Only top
            if (!themes.has(broader)) {
              themes.put(broader, 1);
            } else {
              themes.put(broader, themes.getInt(broader) + 1);
            }
          }

          if (!broaders.has(broader)) {
            broaders.put(broader, (new JSONObject()).put("count", ids.get(docId)));
          }
          broaders.put("count", broaders.optInt("count", ids.get(docId)) + 1);
          broaders = broaders.getJSONObject(broader);
        }
      }
      broaders.put("count", broaders.optInt("count", 0) + 1);

      ret.append("docs", doc);
    }

    return ret;
  }

  @SuppressWarnings("serial")
  public static class SolrTaggerRequest extends QueryRequest {

    private final String input;

    public SolrTaggerRequest(SolrParams p, String input) {
      super(p, SolrRequest.METHOD.POST);
      this.input = input;
    }

    @Override
    public Collection<ContentStream> getContentStreams() {
      return Collections.singleton((ContentStream) new ContentStreamBase.StringStream(input));
    }
  }
}
