import { Injectable } from '@angular/core';
import { Http, Response, URLSearchParams, Headers, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';

import {AppState} from './app.state';
import { ScoreConfig } from './models/score-config';
import { Candidate } from './models/candidate';

@Injectable()
export class AppService {

  //basefolder: string = '/home/kudela/.ntk/balicky/';
  constructor(
    private http: Http,
    private state: AppState) { }


  processFolder(foldername: string, config: ScoreConfig): Observable<any> {

    var url = 'mdt';
    //url = '/assets/F21395f_000170834.json';
    let params: URLSearchParams = new URLSearchParams();
    params.set('action', 'ANALYZE_FOLDER');
    params.set('foldername', this.state.config['basefolder']+ foldername);
    params.set('scoreconfig', JSON.stringify(config));
    return this.http.get(url, { search: params })
      .map((response: Response) => {
        return response.json();
      });
  }
  
  getExport(cs: Candidate[]): Observable<any> {

    var url = 'candidates';
    let params: URLSearchParams = new URLSearchParams();
    params.set('action', 'EXPORT');
    params.set('canditates', JSON.stringify(cs));
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
  
  getTocText(foldername: string): Observable<any> {

    var url = 'mdt';
    //url = '/assets/toc.txt';
    let params: URLSearchParams = new URLSearchParams();
    params.set('action', 'VIEW_TOC');
    params.set('foldername', this.state.config['basefolder']+ foldername);
    return this.http.get(url, { search: params })
      .map((response: Response) => {
        return response.text();
      });
  }
}
