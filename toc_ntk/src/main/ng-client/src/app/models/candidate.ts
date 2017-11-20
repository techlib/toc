import {DictionaryMatch} from './dictionary-match';

export class Candidate {
  text: string;
  blacklisted: boolean;
  isMatched: boolean;
  matched_text: string;
  dictionaries: DictionaryMatch[] = [];
  type: string;
  hasProperNoun: boolean;
  score: number;
  found: number;
  
}

