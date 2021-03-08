import { ScoreConfig } from "./score-config";

export interface Configuration {

  context: string;
  lang: string;

  snackDuration: number;

  basefolder: string;
  scoreConfig: ScoreConfig;
  score: {
    found: number,
    matched: number,
    multiple: number,
    hasProperNoun: number,
    isDictionaryWord: number,
    extent: number,
    inTitle: number,
    addExtentForTitle: boolean,
    dictionaries: {
      psh: number,
      keywords: number,
      konspekt: number,
      nerizene: number
    }
  }
}


