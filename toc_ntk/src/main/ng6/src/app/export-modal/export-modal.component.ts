import { Component } from '@angular/core';
import {MzBaseModal} from 'ngx-materialize';
import {AppService} from 'src/app/app.service';
import {DictionaryMatch} from 'src/app/models/dictionary-match';
import {AppState} from 'src/app/app.state';
import {Candidate} from 'src/app/models/candidate';

@Component({
  selector: 'app-export-modal',
  templateUrl: './export-modal.component.html',
  styleUrls: ['./export-modal.component.scss']
})
export class ExportModalComponent  extends MzBaseModal{
    service: AppService;
    state: AppState;
    selected: DictionaryMatch[];
    exported: string;
    alephFormat : boolean;
    
    newkey: string;
    
    save(){
      this.service.saveNewKeys(this.selected).subscribe(res => {
        this.selected.forEach(dm => {
          if (dm.name === 'novy'){
            dm.name = 'nerizene';
          }
        });
        this.service.saveToc().subscribe(res => {
          this.state.balicky[this.state.sysno]['saved'] = true;
          console.log(res);
        });
      });
    }
    
    add(){
      let dm2 : DictionaryMatch = new DictionaryMatch();
      dm2.name = 'nerizene';
      dm2.text = this.newkey;
      //dm2.text = c.text;
      dm2.selected = true;
      this.selected.push(dm2);
      let c: Candidate = new Candidate(); 
      c.text = this.newkey;
      c.isMatched = true;
      c.selected = true;
      c.dictionaries.push(dm2);
      this.state.currentToc['candidates'].unshift(c);
      this.newkey = '';
    }
    
    remove(idx: number){
      this.selected.splice(idx, 1);
    }
}
