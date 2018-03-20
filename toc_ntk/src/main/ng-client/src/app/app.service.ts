import {Injectable} from '@angular/core';
import {Http, Response, URLSearchParams} from '@angular/http';
import {Observable} from 'rxjs/Rx';

import {AppState} from './app.state';
import {ScoreConfig} from './models/score-config';
import {DictionaryMatch} from './models/dictionary-match';

@Injectable()
export class AppService {

    //basefolder: string = '/home/kudela/.ntk/balicky/';
    constructor(
        private http: Http,
        private state: AppState) {}


    processFolder(foldername: string, config: ScoreConfig): Observable<any> {

        var url = 'candidates';
        //url = '/assets/F21395f_000170834.json';
        let params: URLSearchParams = new URLSearchParams();
        params.set('action', 'FIND');
        params.set('foldername', this.state.config['basefolder'] + foldername);
        params.set('scoreconfig', JSON.stringify(config));
        return this.http.get(url, {search: params})
            .map((response: Response) => {
                return response.json();
            });
    }

    processSysno(sysno: string, config: ScoreConfig): Observable<any> {

        var url = 'candidates';
        //url = '/assets/F21395f_000170834.json'; // comment
        let params: URLSearchParams = new URLSearchParams();
        params.set('action', 'FIND');
        params.set('sysno', sysno);
        params.set('scoreconfig', JSON.stringify(config));
        return this.http.get(url, {search: params})
            .map((response: Response) => {
                return response.json();
            });
    }

    getExport(cs: DictionaryMatch[]): Observable<any> {

        var url = 'candidates';
        let params: URLSearchParams = new URLSearchParams();
        params.set('action', 'EXPORT');
        params.set('candidates', JSON.stringify(cs));
        return this.http.get(url, {search: params})
            .map((response: Response) => {
                return response.json();
            });
    }
    
    saveNewKeys(cs: DictionaryMatch[]): Observable<any> {

        var url = 'candidates';
        let params: URLSearchParams = new URLSearchParams();
        params.set('action', 'ADD_TO_NERIZENE');
        cs.forEach(dm => {
          if (dm.name === 'novy'){
            params.append('key', dm.text);
          }
        })
        //params.set('candidates', JSON.stringify(cs));
        return this.http.get(url, {search: params})
            .map((response: Response) => {
                return response.json();
            });
    }
    
    updateFolders(): Observable<any> {

        var url = 'candidates';
        //url = '/assets/balicky.json'; // comment
        let params: URLSearchParams = new URLSearchParams();
        params.set('action', 'BALICKY');
        return this.http.get(url, {search: params})
            .map((response: Response) => {
                return response.json();
            });
    }

    getBalicky(update: boolean = false): Observable<any> {

        var url = 'candidates';
        //url = '/assets/balicky.json'; // comment
        let params: URLSearchParams = new URLSearchParams();
        params.set('action', 'BALICKY');
        if(update){
            params.set('update', 'true');
        }
        return this.http.get(url, {search: params})
            .map((response: Response) => {
                return response.json();
            });
    }

    getBlacklist(): Observable<any> {

        var url = 'search/blacklist/select';
        //url = '/assets/blacklist.json'; // comment
        let params: URLSearchParams = new URLSearchParams();
        params.set('q', '*');
        params.set('rows', '100');
        params.set('sort', 'key asc');
        return this.http.get(url, {search: params})
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
        return this.http.get(url, {search: params})
            .map((response: Response) => {
                return response.json();
            });
    }

    removeFromBlackList(key: string): Observable<any> {

        var url = 'candidates';
        let params: URLSearchParams = new URLSearchParams();
        params.set('action', 'REMOVE_FROM_BLACKLIST');
        params.set('key', key);
        return this.http.get(url, {search: params})
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
        return this.http.get(url, {search: params})
            .map((response: Response) => {
                return response.text();
            });
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
