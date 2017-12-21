import { Component } from '@angular/core';
import {MzBaseModal} from 'ng2-materialize';

@Component({
  selector: 'app-export-modal',
  templateUrl: './export-modal.component.html',
  styleUrls: ['./export-modal.component.scss']
})
export class ExportModalComponent  extends MzBaseModal{
    exported: string;
}
