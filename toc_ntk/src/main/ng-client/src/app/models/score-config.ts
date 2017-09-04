export class ScoreConfig {
  
  found: number = .2;
  matched: number = 2.0;
  multiple: number = 3.0;
  hasProperNoun: number = 5.0;
  isDictionaryWord: number = 2.0;
  dicts: any[] = ['PSH', "653_klicova_slova_b.txt"];
  dictionaries: any = {PSH: 3.0, "653_klicova_slova_b.txt": 1.2};
}
