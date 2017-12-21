import {Component, OnInit, ViewChild, ElementRef} from '@angular/core';
import {Router, ActivatedRoute} from '@angular/router';
import {Subscription} from 'rxjs/Subscription';
import {MzBaseModal, MzModalComponent, MzModalService} from 'ng2-materialize';

import {AppState} from '../app.state';
import {AppService} from '../app.service';
import {ScoreConfig} from '../models/score-config';
import {Candidate} from '../models/candidate';
import {DictionaryMatch} from '../models/dictionary-match';
import {TocModalComponent} from 'app/toc-modal/toc-modal.component';

@Component({
  selector: 'app-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss']
})
export class ToolbarComponent implements OnInit {

  @ViewChild('exportModal') exportModal: MzModalComponent;
  @ViewChild('tocModal') tocModal: MzModalComponent;
  @ViewChild('exportArea') exportArea: ElementRef;

  subscriptions: Subscription[] = [];

  error: string = '';
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
  private modalService: MzModalService,
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
  }

  setSysno() {
      this.router.navigate(['/sysno', this.state.sysno]);
      this.state.setSysno(this.state.sysno, 'tool');
  }

  select(c: string) {
      //this.selected = this.candidates.filter((c: Candidate) => {return c['selected'] !== 'undefined' && c['selected']});
      this.service.copyTextToClipboard(c);
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
          c.explain = [];
          if (c.blacklisted) {
              c.score = 0;
              c.explain.push('is in blaclist');
          } else {
              let sc: ScoreConfig = this.state.scoreConfig;
              c.score = 1;
              if (c.isMatched) {
                  c.score = c.score * sc.matched;
                  c.explain.push('matched in dictionaries ( ' + sc.matched + ' ) = ' + c.score);
                  if (c.text.split(" ").length > 1) {
                      c.score = c.score * sc.multiple;
                      c.explain.push('is a mulpitle word keyword ( ' + sc.multiple + ' ) = ' + c.score);
                  }

                  for (let i in c.dictionaries) {
                      let dm = c.dictionaries[i];
                      if (sc.dictionaries.hasOwnProperty(dm['name'])) {
                          c.score = c.score * sc.dictionaries[dm['name']];
                          c.explain.push('matched in ' + dm['name'] + ' dictionary ( ' + sc.dictionaries[dm['name']] + ' ) = ' + c.score);
                      }
                  }


              }
              if (c.hasProperNoun) {
                  c.score = c.score * sc.hasProperNoun;
                      c.explain.push('keyword has proper noun ( ' + sc.hasProperNoun + ' ) = ' + c.score);
              }

//                if (c.type == 'DICTIONARY_WORD') {
//                    c.score = c.score * sc.isDictionaryWord;
//                    c.explain.push('is a dictionary word keyword ( ' + sc.isDictionaryWord + ' ) = ' + c.score);
//                }
              if (c.inTitle) {
                  c.score = c.score * sc.inTitle;
                  c.explain.push('keyword found in title ( ' + sc.inTitle + ' ) = ' + c.score);
              }
              c.score += c.found * sc.found;
              c.explain.push('key word found ' + c.found + ' times ( ' + sc.found + ' ) = ' + c.score);
              c.score += c.extent * sc.extent;
              c.explain.push('keyword has a extent of ' + c.extent + ' pages ( ' + sc.extent + ' ) = ' + c.score);
              this.maxScore = Math.max(this.maxScore, c.score);
          }
      });
      this.candidates.sort((a, b) => {return b.score - a.score})

  }

  formatInfo(c: Candidate): string{
      let ret: string = c.text + '\n\n';
      for(let e in c.explain){
          ret += c.explain[e] + '\n';
      }
      return ret;
  }

  sp(e, d) {
      console.log(e, d);
  }

  openToc() {
      this.service.getTocText(this.state.sysno).subscribe(res => {
          this.toc_text = res + '\n';
          //this.tocModal.open();
          this.modalService.open(TocModalComponent, {toc_text: this.toc_text, sysno: this.state.sysno, title: this.state.title});
          this.loading = false;
      });
  }

  addToBlackList(c: Candidate) {
      this.service.addToBlackList(c.text).subscribe(res => {
          console.log(res);
      });
  }
}
