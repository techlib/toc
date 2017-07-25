import { Injectable } from '@angular/core';
import { Http, Response, URLSearchParams, Headers, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';

import { ScoreConfig } from './models/score-config';

@Injectable()
export class AppService {

  constructor(private http: Http) { }


  processFolder(foldername: string, config: ScoreConfig): Observable<any> {

    var url = 'mdt';
    let params: URLSearchParams = new URLSearchParams();
    params.set('action', 'ANALYZE_FOLDER');
    params.set('foldername', '/home/alberto/Projects/NTK/balicky/'+ foldername);
    params.set('scoreconfig', JSON.stringify(config));
    return this.http.get(url, { search: params })
      .map((response: Response) => {
        return response.json();
      });
  }
  
  getTocText(foldername: string): Observable<any> {

    var url = 'mdt';
    let params: URLSearchParams = new URLSearchParams();
    params.set('action', 'VIEW_TOC');
    params.set('foldername', '/home/alberto/Projects/NTK/balicky/'+ foldername);
    return this.http.get(url, { search: params })
      .map((response: Response) => {
        return response.text();
      });
  }
}