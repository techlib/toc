import {Component, OnInit, ViewChild, ElementRef} from '@angular/core';
import {Router, ActivatedRoute} from '@angular/router';
import {Subscription} from 'rxjs/Subscription';
import {MzBaseModal, MzModalComponent} from 'ng2-materialize';

import {AppState} from '../app.state';
import {AppService} from '../app.service';
import {ScoreConfig} from '../models/score-config';
import {Candidate} from '../models/candidate';
import {DecimalPipe} from '@angular/common';


@Component({
  selector: 'app-analyze',
  templateUrl: './analyze.component.html',
  styleUrls: ['./analyze.component.scss']
})
export class AnalyzeComponent implements OnInit {

    @ViewChild('exportModal') exportModal: MzModalComponent;
    @ViewChild('tocModal') tocModal: MzModalComponent;
    @ViewChild('exportArea') exportArea: ElementRef;

    subscriptions: Subscription[] = [];

    error: string = '';
    toc_text: string;

    info: any = {};
    author: string = "";
    //exported: string;
    loading: boolean = false;

    showMatched: boolean = true;
    showFree: boolean = true;

    maxScore: number = 0;


    constructor(
    private numberPipe: DecimalPipe,
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

        let sysno = this.route.snapshot.paramMap.get('sysno');
        if (sysno) {
            setTimeout(() => this.state.setSysno(sysno, 'analyze'), 100);
        }

        this.subscriptions.push(this.state.stateChanged.subscribe(st => {
            this.analyze();
        }));
    }

    setSysno() {
//        this.router.navigate(['/sysno', this.state.sysno]);
        this.analyze();
    }

    select(c: string) {
        //this.selected = this.candidates.filter((c: Candidate) => {return c['selected'] !== 'undefined' && c['selected']});
        this.service.copyTextToClipboard(c);
    }

    analyze() {
        this.loading = true;
        this.info = {};
        this.state.title = '';
        this.state.hasToc = false;
        this.error = '';
        this.author = '';
        this.state.candidates = [];
        this.state.selected = [];
        this.service.processSysno(this.state.sysno, this.state.scoreConfig).subscribe(res => {
            
            if(res.hasOwnProperty('error')){
                this.error = res['error'];
                this.state.hasToc = false;
                this.loading = false;
            }else{
                
                this.state.candidates = res['candidates'];
                this.info = res['info'];
                this.setInfo();
                this.rescore();
                this.state.hasToc = true;
                this.loading = false;

                setTimeout(() => {
                    $("#app-table-score").tableHeadFixer();
                }, 1);
            }
        });
    }

    setInfo() {
        this.state.title = '';
        for (let i in this.info['varfield']) {
            let vf = this.info['varfield'][i];
            if (vf['id'] === 245) {
                for (let sb in vf['subfield']) {
                    this.state.title += vf['subfield'][sb]['content'] + ' ';
                }
            }
        }
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
        this.state.candidates.forEach((c: Candidate) => {
            c.explain = [];
            if (c.blacklisted) {
                c.score = 0;
                c.explain.push('is in blaclist');
            } else {
                let sc: ScoreConfig = this.state.scoreConfig;
                c.score = 1;
                if (c.isMatched) {
                    c.score = c.score * sc.matched;
                    c.explain.push('matched in dictionaries ( ' + sc.matched + ' ) = ' + this.numberPipe.transform(c.score, '1.1-3'));
                    if (c.text.split(" ").length > 1) {
                        c.score = c.score * sc.multiple;
                        c.explain.push('is a mulpitle word keyword ( ' + sc.multiple + ' ) = ' + this.numberPipe.transform(c.score, '1.1-3'));
                    }

                    for (let i in c.dictionaries) {
                        let dm = c.dictionaries[i];
                        if (sc.dictionaries.hasOwnProperty(dm['name'])) {
                            c.score = c.score * sc.dictionaries[dm['name']];
                            c.explain.push('matched in ' + dm['name'] + ' dictionary ( ' + sc.dictionaries[dm['name']] + ' ) = ' + this.numberPipe.transform(c.score, '1.1-3'));
                        }
                    }


                }
                if (c.hasProperNoun) {
                    c.score = c.score * sc.hasProperNoun;
                        c.explain.push('keyword has proper noun ( ' + sc.hasProperNoun + ' ) = ' + this.numberPipe.transform(c.score, '1.1-3'));
                }

//                if (c.type == 'DICTIONARY_WORD') {
//                    c.score = c.score * sc.isDictionaryWord;
//                    c.explain.push('is a dictionary word keyword ( ' + sc.isDictionaryWord + ' ) = ' + c.score);
//                }
                if (c.inTitle) {
                    c.score = c.score * sc.inTitle;
                    c.explain.push('keyword found in title ( ' + sc.inTitle + ' ) = ' + this.numberPipe.transform(c.score, '1.1-3'));
                }
                c.score += c.found * sc.found;
                c.explain.push('key word found ' + c.found + ' times ( ' + sc.found + ' ) = ' + this.numberPipe.transform(c.score, '1.1-3'));
                c.score += c.extent * sc.extent;
                c.explain.push('keyword has a extent of ' + c.extent + ' pages ( ' + sc.extent + ' ) = ' + this.numberPipe.transform(c.score, '1.1-3'));
                this.maxScore = Math.max(this.maxScore, c.score);
            }
        });
        this.state.candidates.sort((a, b) => {return b.score - a.score})

    }
    
    formatInfo(c: Candidate): string{
        let ret: string = c.text + '\n\n';
        for(let e in c.explain){
            ret += c.explain[e] + '\n';
        }
        return ret;
    }
    
    formatTooltip(c: Candidate): string{
        let ret: string = c.text + '<br/><br/>';
        for(let e in c.explain){
            ret += c.explain[e] + '<br/>';
        }
        return ret;
    }

    sp(e, d) {
        console.log(e, d);
    }

    openToc() {
        this.service.getTocText(this.state.sysno).subscribe(res => {
            this.toc_text = res + '\n';
            this.tocModal.open();
            this.loading = false;
        });
    }

    addToBlackList(c: Candidate) {
        this.service.addToBlackList(c.text).subscribe(res => {
            console.log(res);
        });
    }
  
    toogleAsideBar() {
      this.state.showAsideBar();
    }
}