import { ScoreConfig } from "./score-config";

export class TagCandidate {

  id: string;
  
  // Text to show
  text: string;
  text_cze: string;
  text_eng: string;

  matchedText: string[];
  
  //Keeps how many times the canddate was found in the ToC 
  count: number;
  
  // Wether term found in title
  isInTitle: boolean;
  
  // Path of broaders
  path: string[];

  explain: string[];
  
  // Computed score, for sorting results
  score: number;
  
  selected: boolean;


}

