import {Component, OnInit} from '@angular/core';
//import {Http} from '@angular/http';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
//import {Observable} from 'rxjs/Observable';
//import {Subscription} from 'rxjs/Subscription';
//import {Subject} from 'rxjs/Subject';
//import {ActivatedRoute, Router, NavigationStart, NavigationEnd, NavigationExtras} from '@angular/router';

import {AppService} from './app.service';
import {AppState} from './app.state';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  constructor(
    public state: AppState,
    private service: AppService,
    private http: HttpClient) {

  }

  ngOnInit() {

    this.getConfig().subscribe(
      cfg => {
        this.processUrl();
      }
    );
  }


  getConfig() {
    return this.http.get("assets/config.json").map(res => {
      let cfg = res;

      this.state.setConfig(cfg);
      var userLang = navigator.language.split('-')[0]; // use navigator lang if available
      userLang = /(cs|en)/gi.test(userLang) ? userLang : 'cs';
      if (cfg.hasOwnProperty('defaultLang')) {
        userLang = cfg['defaultLang'];
      }
//      this.service.changeLang(userLang);
      return this.state.config;
    });
  }
  
  processUrl() {
//    this.router.events.subscribe(val => {
//      if (val instanceof NavigationEnd) {
//        //this.state.paramsChanged();
//      } 
//    });
//
//    this.route.queryParams.subscribe(searchParams => {
//      this.processUrlParams(searchParams);
//    });

    //this.state.paramsChanged();
  }

  processUrlParams(searchParams) {
    for (let p in searchParams) {

    }
  }

}
