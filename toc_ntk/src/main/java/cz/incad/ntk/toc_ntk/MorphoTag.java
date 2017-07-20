/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alberto
 */
public class MorphoTag {

  public static final Logger LOGGER = Logger.getLogger(MorphoTag.class.getName());

  String tag;

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

  boolean isValid;

  /*
  http://ufal.mff.cuni.cz/pdt2.0/doc/manuals/en/m-layer/html/ch02s02s01.html
  Position	Name	Description
1	POS	Part of speech
2	SubPOS	Detailed part of speech
3	Gender	Gender
4	Number	Number
5	Case	Case
6	PossGender	Possessor's gender
7	PossNumber	Possessor's number
8	Person	Person
9	Tense	Tense
10	Grade	Degree of comparison
11	Negation	Negation
12	Voice	Voice
13	Reserve1	Reserve
14	Reserve2	Reserve
15	Var	Variant, style
  
   */
  public MorphoTag(String str) {
    if (str.length() != 15) {
      LOGGER.log(Level.WARNING, "invalid tag string");
      this.isValid = false;
    } else {
      this.isValid = true;
      this.tag = str;
    }
  }

  /**
   * get the Part of speech
   *
   * @return A	Adjective C	Numeral D	Adverb I	Interjection J	Conjunction N	Noun
   * P	Pronoun V	Verb R	Preposition T	Particle X	Unknown, Not Determined,
   * Unclassifiable Z	Punctuation (also used for the Sentence Boundary token)
   */
  public String getPos() {
    return this.tag.substring(0, 1);
  }
  
  public boolean isNoun(){
    return "N".equals(this.getPos());
  }
  
  public boolean isNeutral(){
    return "N".equals(this.tag.substring(2, 3));
  }
  
  public boolean isAdjective(){
    return "A".equals(this.getPos());
  }
  
  public boolean isGenitive(){
    return "2".equals(this.getCase());
  }
  
  

  public String getPosHuman() {
    return posHuman.valueOf(this.getPos()).humanName();
  }

  /**
   * get the case
   *
   * @return 1	Nominative, e.g. žena 2	Genitive, e.g. ženy 3	Dative, e.g. ženě 4
   * Accusative, e.g. ženu 5	Vocative, e.g. ženo 6	Locative, e.g. ženě 7
   * Instrumental, e.g. ženou X	Any
   */
  public String getCase() {
    return this.tag.substring(4, 5);
  }

  public String getCaseHuman() {
    return caseHuman.humanName(this.getCase());
  }
}
