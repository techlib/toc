import {Component, OnInit, ViewChild, ElementRef} from '@angular/core';
import {Router, ActivatedRoute} from '@angular/router';
import {Subscription} from 'rxjs/Subscription';
import {MzBaseModal, MzModalComponent} from 'ng2-materialize';

import {AppState} from '../app.state';
import {AppService} from '../app.service';
import {ScoreConfig} from '../models/score-config';
import {Candidate} from '../models/candidate';
import {DictionaryMatch} from '../models/dictionary-match';


@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  @ViewChild('exportModal') exportModal: MzModalComponent;
  @ViewChild('tocModal') tocModal: MzModalComponent;
  @ViewChild('exportArea') exportArea: ElementRef;

  subscriptions: Subscription[] = [];


  toc_text: string;

  info: any = {};
  title: string = "";
  author: string = "";
  candidates: Candidate[] = [];
  selected: DictionaryMatch[] = [];
  exported: string;
  loading: boolean = false;
  hasToc: boolean = false;

  showMatched: boolean = true;
  showFree: boolean = true;

  maxScore: number = 0;


  constructor(
    private router: Router,
    private route: ActivatedRoute,
    public state: AppState,
    private service: AppService) {
  }

  ngOnDestroy() {
    this.subscriptions.forEach((s: Subscription) => {
      s.unsubscribe();
    });
    this.subscriptions = [];
  }

  ngOnInit() {

    this.subscriptions.push(this.state.stateChanged.subscribe(st => {
      this.analyze();
    }));

    let sysno = this.route.snapshot.paramMap.get('sysno');
    if (sysno) {
      this.state.setSysno(sysno);
    }
  }

  setSysno() {
    this.router.navigate(['/sysno', this.state.sysno]);
    this.analyze();
  }

  select() {
    //this.selected = this.candidates.filter((c: Candidate) => {return c['selected'] !== 'undefined' && c['selected']});
  }

  analyze() {
    this.loading = true;
    this.info = {};
    this.title = '';
    this.author = '';
    this.candidates = [];
    this.selected = [];
    this.service.processSysno(this.state.sysno, this.state.scoreConfig).subscribe(res => {
      this.candidates = res['candidates'];
      this.info = res['info'];
      this.setInfo();
      this.rescore();
      this.hasToc = true;
      this.loading = false;

      setTimeout(() => {
        $("#app-table-score").tableHeadFixer();
      }, 1);
    });
  }

  setInfo() {
    for (let i in this.info['varfield']) {
      let vf = this.info['varfield'][i];
      if (vf['id'] === 245) {
        for (let sb in vf['subfield']) {
          this.title += vf['subfield'][sb]['content'];
        }
      }
    }
  }

  showExport() {
    //this.loading = true;
    //this.selected = this.candidates.filter((c: Candidate) => {return c['selected']});
    this.selected = [];
    this.candidates.forEach((c: Candidate) => {
      if (c.dictionaries) {
        c.dictionaries.forEach((dm: DictionaryMatch) => {
          if (dm['selected']) {
            this.selected.push(dm)
          }
        });
      }

    });
    this.service.getExport(this.selected).subscribe(res => {
      this.exported = JSON.stringify(res);
      this.exportModal.open();
      console.log(this.exportArea);
      setTimeout(() => {
         this.exportArea.nativeElement.select();
         let a = document.execCommand('copy');
         console.log(a);
      }, 10);
      this.loading = false;
    });
  }

  selectElementText(el) {
    var doc = window.document;
    if (window.getSelection && doc.createRange) {
      let sel = window.getSelection();
      let range = doc.createRange();
      range.selectNodeContents(el);
      sel.removeAllRanges();
      sel.addRange(range);
    } else {
      el.select();
//    } else if (doc.body.createTextRange) {
//      let range = doc.body.createTextRange();
//      range.moveToElementText(el);
//      range.select();
    }
  }



  rescore() {
    this.maxScore = 0;
    this.candidates.forEach((c: Candidate) => {
      if (c.blacklisted) {
        c.score = 0;
      } else {
        let sc: ScoreConfig = this.state.scoreConfig;
        c.score = 1;
        if (c.isMatched) {
          c.score = c.score * sc.matched;
          if (c.text.split(" ").length > 1) {
            c.score = c.score * sc.multiple;
          }

          for (let i in c.dictionaries) {
            let dm = c.dictionaries[i];
            if (sc.dictionaries.hasOwnProperty(dm['name'])) {
              c.score = c.score * sc.dictionaries[dm['name']];
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
        c.score += c.extents[0] * sc.extent;
        this.maxScore = Math.max(this.maxScore, c.score);
      }
    });
    this.candidates.sort((a, b) => {return b.score - a.score})

  }

  sp(e, d) {
    console.log(e, d);
  }

  openToc() {
    this.service.getTocText(this.state.sysno).subscribe(res => {
      this.toc_text = res;
      this.tocModal.open();
      this.loading = false;
    });
  }

  addToBlackList(c: Candidate) {
    this.service.addToBlackList(c.text).subscribe(res => {
      console.log(res);
    });
  }
}
