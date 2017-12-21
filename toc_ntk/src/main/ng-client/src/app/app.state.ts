import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {Subject} from 'rxjs/Subject';

import {ScoreConfig} from './models/score-config';
import {Candidate} from 'app/models/candidate';
import {DictionaryMatch} from 'app/models/dictionary-match';

@Injectable()
export class AppState {


  private _stateSubject = new Subject();
  public stateChanged: Observable<any> = this._stateSubject.asObservable();


  public _configSubject = new Subject();
  public configSubject: Observable<any> = this._configSubject.asObservable();
  config: any = {};
  scoreConfig: ScoreConfig = new ScoreConfig();

  sysno: string;
  title: string = "";
  hasToc: boolean = false;
  
  candidates: Candidate[] = [];
  selected: DictionaryMatch[] = [];
  
  showAside: boolean = true;
  showScoreConfig: boolean = false;
  showMatched: boolean = true;
  showFree: boolean = true;
  showBlacklisted: boolean = true;
  showDetails: boolean = false;
  
  threshold: number = .5;
  
  removedFromBlacklist: string[] = [];


  setSysno(f: string, kdo: string) {
    this.sysno = f;
    this._stateSubject.next(this);
  }

  setConfig(cfg) {
    this.config = cfg;
    this.scoreConfig = cfg['score'];
    this.scoreConfig.dicts = [];
    for (let key in this.scoreConfig.dictionaries){
      this.scoreConfig.dicts.push(key);
    };

    this._configSubject.next(cfg);
  }
  
  // aside bar
  showAsideBar() {
    this.showAside = !this.showAside;
  }
}
