import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {Subject} from 'rxjs/Subject';
import {DecimalPipe} from '@angular/common';

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
  maxScore: number = 0;
  
  showAside: boolean = true;
  showScoreConfig: boolean = false;
  showMatched: boolean = true;
  showFree: boolean = true;
  showBlacklisted: boolean = true;
  showDetails: boolean = false;
  
  threshold: number = .5;
  
  removedFromBlacklist: string[] = [];

constructor(
    private numberPipe: DecimalPipe){
        
    }
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
  
  rescore() {
        this.maxScore = 0;
        this.candidates.forEach((c: Candidate) => {
            c.explain = [];
            if (c.blacklisted) {
                c.score = 0;
                c.explain.push('is in blaclist');
            } else {
                let sc: ScoreConfig = this.scoreConfig;
                c.score = 1;
                if (c.isMatched) {
                    c.score = c.score * sc.matched;
                    c.explain.push('matched in dictionaries ( ' + sc.matched + ' ) = ' + this.numberPipe.transform(c.score, '1.1-3'));
                    if (c.text.split(" ").length > 1) {
                        c.score = c.score * sc.multiple;
                        c.explain.push('is a mulpitle word keyword ( ' + sc.multiple + ' ) = ' + this.numberPipe.transform(c.score, '1.1-3'));
                    }

                    for (let i in c.dictionaries) {
                        let dm = c.dictionaries[i];
                        if (sc.dictionaries.hasOwnProperty(dm['name'])) {
                            c.score = c.score * sc.dictionaries[dm['name']];
                            c.explain.push('matched in ' + dm['name'] + ' dictionary ( ' + sc.dictionaries[dm['name']] + ' ) = ' + this.numberPipe.transform(c.score, '1.1-3'));
                        }
                    }


                }
                if (c.hasProperNoun) {
                    c.score = c.score * sc.hasProperNoun;
                        c.explain.push('keyword has proper noun ( ' + sc.hasProperNoun + ' ) = ' + this.numberPipe.transform(c.score, '1.1-3'));
                }

//                if (c.type == 'DICTIONARY_WORD') {
//                    c.score = c.score * sc.isDictionaryWord;
//                    c.explain.push('is a dictionary word keyword ( ' + sc.isDictionaryWord + ' ) = ' + c.score);
//                }
                if (c.inTitle) {
                    c.score = c.score * sc.inTitle;
                    c.explain.push('keyword found in title ( ' + sc.inTitle + ' ) = ' + this.numberPipe.transform(c.score, '1.1-3'));
                }
                c.score += c.found * sc.found;
                c.explain.push('key word found ' + c.found + ' times ( ' + sc.found + ' ) = ' + this.numberPipe.transform(c.score, '1.1-3'));
                if(!c.inTitle || sc.addExtentForTitle) {
                    c.score += c.extent * sc.extent;
                    c.explain.push('keyword has a extent of ' + c.extent + ' pages ( ' + sc.extent + ' ) = ' + this.numberPipe.transform(c.score, '1.1-3'));
                }
                this.maxScore = Math.max(this.maxScore, c.score);
            }
        });
        this.candidates.sort((a, b) => {return b.score - a.score})

    }
}
