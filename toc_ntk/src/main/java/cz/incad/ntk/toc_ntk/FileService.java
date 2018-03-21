/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk;

import static cz.incad.ntk.toc_ntk.TocAnalizer.LOGGER;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author alberto.a.hernandez
 */
public class FileService {

  public static File getTocFile(String sysno) throws IOException {
    String dir = Options.getInstance().getString("saved_tocs_dir");
    return new File(dir + sysno);
  }

  public static boolean existsToc(String sysno) {
    try {
      File tocFile = getTocFile(sysno);
      return tocFile.exists();
    } catch (IOException | JSONException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
    return false;
  }
  

  public static String getToc(String sysno) {
    try {
      File tocFile = getTocFile(sysno);
      if (tocFile.exists()) {
        return FileUtils.readFileToString(tocFile, Charset.forName("UTF-8"));
      }
    } catch (IOException | JSONException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public static void save(String sysno, String data) {
    try {
      File tocFile = getTocFile(sysno);
      JSONObject tocFileData = new JSONObject(data);
      FileUtils.writeStringToFile(tocFile, tocFileData.toString(2), Charset.forName("UTF-8"));
    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
  }
}
