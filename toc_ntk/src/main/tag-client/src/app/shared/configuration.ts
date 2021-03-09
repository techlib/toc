import { ScoreConfig } from "./score-config";

export interface Configuration {

  context: string;
  lang: string;

  snackDuration: number;

  basefolder: string;
  scoreConfig: ScoreConfig;
}


