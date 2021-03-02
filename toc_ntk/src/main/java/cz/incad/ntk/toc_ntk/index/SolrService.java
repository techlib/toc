/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk.index;

import cz.incad.ntk.toc_ntk.Candidate;
import cz.incad.ntk.toc_ntk.DictionaryMatch;
import cz.incad.ntk.toc_ntk.FileService;
import cz.incad.ntk.toc_ntk.Options;
import cz.incad.ntk.toc_ntk.XServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.InputStreamResponseParser;
import org.apache.solr.client.solrj.impl.NoOpResponseParser;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.noggit.JSONUtil;

/**
 *
 * @author alberto
 */
public class SolrService {

  static final Logger LOGGER = Logger.getLogger(Candidate.class.getName());
  static final String urlString = "http://localhost:8983/solr";

  public static void addToBlackList(String key) throws SolrServerException, IOException {

    SolrClient solr = new HttpSolrClient.Builder(urlString).build();
    SolrInputDocument idoc = new SolrInputDocument();
    idoc.addField("key", key);
    LOGGER.info(idoc.toString());
    solr.add("blacklist", idoc, 100);
    solr.close();
  }

  public static void removeFromBlackList(String key) throws SolrServerException, IOException {

    SolrClient solr = new HttpSolrClient.Builder(urlString).build();
    solr.deleteById("blacklist", key, 10);

    LOGGER.info("deleted");
    solr.close();
  }

  public static void addToNerizene(String[] keys) throws SolrServerException, IOException {

    SolrClient solr = new HttpSolrClient.Builder(urlString).build();
    for (String key : keys) {
      SolrInputDocument idoc = new SolrInputDocument();
      idoc.addField("key", key);
      solr.add("nerizene", idoc, 100);
    }
    solr.close();
  }

  public static void saveToc(String sysno, String toc) throws SolrServerException, IOException {
    SolrClient solr = new HttpSolrClient.Builder(urlString).build();
    SolrInputDocument idoc = new SolrInputDocument();
    idoc.addField("sysno", sysno);
    idoc.addField("toc", toc);
    solr.add("tocs", idoc, 100);
    solr.close();
  }

  public static String getToc(String sysno) throws SolrServerException, IOException {
    SolrClient solr = new HttpSolrClient.Builder(urlString).build();
    SolrQuery query = new SolrQuery();
    query.setQuery("sysno:\"" + sysno + "\"");
    QueryResponse response = solr.query("tocs", query);
    solr.close();
    if (response.getResults().getNumFound() > 0) {
      String toc = (String) response.getResults().get(0).getFirstValue("toc");

      LOGGER.info(toc);
      return toc;
    }
    return null;
  }

  public static boolean isInBlackList(String text) {
    try {
      SolrClient solr = new HttpSolrClient.Builder(urlString).build();
      SolrQuery query = new SolrQuery();

      //Search in blacklist
      query.setQuery("\"" + text.toLowerCase() + "\"");
      query.set("defType", "edismax");
      query.set("qf", "key");
      query.set("mm", "100%");
      QueryResponse response = solr.query("blacklist", query);
      solr.close();
      if (response.getResults().getNumFound() > 0) {
        return true;
      }

    } catch (SolrServerException | IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);

    }

