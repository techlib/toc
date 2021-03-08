export class ScoreConfig {
  
  isInTitle = 6.5;

  excludedThemes = ['generalities'];
  titleTheme = 5.5;
  bodyTheme = 1.2;


  found: number = .2;
  matched: number = 2.0;
  multiple: number = 3.0;
  hasProperNoun: number = 5.0;
  isDictionaryWord: number = 2.0;
  extent: number = 1.2;
  deep: 1.0;
  dicts: any[] = ['PSH', "keywords", "konspekt"];
  dictionaries: any = {PSH: 3.0, "keywords": 1.2, "konspekt": 4.0};
  inTitle: number = 2.0;
  addExtentForTitle: boolean = false;
}
