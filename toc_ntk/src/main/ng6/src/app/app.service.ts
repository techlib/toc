import {Injectable} from '@angular/core';
//import {Http, Response, URLSearchParams} from '@angular/http';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import {Observable} from 'rxjs';

import {AppState} from './app.state';
import {ScoreConfig} from './models/score-config';
import {DictionaryMatch} from './models/dictionary-match';

@Injectable()
export class AppService {

    //basefolder: string = '/home/kudela/.ntk/balicky/';
    constructor(
        private http: HttpClient,
        private state: AppState) {}


    processFolder(foldername: string, config: ScoreConfig): Observable<any> {

        var url = 'candidates';
        //url = '/assets/F21395f_000170834.json';
        let params: HttpParams = new HttpParams()
        .set('action', 'FIND')
        .set('foldername', this.state.config['basefolder'] + foldername)
        .set('scoreconfig', JSON.stringify(config));
        return this.http.get(url, {params: params});
    }

    processSysno(sysno: string, config: ScoreConfig): Observable<any> {

        var url = 'candidates';
        //url = '/assets/F21395f_000170834.json'; // comment
        let params: HttpParams = new HttpParams()
        .set('action', 'FIND')
        .set('sysno', sysno)
        .set('scoreconfig', JSON.stringify(config));
        return this.http.get(url, {params: params});
    }

    getExport(cs: DictionaryMatch[]): Observable<any> {

        var url = 'candidates';
        let params: HttpParams = new HttpParams().set('action', 'EXPORT').set('candidates', JSON.stringify(cs));
        return this.http.get(url, {params: params});
    }
    
    saveNewKeys(cs: DictionaryMatch[]): Observable<any> {

        var url = 'candidates';
        let params: HttpParams = new HttpParams().set('action', 'ADD_TO_NERIZENE');
        cs.forEach(dm => {
          if (dm.name === 'novy'){
            params = params.append('key', dm.text);
          }
        })
        //params.set('candidates', JSON.stringify(cs));
        return this.http.get(url, {params: params});
    }
    
    saveToc(): Observable<any> {
      
      

    let headers = new HttpHeaders({ 'Content-Type': 'text/plain;charset=UTF-8' });
    const options = { headers: headers };
    
    

      var url = 'candidates?action=SAVE_TOC&sysno=' + this.state.sysno;
      //let params: HttpParams = new HttpParams();

      //params.set('sysno', this.state.sysno);
      //params.set('toc', JSON.stringify(this.state.currentToc));
        
      return this.http.post<string>(url, JSON.stringify(this.state.currentToc), options);
    }
    
    updateFolders(): Observable<any> {

        var url = 'candidates';
        //url = '/assets/balicky.json'; // comment
        let params: HttpParams = new HttpParams().set('action', 'BALICKY');
        return this.http.get(url, {params: params});
    }

    getBalicky(update: boolean = false): Observable<any> {

        var url = 'candidates';
        //url = '/assets/balicky.json'; // comment
        let params: HttpParams = new HttpParams().set('action', 'BALICKY');
        if(update){
            params.set('update', 'true');
        }
        return this.http.get(url, {params: params});
    }

    getBlacklist(): Observable<any> {

        var url = 'search/blacklist/select';
        //url = '/assets/blacklist.json'; // comment
        let params: HttpParams = new HttpParams().set('q', '*').set('rows', '100').set('sort', 'key asc');
        return this.http.get(url, {params: params});
    }

    addToBlackList(key: string): Observable<any> {

        var url = 'candidates';
        let params: HttpParams = new HttpParams().set('action', 'ADD_TO_BLACKLIST').set('key', key);
        return this.http.get(url, {params: params});
    }

    removeFromBlackList(key: string): Observable<any> {

        var url = 'candidates';
        let params: HttpParams = new HttpParams()
        .set('action', 'REMOVE_FROM_BLACKLIST')
        .set('key', key);
        return this.http.get(url, {params: params});
    }

    getTocText(sysno: string): Observable<any> {

        var url = 'mdt';
        //url = '/assets/toc.txt';
        let params: HttpParams = new HttpParams()
        .set('action', 'VIEW_TOC')
        .set('sysno', sysno);
        return this.http.get(url, {responseType: 'text', params: params});
    }

    copyTextToClipboard(text) {
        let textArea = document.createElement("textarea");

        // Place in top-left corner of screen regardless of scroll position.
        textArea.style.position = 'fixed';
        textArea.style.top = '0';
        textArea.style.left = '0';

        // Ensure it has a small width and height. Setting to 1px / 1em
        // doesn't work as this gives a negative w/h on some browsers.
        textArea.style.width = '2em';
        textArea.style.height = '2em';

        // We don't need padding, reducing the size if it does flash render.
        textArea.style.padding = '0';

        // Clean up any borders.
        textArea.style.border = 'none';
        textArea.style.outline = 'none';
        textArea.style.boxShadow = 'none';

        // Avoid flash of white box if rendered for any reason.
        textArea.style.background = 'transparent';


        textArea.value = text;

        document.body.appendChild(textArea);

        textArea.select();

        try {
            var successful = document.execCommand('copy');
            var msg = successful ? 'successful' : 'unsuccessful';
            console.log('Copying text command was ' + msg);
        } catch (err) {
            console.log('Oops, unable to copy', err);
        }

        document.body.removeChild(textArea);
    }
}
