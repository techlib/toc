import { Injectable } from '@angular/core';
// import {Http, Response, URLSearchParams} from '@angular/http';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

import { AppState } from './app.state';
import { ScoreConfig } from './shared/score-config';
import { DictionaryMatch } from './shared/dictionary-match';

@Injectable()
export class AppService {

    // basefolder: string = '/home/kudela/.ntk/balicky/';
    constructor(
        private http: HttpClient,
        private state: AppState) { }


    private get<T>(url: string, params: HttpParams = new HttpParams(), responseType?): Observable<T> {
        // const r = re ? re : 'json';
        const options = { params, responseType, withCredentials: true };
        return this.http.get<T>(`api/${url}`, options);
    }

    private post(url: string, obj: any) {
        return this.http.post<any>(`api${url}`, obj);
    }

    processFolder(foldername: string, config: ScoreConfig): Observable<any> {

        let url = 'candidates/find';
        // url = '/assets/F21395f_000170834.json';
        const params: HttpParams = new HttpParams()
            .set('foldername', this.state.config.basefolder + foldername)
            .set('scoreconfig', JSON.stringify(config));
        return this.get(url, params);
    }

    processSysno(sysno: string, config: ScoreConfig): Observable<any> {

        let url = 'index/tag';
        // url = '/assets/F21395f_000170834.json'; // comment
        const params: HttpParams = new HttpParams()
            .set('sysno', sysno)
            .set('scoreconfig', JSON.stringify(config));
        return this.get(url, params);
    }

    getExport(cs: DictionaryMatch[]): Observable<any> {

        let url = 'candidates/export';
        const params: HttpParams = new HttpParams()
        .set('candidates', JSON.stringify(cs));
        return this.get(url, params);
    }

    saveNewKeys(cs: DictionaryMatch[]): Observable<any> {

        let url = 'candidates/add_to_nerizene';
        let params: HttpParams = new HttpParams();
        cs.forEach(dm => {
            if (dm.name === 'novy') {
                params = params.append('key', dm.text);
            }
        });
        // params.set('candidates', JSON.stringify(cs));
        return this.get(url, params);
    }

    saveToc(): Observable<any> {



        const headers = new HttpHeaders({ 'Content-Type': 'text/plain;charset=UTF-8' });
        const options = { headers };



        let url = 'candidates/save_toc?sysno=' + this.state.sysno;
        // let params: HttpParams = new HttpParams();

        // params.set('sysno', this.state.sysno);
        // params.set('toc', JSON.stringify(this.state.currentToc));

        return this.http.post<string>(url, JSON.stringify(this.state.currentToc), options);
    }

    updateFolders(): Observable<any> {

        let url = 'candidates/balicky';
        // url = '/assets/balicky.json'; // comment
        const params: HttpParams = new HttpParams();
        return this.get(url, params);
    }

    getBalicky(update: boolean = false): Observable<any> {

        let url = 'candidates/balicky';
        // url = '/assets/balicky.json'; // comment
        const params: HttpParams = new HttpParams();
        if (update) {
            params.set('update', 'true');
        }
        return this.get(url, params);
    }

    getBlacklist(): Observable<any> {

        let url = 'search/blacklist/select';
        // url = '/assets/blacklist.json'; // comment
        const params: HttpParams = new HttpParams().set('q', '*').set('rows', '100').set('sort', 'key asc');
        return this.get(url, params);
    }

    addToBlackList(key: string): Observable<any> {

        let url = 'candidates/ADD_TO_BLACKLIST';
        const params: HttpParams = new HttpParams().set('key', key);
        return this.get(url, params);
    }

    removeFromBlackList(key: string): Observable<any> {

        let url = 'candidates/REMOVE_FROM_BLACKLIST';
        const params: HttpParams = new HttpParams()
            .set('key', key);
        return this.get(url, params);
    }

    getTocText(sysno: string): Observable<any> {

        let url = 'mdt';
        // url = '/assets/toc.txt';
        const params: HttpParams = new HttpParams()
            .set('action', 'VIEW_TOC')
            .set('sysno', sysno);
        return this.get(url, params, 'text');
    }

    copyTextToClipboard(text) {
        const textArea = document.createElement('textarea');

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
            let successful = document.execCommand('copy');
            let msg = successful ? 'successful' : 'unsuccessful';
            console.log('Copying text command was ' + msg);
        } catch (err) {
            console.log('Oops, unable to copy', err);
        }

        document.body.removeChild(textArea);
    }
}
