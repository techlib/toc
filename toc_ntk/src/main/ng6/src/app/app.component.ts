import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';

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
        this.state.setConfig(cfg);
        var userLang = navigator.language.split('-')[0]; // use navigator lang if available
        userLang = /(cs|en)/gi.test(userLang) ? userLang : 'cs';
        if (cfg.hasOwnProperty('defaultLang')) {
          userLang = cfg['defaultLang'];
        }
        this.processUrl();
      }
    );
  }


  getConfig() {
    return this.http.get("assets/config.json");
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
