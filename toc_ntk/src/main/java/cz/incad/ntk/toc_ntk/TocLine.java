/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alberto
 */
public class TocLine {

  public static final Logger LOGGER = Logger.getLogger(TocLine.class.getName());

  String raw;
  String deep;
  String text;
  int start_page;

  public TocLine(String rawStr) {
    this.raw = rawStr;
    String str = rawStr.trim();
    if(str.codePointAt(0) == 65279){
      str = str.substring(1);
    }
    int page_pos = str.length() - 1;
    while (page_pos > -1 && Character.isDigit(str.charAt(page_pos))) {
      page_pos--;
    }

    try {
      this.start_page = Integer.parseInt(str.substring(page_pos + 1));
    } catch (NumberFormatException ex) {
      LOGGER.log(Level.INFO, "no page start for {0}", str);
    }
    
    if (Character.isDigit(str.charAt(0))) {
      int pos = str.indexOf("\t");
      if (pos > -1) {
        pos = Math.min(pos, Math.max(str.indexOf(" "), 0));
      } else {
        pos = str.indexOf(" ");
      }
      if (pos > -1) {
        this.deep = str.substring(0, pos);
        this.text = str.substring(pos, page_pos+1).trim();
      } else {
        this.text = str.substring(0, page_pos + 1).trim();
      }
    } else {
      this.text = str.substring(0, page_pos).trim();
    }
  }
}
