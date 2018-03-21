import { Component } from '@angular/core';
import {MzBaseModal} from 'ng2-materialize';
import {AppService} from 'app/app.service';
import {DictionaryMatch} from 'app/models/dictionary-match';

@Component({
  selector: 'app-export-modal',
  templateUrl: './export-modal.component.html',
  styleUrls: ['./export-modal.component.scss']
})
export class ExportModalComponent  extends MzBaseModal{
    service: AppService;
    selected: DictionaryMatch[];
    exported: string;
    alephFormat : boolean;
    
    newkey: string;
    
    save(){
      this.service.saveNewKeys(this.selected).subscribe(res => {
        console.log(res);
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
