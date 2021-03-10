/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk.index;

import cz.incad.ntk.toc_ntk.FileService;
import cz.incad.ntk.toc_ntk.Options;
import cz.incad.ntk.toc_ntk.XServer;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;

/**
 *
 * @author alberto
 */
public class TOCIndexer {

  public static final Logger LOGGER = Logger.getLogger(PSHIndexer.class.getName());
  final String collection = "tocs";
  Options opts = Options.getInstance();

  public JSONObject full() {
    JSONObject ret = new JSONObject();

    try (SolrClient solr = new HttpSolrClient.Builder(opts.getString("solr.host", "http://localhost:8983/solr/")).build()) {

      // ret = getFolders(solr);
      ret = getTocs(solr);
      solr.commit(collection);
      solr.close();
    } catch (SolrServerException | IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
    return ret;
  }
  
  

  public JSONObject getTocs(SolrClient solr) {

    String tocs_dir = opts.getString("saved_tocs_dir");
    JSONObject dirs = new JSONObject();
    try {

      LOGGER.log(Level.FINE, tocs_dir);
      File dir = new File(tocs_dir);
      
      for (String d : dir.list()) {
        //sysno je nazev soubor

        File f = new File(tocs_dir + File.separator + d);
        JSONObject js = new JSONObject(FileUtils.readFileToString(f, Charset.forName("UTF-8")));
        JSONArray cs = js.optJSONArray("candidates");
        JSONObject info = js.getJSONObject("info");
        
          String title = XServer.getTitle(info);

        SolrInputDocument idoc = new SolrInputDocument();
        idoc.addField("title", title);
        idoc.addField("filename", js.getString("foldername"));
        idoc.addField("last_modified", new Date(f.lastModified()));
        idoc.addField("saved", true);

        idoc.addField("sysno", d);
        String toc = FileService.getRawTocFolder(js.getString("foldername"));
        idoc.addField("toc", toc);
        solr.add(collection, idoc);
      }
    } catch (SolrServerException | IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }

    return dirs;
  }

  public JSONObject getFolders(SolrClient solr) {

    String tocs_dir = opts.getString("saved_tocs_dir");
    JSONObject dirs = new JSONObject();
    try {

      String foldername = opts.getString("balicky_dir");
      LOGGER.log(Level.FINE, foldername);
      File dir = new File(foldername);
      String[] directories = dir.list(new FilenameFilter() {
        @Override
        public boolean accept(File current, String name) {
          return name.startsWith("DONE_") && new File(current, name).isDirectory();
        }
      });
      for (String d : directories) {
        //sysno je nazev jedineho existujici .jpg soubor
        String sysno = null;

        File dir2 = new File(foldername + File.separator + d);
        String[] jpgs = dir2.list(new FilenameFilter() {
          @Override
          public boolean accept(File current, String name) {
            return name.endsWith("jpg") || name.endsWith("jpeg");
          }
        });

        if (jpgs.length > 0) {
          sysno = jpgs[0].substring(0, jpgs[0].indexOf("."));
        }

        SolrInputDocument idoc = new SolrInputDocument();
        idoc.addField("filename", d);
        idoc.addField("last_modified", new Date(dir2.lastModified()));
        boolean saved = new File(tocs_dir + sysno).exists();
        idoc.addField("saved", saved);

        if (sysno != null && sysno.length() == 9) {
          dirs.put(sysno, new JSONObject().put("name", d).put("saved", saved));
          JSONObject info = XServer.find(sysno);
          String title = XServer.getTitle(info);
          idoc.addField("title", title);
        } else {
          LOGGER.log(Level.INFO, "Invalid folder. Folder {0} has sysno {1}", new String[]{d, sysno});
          sysno = d;
        }
        idoc.addField("sysno", sysno);
        String toc = FileService.getRawTocFolder(dir2.getAbsolutePath());
        
        idoc.addField("toc", toc);
        solr.add(collection, idoc);
      }
    } catch (SolrServerException | IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }

    return dirs;
  }
}
