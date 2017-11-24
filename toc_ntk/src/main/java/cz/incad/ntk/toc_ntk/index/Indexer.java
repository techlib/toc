/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk.index;

import cz.incad.ntk.toc_ntk.Options;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class Indexer {

  static final Logger LOGGER = Logger.getLogger(Indexer.class.getName());
  Options opts;
  SolrClient client;

  public Indexer() {
    try {
      opts = Options.getInstance();
      client = getClient("journal");
    } catch (IOException | JSONException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
  }

  private SolrClient getClient(String core) throws IOException {
    return new HttpSolrClient.Builder(String.format("%s%s",
            opts.getString("solr.host", "http://localhost:8983/solr/"),
            core)).build();
  }

  public JSONObject run() {
    JSONObject ret = new JSONObject();
    return ret;
  }

  public JSONObject indexPSH() {

    JSONObject ret = new JSONObject();
    PSHReader psh = new PSHReader();
    try {
      client = getClient("psh");
      psh.readFromXML(Indexer.class.getResourceAsStream("psh-skos.rdf"), client);
    } catch (IOException | XMLStreamException | SolrServerException ex) {
      ret.put("error", ex);
      Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
    }
    return ret;
  }

  public JSONObject indexKeywords() {

    JSONObject ret = new JSONObject();
    SimpleKeywordsReader r = new SimpleKeywordsReader("keywords");
    try {
      client = getClient("keywords");
      r.readFromTxt(Indexer.class.getResourceAsStream("653_klicova_slova_b.txt"), client);
    } catch (IOException ex) {
      ret.put("error", ex);
      Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
    }
    return ret;
    
  }

  public JSONObject indexKonspekt() {

    JSONObject ret = new JSONObject();
    KonspektReader k = new KonspektReader();
    try {
      client = getClient("konspekt");
      k.readFromTxt(Indexer.class.getResourceAsStream("konsp_uni.txt"), client);
    } catch (Exception ex) {
      ret.put("error", ex);
      Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
    }
    return ret;
  }
}
