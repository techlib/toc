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
import {ExportModalComponent} from 'app/export-modal/export-modal.component';

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

  
  loading: boolean = false;
  hasToc: boolean = false;

  showMatched: boolean = true;
  showFree: boolean = true;

  maxScore: number = 0;


  constructor(
  private modalService: MzModalService,
      private router: Router,
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
  
  setSelected(){
    this.state.selected = [];
      this.state.candidates.forEach((c: Candidate) => {
          if (c.dictionaries) {
              c.dictionaries.forEach((dm: DictionaryMatch) => {
                  if (dm['selected']) {
                      this.state.selected.push(dm);
                  }
              });
          } else if (c['selected']) {
                let dm2 : DictionaryMatch = new DictionaryMatch();
                dm2.name = 'novy';
                dm2.text = c.text;
                //dm2.text = c.text;
                dm2.selected = true;
                this.state.selected.push(dm2);
            
          }

      });
  }
    
    saveNew(){
      this.setSelected();
      this.service.saveNewKeys(this.state.selected).subscribe(res => {
        console.log(res);
        this.service.saveToc().subscribe(res => {
          this.state.balicky[this.state.sysno]['saved'] = true;
          console.log(res);
        });
      });
    }

  add() {
      //this.loading = true;
      //this.selected = this.candidates.filter((c: Candidate) => {return c['selected']});
    this.setSelected();
      this.service.getExport(this.state.selected).subscribe(res => {
          //this.exported = JSON.stringify(res);
          //this.exportModal.open();
          
          this.modalService.open(ExportModalComponent,
           {service: this.service, state: this.state,
             selected: this.state.selected,
           exported: JSON.stringify(res)}
           );
          
//          console.log(this.exportArea);
//          setTimeout(() => {
//              this.exportArea.nativeElement.select();
//              let a = document.execCommand('copy');
//              console.log(a);
//          }, 10);
          this.loading = false;
      });
  }

  openToc() {
      this.service.getTocText(this.state.sysno).subscribe(res => {
          this.toc_text = res + '\n';
          //this.tocModal.open();
          this.modalService.open(TocModalComponent, {toc_text: this.toc_text, sysno: this.state.sysno, title: this.state.title});
          this.loading = false;
      });
  }
}
