import { Component, OnInit, ViewChild } from '@angular/core';
//import { FormBuilder } from '@angular/forms';
import { MzBaseModal, MzModalComponent } from 'ng2-materialize';

import { AppState } from '../app.state';
import { AppService } from '../app.service';
import { ScoreConfig } from '../models/score-config';
import { Candidate } from '../models/candidate';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  @ViewChild('exportModal') exportModal: MzModalComponent;
  @ViewChild('tocModal') tocModal: MzModalComponent;
  toc_text: string;

  candidates: Candidate[] = [];
  selected: Candidate[] = [];
  exported: string;
  loading: boolean = false;
  hasToc: boolean = false;
  
  showMatched: boolean = true;
  showFree: boolean = true;


  constructor(
    public state: AppState,
    private service: AppService) {
    this.createForm();
  }

  createForm() {
    //    sc.dicts = [['PSH', '653_klicova_slova_b.txt']];
    //    this.scoreConfig = this.fb.group(sc);
  }
  ngOnInit() {
    this.state.stateChanged.subscribe(st => {
      this.analyze();
    });
  }
  
  select(){
    
    this.selected = this.candidates.filter((c: Candidate) => { return c['selected'] !== 'undefined' && c['selected'] });
  }

  analyze() {
    this.loading = true;
    this.candidates = [];
    this.selected = [];
    this.service.processFolder(this.state.foldername, this.state.scoreConfig).subscribe(res => {
      this.candidates = res['candidates'];
      this.rescore();
      this.hasToc = true;
      this.loading = false;
    });
  }

  export() {
    this.loading = true;
    this.selected = this.candidates.filter((c: Candidate) => { return c['selected'] });
    this.service.export(this.selected).subscribe(res => {
      this.exported = JSON.stringify(res);
      this.exportModal.open();
      this.loading = false;
    });
  }

  rescore() {
    this.candidates.forEach((c: Candidate) => {
      let sc: ScoreConfig = this.state.scoreConfig;
      c.score = 1;
      if (c.isMatched) {
        c.score = c.score * sc.matched;
        if (c.text.split(" ").length > 1) {
          c.score = c.score * sc.multiple;
        }

        for (let i in c.dictionaries) {
          let dm = c.dictionaries[i];
          if (sc.dictionaries.hasOwnProperty(dm)) {
            c.score = c.score * sc.dictionaries[dm];
          }
        }


      }
      if (c.hasProperNoun) {
        c.score = c.score * sc.hasProperNoun;
      }

      if (c.type == 'DICTIONARY_WORD') {
        c.score = c.score * sc.isDictionaryWord;
      }
      c.score += c.found * sc.found;
    });
    this.candidates.sort((a, b) => { return b.score - a.score })

  }

  sp(e, d) {
    console.log(e, d);
  }

  openToc() {
    this.service.getTocText(this.state.foldername).subscribe(res => {
      this.toc_text = res;
      this.tocModal.open();
      this.loading = false;
    });
  }

}