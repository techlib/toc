/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map.Entry;

/**
 *
 * @author alberto
 */
public class TagCandidate {
  // id in the index
  public String id;
  
  // Text to show
  public String text;
  
  //Keeps how many times the canddate was found in the ToC 
  public int count;
  
  // Wether term found in title
  public boolean isInTitle;
  
  // Path of broaders
  public String path;
  
  // Computed score, for sorting results
  double score;
  
  public void setScore(List<AbstractMap.SimpleEntry<String, Integer>> titleThemes, List<AbstractMap.SimpleEntry<String, Integer>> body) {
    score = count * (isInTitle ? 5.5 : 1);
    for (Entry<String, Integer> e : titleThemes) {
      if (path.contains(e.getKey()) && !"generalities".equals(e.getKey())) {
        score = score * 5.5 * e.getValue();
      }
    }
    for (Entry<String, Integer> e : body) {
      if (path.contains(e.getKey()) && !"generalities".equals(e.getKey())) {
        score = score * e.getValue();
      }
    }
  }
  
  public String getText() {
    return text;
  }
  
  public String getPath() {
    return path;
  }
  
  public boolean isInTitle() {
    return isInTitle;
  }
  
  public int getCount() {
    return count;
  }
  
  public double  getScore() {
    return score;
  }
}
