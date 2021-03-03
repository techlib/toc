import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { AppService } from 'src/app/app.service';
import { AppState } from 'src/app/app.state';
import { Candidate } from 'src/app/models/candidate';

@Component({
  selector: 'app-analyze',
  templateUrl: './analyze.component.html',
  styleUrls: ['./analyze.component.scss']
})
export class AnalyzeComponent implements OnInit, OnDestroy {

  subscriptions: Subscription[] = [];

  error = '';
  toc_text: string;

  info: any = {};
  author = '';
  // exported: string;
  loading = false;

  showMatched = true;
  showFree = true;

  displayedColumns: string[] = ['score', 'PSH', 'keywords', 'konspekt', 'noslovnik'];

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    public state: AppState,
    private service: AppService) { }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s: Subscription) => {
      s.unsubscribe();
    });
    this.subscriptions = [];
  }

  ngOnInit(): void {

    const sysno = this.route.snapshot.paramMap.get('sysno');
    if (sysno) {
      setTimeout(() => this.state.setSysno(sysno, 'analyze'), 10);
    }

    this.subscriptions.push(this.state.stateChanged.subscribe(st => {
      this.analyze();
    }));
  }

  setSysno(): void {
    //        this.router.navigate(['/sysno', this.state.sysno]);
    this.analyze();
  }

  select(c: string): void {
    // this.selected = this.candidates.filter((c: Candidate) => {return c['selected'] !== 'undefined' && c['selected']});

    this.service.copyTextToClipboard(c);
  }

  analyze(): void {
    this.loading = true;
    this.info = {};
    this.state.title = '';
    this.state.hasToc = false;
    this.error = '';
    this.author = '';
    this.state.candidates = [];
    this.state.selected = [];
    this.service.processSysno(this.state.sysno, this.state.scoreConfig).subscribe(res => {

      if (res.hasOwnProperty('error')) {
        this.error = res.error;
        this.state.hasToc = false;
        this.loading = false;
      } else {

        this.state.currentToc = res;
        this.state.candidates = res.candidates;
        this.info = res.info;
        this.setInfo();
        this.state.rescore();
        this.state.hasToc = true;
        this.loading = false;

        setTimeout(() => {
          // $("#app-table-score").tableHeadFixer();
        }, 1);
      }
    });
  }

  setInfo(): void {
    this.state.title = '';
    for (const i in this.info.varfield) {
      const vf = this.info.varfield[i];
      if (vf.id === 245) {
        for (const sb in vf.subfield) {
          this.state.title += vf.subfield[sb].content + ' ';
        }
      }
    }
  }

  selectElementText(el): void {
    let doc = window.document;
    if (window.getSelection && doc.createRange) {
      const sel = window.getSelection();
      const range = doc.createRange();
      range.selectNodeContents(el);
      sel.removeAllRanges();
      sel.addRange(range);
    } else {
      el.select();
    }
  }



  formatInfo(c: Candidate): string {
    let ret: string = c.text + '\n\n';
    c.explain.forEach(e => {
      ret += e + '\n';
    });
    return ret;
  }

  formatTooltip(c: Candidate): string {
    let ret: string = c.text + '<br/><br/>';
    c.explain.forEach(e => {
      ret += e + '<br/>';
    });
    return ret;
  }

  sp(e, d): void {
    console.log(e, d);
  }

  addToBlackList(c: Candidate): void {
    this.service.addToBlackList(c.text).subscribe(res => {
      console.log(res);
    });
  }

  toogleAsideBar(): void {
    this.state.showAsideBar();
  }

}
