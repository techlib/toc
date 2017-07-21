import { Component, OnInit } from '@angular/core';

import { AppService } from '../app.service';
import { ScoreConfig } from '../score-config';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  foldername : string = '/home/alberto/Projects/NTK/balicky/F21395f_000170834/';
  candidates: any[] = [];
  loading : boolean = false;
  constructor(private service: AppService) { }

  ngOnInit() {
  }
  
  analyze(){
    this.loading = true;
    this.service.processFolder(this.foldername, new ScoreConfig()).subscribe(res => {
      this.candidates = res['candidates in dictionary'];
    this.loading = false;
    });
    
  }

}
