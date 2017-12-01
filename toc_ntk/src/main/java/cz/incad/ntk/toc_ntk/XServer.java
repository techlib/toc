
package cz.incad.ntk.toc_ntk;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.XML;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
class XServer {

  public static final Logger LOGGER = Logger.getLogger(XServer.class.getName());

  static JSONObject find(String sysno) {
      JSONObject ret= new JSONObject();
    try {
      String url = Options.getInstance().getString("xserver", "http://aleph.techlib.cz/X?op=find-doc&base=stk01");
      InputStream inputStream = RESTHelper.inputStream(url + "&doc_num=" + sysno);
      
      String xml = org.apache.commons.io.IOUtils.toString(inputStream, "UTF8");
      ret = XML.toJSONObject(xml)
              .getJSONObject("find-doc")
              .getJSONObject("record")
              .getJSONObject("metadata")
              .getJSONObject("oai_marc");
    } catch (IOException | JSONException ex) {
      ret.put("error", "error getting info from xserver for " + sysno);
      LOGGER.log(Level.SEVERE, "error getting info from xserver for {0}", sysno);
      LOGGER.log(Level.SEVERE, null, ex);
    }
      return ret;
  }
  
}
