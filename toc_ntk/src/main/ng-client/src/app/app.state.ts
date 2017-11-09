import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {Subject} from 'rxjs/Subject';

import {ScoreConfig} from './models/score-config';

@Injectable()
export class AppState {


  private _stateSubject = new Subject();
  public stateChanged: Observable<any> = this._stateSubject.asObservable();


  public _configSubject = new Subject();
  public configSubject: Observable<any> = this._configSubject.asObservable();
  config: any = {};
  scoreConfig: ScoreConfig = new ScoreConfig();

  foldername: string;


  setFoldername(f: string) {
    this.foldername = f;
    this._stateSubject.next(this);
  }

  setConfig(cfg) {
    this.config = cfg;
    this.scoreConfig = cfg['score'];
    this.scoreConfig.dicts = [];
    this.scoreConfig.dictionaries.forEach((d, a) => {
      console.log(d, a);
      this.scoreConfig.dicts.push(d);
    });

    this._configSubject.next(cfg);
  }
}
