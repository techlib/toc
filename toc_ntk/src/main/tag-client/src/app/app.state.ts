import { Observable, Subject, BehaviorSubject, ReplaySubject } from 'rxjs';
import { Params, ParamMap } from '@angular/router';
import { NavigationExtras } from '@angular/router';
import { Configuration } from './shared/configuration';
import { Candidate } from './shared/candidate';
import { ScoreConfig } from './shared/score-config';

export class AppState {

  config: Configuration;

  private _paramsProcessed: ReplaySubject<string> = new ReplaySubject(3);
  public paramsProcessed: Observable<any> = this._paramsProcessed.asObservable();

  private _stateSubject = new Subject();
  public stateChanged: Observable<any> = this._stateSubject.asObservable();


  public _configSubject = new Subject();
  public configSubject: Observable<any> = this._configSubject.asObservable();

  scoreConfig: ScoreConfig = new ScoreConfig();

  balicky: any[] = [];
  sysno: string;
  title: string = "";
  hasToc: boolean = false;

  currentToc: { 'candidates': Candidate[] };
  candidates: Candidate[] = [];
  maxScore: number = 0;

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
    // this.scoreConfig = cfg['score'];
    // this.scoreConfig.dicts = [];
    // for (let key in this.scoreConfig.dictionaries) {
    //   this.scoreConfig.dicts.push(key);
    // };

  }

  // aside bar
  showAsideBar() {
    this.showAside = !this.showAside;
  }

  rescore() {

  }

  processUrlParams(searchParams: ParamMap) {
    searchParams.keys.forEach(p => {
      const param = searchParams.get(p);
      if (p === 'view') {

      } else {
        //this.addFilter(p, param, null, false);
      }
    });
    this._paramsProcessed.next();
  }
}
