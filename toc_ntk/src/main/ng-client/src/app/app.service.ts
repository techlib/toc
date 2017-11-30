import { Injectable } from '@angular/core';
import { Http, Response, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Rx';

import {AppState} from './app.state';
import { ScoreConfig } from './models/score-config';
import {DictionaryMatch} from './models/dictionary-match';

@Injectable()
export class AppService {

  //basefolder: string = '/home/kudela/.ntk/balicky/';
  constructor(
    private http: Http,
    private state: AppState) { }


  processFolder(foldername: string, config: ScoreConfig): Observable<any> {

    var url = 'candidates';
    //url = '/assets/F21395f_000170834.json';
    let params: URLSearchParams = new URLSearchParams();
    params.set('action', 'FIND');
    params.set('foldername', this.state.config['basefolder']+ foldername);
    params.set('scoreconfig', JSON.stringify(config));
    return this.http.get(url, { search: params })
      .map((response: Response) => {
        return response.json();
      });
  }
  
  processSysno(sysno: string, config: ScoreConfig): Observable<any> {

    var url = 'candidates';
    //url = '/assets/F21395f_000170834.json';
    let params: URLSearchParams = new URLSearchParams();
    params.set('action', 'FIND');
    params.set('sysno', sysno);
    params.set('scoreconfig', JSON.stringify(config));
    return this.http.get(url, { search: params })
      .map((response: Response) => {
        return response.json();
      });
  }
  
  getExport(cs: DictionaryMatch[]): Observable<any> {

    var url = 'candidates';
    let params: URLSearchParams = new URLSearchParams();
    params.set('action', 'EXPORT');
    params.set('canditates', JSON.stringify(cs));
    return this.http.get(url, { search: params })
      .map((response: Response) => {
        return response.json();
      });
  }
  
  getBalicky(): Observable<any> {

    var url = 'candidates';
    let params: URLSearchParams = new URLSearchParams();
    params.set('action', 'BALICKY');
    return this.http.get(url, { search: params })
      .map((response: Response) => {
        return response.json();
      });
  }
  
  getBlacklist(): Observable<any> {

    var url = 'search/blacklist/select';
    let params: URLSearchParams = new URLSearchParams();
    params.set('q', '*');
    params.set('rows', '50');
    params.set('sort', 'key asc');
    return this.http.get(url, { search: params })
      .map((response: Response) => {
        return response.json();
      });
  }
  
  addToBlackList(key: string): Observable<any> {

    var url = 'candidates';
    let params: URLSearchParams = new URLSearchParams();
    params.set('action', 'ADD_TO_BLACKLIST');
    params.set('key', key);
    console.log(key);
    return this.http.get(url, { search: params })
      .map((response: Response) => {
        return response.json();
      });
  }
  
  removeFromBlackList(key: string): Observable<any> {

    var url = 'candidates';
    let params: URLSearchParams = new URLSearchParams();
    params.set('action', 'REMOVE_FROM_BLACKLIST');
    params.set('key', key);
    return this.http.get(url, { search: params })
      .map((response: Response) => {
        return response.json();
      });
  }
  
  getTocText(sysno: string): Observable<any> {

    var url = 'mdt';
    //url = '/assets/toc.txt';
    let params: URLSearchParams = new URLSearchParams();
    params.set('action', 'VIEW_TOC');
    params.set('sysno', sysno);
    return this.http.get(url, { search: params })
      .map((response: Response) => {
        return response.text();
      });
  }
}
