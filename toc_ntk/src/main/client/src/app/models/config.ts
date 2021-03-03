
export interface Sort { label: string; field: string; dir: string; };

export class Configuration {
  basefolder: string;
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
