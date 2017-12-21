import {Component, OnInit} from '@angular/core';
import {MzBaseModal} from 'ng2-materialize';

@Component({
    selector: 'app-toc-modal',
    templateUrl: './toc-modal.component.html',
    styleUrls: ['./toc-modal.component.scss']
})
export class TocModalComponent extends MzBaseModal {
    sysno: string;
    title: string;
    toc_text: string;

}
