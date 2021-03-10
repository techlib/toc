import { TagPlaceholder } from '@angular/compiler/src/i18n/i18n_ast';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router, ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { AppConfiguration } from 'src/app/app-configuration';
import { AppService } from 'src/app/app.service';
import { AppState } from 'src/app/app.state';
import { TocDialogComponent } from 'src/app/components/toc-dialog/toc-dialog.component';
import { ScoreConfig } from 'src/app/shared/score-config';
import { TagCandidate } from 'src/app/shared/tag-candidate';

interface ThemeCount { label: string, count: number, selected: boolean };

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

  titleThemes: ThemeCount[] = [];
  authorThemes: ThemeCount[] = [];
  bodyThemes: ThemeCount[] = [];
  candidates: TagCandidate[];
  selected: string[] = [];

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    public dialog: MatDialog,
    private config: AppConfiguration,
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

  themeChange(th, e) {
    th.selected = e.checked;
    this.rescore();
  }

  rescore() {
    const titleThemes = this.titleThemes.filter(t => t.selected);
    const bodyThemes = this.bodyThemes.filter(t => t.selected);
    const authorThemes = this.authorThemes.filter(t => t.selected);

    this.candidates.forEach(tc => {
      this.setScore(tc, this.config.scoreConfig, titleThemes, authorThemes, bodyThemes);
    });
    this.candidates.sort((a, b) => b.score - a.score);
  }

  setScore(tc: TagCandidate, conf: ScoreConfig, titleThemes: ThemeCount[], authorThemes: ThemeCount[], bodyThemes: ThemeCount[]) {
    tc.explain = [];
    tc.score = tc.count * (tc.isInTitle ? conf.isInTitle : 1);
    tc.explain.push("count:" + tc.count);
    tc.explain.push(" * (is in title)(" + (tc.isInTitle ? conf.isInTitle : 1) + ")");

    titleThemes.forEach(th => {
      tc.path.forEach(p => {
        if (!conf.excludedThemes.includes(th.label) && p.indexOf(th.label)>-1) {
          tc.score = tc.score * conf.titleTheme;
          tc.explain.push(" * (theme " + th.label + " found " + th.count + " times in title) *" + conf.titleTheme);
        }
      });
    });

    authorThemes.forEach(th => {
      tc.path.forEach(p => {
        if (!conf.excludedThemes.includes(th.label) && p.indexOf(th.label)>-1) {
          tc.score = tc.score * conf.authorTheme;
          tc.explain.push(" * (theme " + th.label + " found " + th.count + " times in author) *" + conf.authorTheme);
        }
      });
    });

    bodyThemes.forEach(th => {
      tc.path.forEach(p => {
        if (!conf.excludedThemes.includes(th.label) && p.indexOf(th.label)>-1) {
          tc.score = tc.score + th.count * conf.bodyTheme;
          tc.explain.push(" + (theme " + th.label + " found " + th.count + " times in TOC)");
        }
      });
    });

  }

  analyze(): void {
    this.loading = true;
    this.info = {};
    this.state.title = '';
    this.state.hasToc = false;
    this.error = '';
    this.author = '';
    this.candidates = [];
    // this.state.selected = [];
    this.service.processSysno(this.state.sysno, this.state.scoreConfig).subscribe(res => {

      if (res.hasOwnProperty('error')) {
        this.error = res.error;
        this.state.hasToc = false;
        this.loading = false;
      } else {

        this.state.currentToc = res;
        this.candidates = res.candidates;
        this.info = res.info;
        this.setInfo();
        this.state.hasToc = true;
        this.loading = false;

        this.titleThemes = [];
        const ks = Object.keys(res.info.themes);
        ks.forEach(k => {
          this.titleThemes.push({ label: k, count: res.info.themes[k], selected: true })
        });

        this.authorThemes = [];
        const ksa = Object.keys(res.authorThemes);
        ksa.forEach(k => {
          this.authorThemes.push({ label: k, count: res.authorThemes[k], selected: true })
        });
        this.bodyThemes = [];
        const ks2 = Object.keys(res.body.themes);
        ks2.forEach(k => {
          this.bodyThemes.push({ label: k, count: res.body.themes[k], selected: true })
        });

        this.bodyThemes.sort((a, b) => b.count - a.count);
        this.rescore();
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

  formatInfo(c: TagCandidate): string {
    let ret: string = c.text + '\n\n';
    c.explain.forEach(e => {
      ret += e + '\n';
    });
    return ret;
  }

  formatTooltip(c: TagCandidate): string {
    let ret: string = c.text + '<br/><br/>';
    c.explain.forEach(e => {
      ret += e + '<br/>';
    });
    return ret;
  }

  sp(e, d): void {
    console.log(e, d);
  }

  addToBlackList(c: TagCandidate): void {
    this.service.addToBlackList(c.text).subscribe(res => {
      console.log(res);
    });
  }

  toogleAsideBar(): void {
    this.state.showAsideBar();
  }

  viewToc(): void {
    
    this.service.getTocText(this.state.sysno).subscribe(res => {
      this.toc_text = res + '\n';
      const dialogRef = this.dialog.open(TocDialogComponent, {
        width: '750px',
        data: this.toc_text
      });
  
      dialogRef.afterClosed().subscribe(result => {
        console.log('The dialog was closed');
        
      });
  });

    
  }

  addToSelected(s: string) {
    if (!this.selected.includes(s)) {
      this.selected.push(s);
    }
    
  }

  removeFromSelected(index: number) {
    this.selected.splice(index, 1);
  }

  save() {

  }

  copy() {
    this.service.copyTextToClipboard(this.selected.join(';'));
  }

}
