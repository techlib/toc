/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk.morpho;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alberto
 */
public abstract class MorphoTag {
  String tag = "";

  public enum posHuman {

    A("Adjective"),
    C("Numeral"),
    D("Adverb"),
    I("Interjection"),
    J("Conjunction"),
    N("Noun"),
    P("Pronoun"),
    V("Verb"),
    R("Preposition"),
    T("Particle"),
    X("Unknown, Not Determined, Unclassifiable"),
    Z("Punctuation");

    private final String name;

    public String humanName() {
      return name;
    }

    posHuman(String humanName) {
      this.name = humanName;
    }
  };

  public enum caseHuman {
    Nominative("1"),
    Genitive("2"),
    Dative("3"),
    Accusative("4"),
    Vocative("5"),
    Locative("6"),
    Instrumental("7"),
    Any("X");

//  1(""),
//2("Genitive"),
//3("Dative"),
//4("Accusative"),
//5("Vocative"),
//6("Locative"),
//7("Instrumental"),
//  X("Any");
    
    private final String name;
    private static Map<String, caseHuman> map = new HashMap<>();
    

    static {
        for (caseHuman c : caseHuman.values()) {
            map.put(c.name, c);
        }
    }

    public static String humanName(String c) {
      if(map.containsKey(c)){
        return map.get(c).toString();
      } else {
        return "hhhmmmm: " + c;
      }
    }

    caseHuman(String humanName) {
      this.name = humanName;
    }
    
  };

  public MorphoTag(String str){
    this.tag = str;
  };

  /**
   * get the Part of speech
   *
   * @return A	Adjective C	Numeral D	Adverb I	Interjection J	Conjunction N	Noun
   * P	Pronoun V	Verb R	Preposition T	Particle X	Unknown, Not Determined,
   * Unclassifiable Z	Punctuation (also used for the Sentence Boundary token)
   */
  public abstract String getPos();
  
  public abstract boolean isNoun();
  
  public abstract boolean isNeutral();
  
  public abstract boolean isAdjective();
  
  public abstract boolean isGenitive();
  
  public String getPosHuman() {
    return posHuman.valueOf(this.getPos()).humanName();
  }

}
