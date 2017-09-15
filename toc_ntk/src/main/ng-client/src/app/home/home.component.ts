import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, FormBuilder, Validators } from '@angular/forms';

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

  toc_text: string;

  candidates: Candidate[] = [];
  loading: boolean = false;

  scoreConfig: ScoreConfig = new ScoreConfig();

  constructor(
    public state: AppState,
    private service: AppService,
    private fb: FormBuilder) {
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

  analyze() {
    this.loading = true;
    this.candidates = [];
    this.service.processFolder(this.state.foldername, this.scoreConfig).subscribe(res => {
      this.candidates = res['candidates'];
      this.rescore();
      this.loading = false;
    });


  }

  modalOpened() {
    this.service.getTocText(this.state.foldername).subscribe(res => {
      this.toc_text = res;
    });
  }

  rescore() {
    this.candidates.forEach((c: Candidate) => {
      let sc: ScoreConfig = this.scoreConfig;
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


  public modalOptions: Materialize.ModalOptions = {
    ready: (modal, trigger) => { // Callback for Modal open. Modal and trigger parameters available.
      
      this.service.getTocText(this.state.foldername).subscribe(res => {
        this.toc_text = res;
      });
      console.log(modal, trigger);
    }
  };

}