    return false;
  }

  public static JSONArray findInDictionariesAsJSON(String text) {
    JSONArray ret = new JSONArray();
    SolrDocumentList docs = SolrService.findInDictionaries(text);
    if (!docs.isEmpty()) {
      for (SolrDocument doc : docs) {
        ret.put(
                new DictionaryMatch(doc.getFirstValue("slovnik").toString(),
                        doc.getFirstValue("key_cz").toString(),
                        new JSONObject(JSONUtil.toJSON(doc))).toJSON());
      }

    }
    return ret;
  }

  public static SolrDocumentList findInDictionaries(String text) {
    SolrDocumentList docs = new SolrDocumentList();
    try {
      SolrClient solr = new HttpSolrClient.Builder(urlString).build();
      JSONArray dicts = Options.getInstance().getJSONArray("dictionaries");
      for (int i = 0; i < dicts.length(); i++) {
        docs.addAll(findInDictionary(solr, dicts.getJSONObject(i), text));
      }

      /*
            SolrQuery query = new SolrQuery();

            //Search in keywords
            query.setQuery("\"" + text.toLowerCase() + "\"");
            query.set("defType", "edismax");
            if (text.indexOf(" ") > 0) {
                query.set("qf", "key_cz");
                query.set("mm", "100%");
            } else {
                query.set("qf", "key_lower");
            }
            QueryResponse response = solr.query("keywords", query);
            if (response.getResults().getNumFound() > 0) {
                docs.add(response.getResults().get(0));
            }

            //Search in psh
            query = new SolrQuery();
            query.setQuery("\"" + text.toLowerCase() + "\"");
            //query.setFields("csPrefLabel");
            query.set("defType", "edismax");
            if (text.indexOf(" ") > 0) {
                query.set("qf", "key_cz");
            } else {
                query.set("qf", "key_cz");
            }
            query.set("mm", "80%");
            QueryResponse pshResp = solr.query("psh", query);

            if (pshResp.getResults().getNumFound() > 0) {
                docs.add(pshResp.getResults().get(0));
            }

            //Search in konspekt
            query = new SolrQuery();
            query.setQuery("\"" + text.toLowerCase() + "\"");
            query.set("defType", "edismax");
            if (text.indexOf(" ") > 0) {
                query.set("qf", "key_cz");
            } else {
                query.set("qf", "key_lower");
            }
            query.set("mm", "100%");
            response = solr.query("konspekt", query);

            if (response.getResults().getNumFound() > 0) {
                docs.add(response.getResults().get(0));
            }

            //Search in nerizene
            query = new SolrQuery();
            query.setQuery("\"" + text.toLowerCase() + "\"");
            query.set("defType", "edismax");
            if (text.indexOf(" ") > 0) {
                query.set("qf", "key_cz");
            } else {
                query.set("qf", "key_lower");
            }
            query.set("mm", "100%");
            response = solr.query("nerizene", query);

            if (response.getResults().getNumFound() > 0) {
                docs.add(response.getResults().get(0));
            }
       */
      solr.close();
    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
    return docs;
  }

  public static SolrDocumentList findInDictionary(SolrClient solr, JSONObject jo, String text) {
    SolrDocumentList docs = new SolrDocumentList();
    try {
      String name = jo.getString("name");
      SolrQuery query = new SolrQuery();

      query.setQuery("\"" + text.toLowerCase() + "\"");
      query.set("defType", "edismax");
      if (text.indexOf(" ") > 0) {
        query.set("qf", "key_cz");
      } else {
        query.set("qf", "key_lower");
      }
      query.set("mm", jo.getString("mm"));
      QueryResponse response = solr.query(name, query);
      if (response.getResults().getNumFound() > 0) {
        SolrDocument doc = response.getResults().get(0);
        doc.put("slovnik", name);
        docs.add(doc);
      }

    } catch (SolrServerException | IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
    return docs;
  }
  
  
  public static JSONObject getTagsJSON(String sysno, String field, String dict) {
    JSONObject ret = new JSONObject();
    try (SolrClient solr = new HttpSolrClient.Builder(urlString).build()) {
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
    UpdateResponse rsp = req.process(solr, dict);
    NamedList nlr = rsp.getResponse();
            
    ret = new JSONObject((String) nlr.get("response"));
    solr.close();
    } catch (IOException | SolrServerException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      ret.put("error", ex);
    }
    return ret;
   }

  public static JSONObject getTags(String sysno, String dict) {
    JSONObject ret = new JSONObject();
    try (SolrClient solr = new HttpSolrClient.Builder(urlString).build()) {
      JSONObject marc = XServer.find(sysno);
      ret.put("marc", marc);
      String lang = XServer.getLanguage(marc);
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
      String text = FileService.getRawToc(sysno);
      QueryRequest queryRequest = new SolrTaggerRequest(query, text);

//      NoOpResponseParser rawJsonResponseParser = new NoOpResponseParser();
//      rawJsonResponseParser.setWriterType("json");
//      queryRequest.setResponseParser(rawJsonResponseParser);
      
      NamedList nlr = solr.request(queryRequest, dict);
      
      // ret = new JSONObject((String) nlr.get("response"));


      ret.put("tagsCount", nlr.get("tagsCount")); 
      ArrayList tags = (ArrayList) nlr.get("tags");
      Map<String, Integer> ids = new HashMap<>();
      for (Iterator it = tags.iterator(); it.hasNext();) {
        NamedList tag = (NamedList) it.next();
        String id = ((ArrayList<String>)tag.get("ids")).get(0);
        if (ids.containsKey(id)) {
          ids.put(id, ids.get(id) + 1);
        } else {
          ids.put(id, 1);
        }
        ret.append("tags", tag.asShallowMap());
      }
      
      JSONObject broaders = new JSONObject();
      ret.put("broaders", new JSONObject());
      JSONObject themes = new JSONObject();
      ret.put("themes", themes);
      

      SolrDocumentList docs = (SolrDocumentList) nlr.get("response");
      for (Iterator it = docs.iterator(); it.hasNext();) {
        SolrDocument doc = (SolrDocument) it.next();
        String[] path = ((String) doc.getFirstValue("path_" + lang)).split("/");
        broaders = ret.getJSONObject("broaders");
        for (String broader : path) {
          if (!themes.has(broader)) {
            themes.put(broader, 1);
          } else {
            themes.put(broader, themes.getInt(broader) + 1);
          }
          
          if (!broaders.has(broader)) {
            broaders.put(broader, (new JSONObject()).put("count", 0));
          }
          broaders.put("count", broaders.optInt("count", 0)+1);
          broaders = broaders.getJSONObject(broader);
        }
        broaders.put("count", broaders.optInt("count", 0)+1);
        
//        String broader = (String) doc.getFirstValue("broader");
//        if (broader == null) {
//          // Broader itself
//          broader = (String) doc.getFirstValue(outputLang + "PrefLabel");
//        } else {
//          SolrDocument sdoc = getById(solr, dict, broader);
//          if (sdoc.containsKey("broader")) {
//            
//          } else {
//            
//          }
//          broader = (String) sdoc.getFirstValue(outputLang + "PrefLabel");
//        }
//        if (!broaders.has(broader)) {
//          broaders.put(broader, new JSONObject());
//        }
//        String label = (String)doc.getFirstValue(outputLang + "PrefLabel");
//        //if (broaders.getJSONObject(broader).has(label)) {
//        //  broaders.getJSONObject(broader).put(label, broaders.getJSONObject(broader).getInt(label) + 1);
//        //} else {
//          broaders.getJSONObject(broader).put(label, ids.get((String)doc.getFirstValue("id")));
//        //}
        
        ret.append("docs", doc);
      }

      solr.close();
    } catch (IOException | SolrServerException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      ret.put("error", ex);
    }
    return ret;
  }
  
  public static SolrDocument getById(SolrClient solr, String collection, String val, String... fl) {
    try {
      SolrQuery q = new SolrQuery("id:\"" + val + "\"");
      return solr.query(collection, q).getResults().get(0);
    } catch (SolrServerException | IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return null;
    }
  }

  @SuppressWarnings("serial")
  public static class SolrTaggerRequest extends QueryRequest {

    private final String input;

    public SolrTaggerRequest(SolrParams p, String input) {
      super(p, METHOD.POST);
      this.input = input;
    }

    @Override
    public Collection<ContentStream> getContentStreams() {
      return Collections.singleton((ContentStream) new ContentStreamBase.StringStream(input));
    }
  }

}
