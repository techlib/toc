import { Component } from '@angular/core';
import {MzBaseModal} from 'ng2-materialize';
import {AppService} from 'app/app.service';
import {DictionaryMatch} from 'app/models/dictionary-match';
import {AppState} from 'app/app.state';

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
        this.service.saveToc().subscribe(res => {
          this.state.balicky[this.state.sysno]['saved'] = true;
          console.log(res);
        });
      });
    }
    
    add(){
      let dm2 : DictionaryMatch = new DictionaryMatch();
      dm2.name = 'novy';
      dm2.text = this.newkey;
      //dm2.text = c.text;
      dm2.selected = true;
      this.selected.push(dm2);
      this.newkey = '';
    }
    
    remove(idx: number){
      this.selected.splice(idx, 1);
    }
}
