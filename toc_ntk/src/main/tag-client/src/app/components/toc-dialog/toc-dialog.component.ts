import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-toc-dialog',
  templateUrl: './toc-dialog.component.html',
  styleUrls: ['./toc-dialog.component.scss']
})
export class TocDialogComponent implements OnInit {

  constructor(
    public dialogRef: MatDialogRef<TocDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data) { }

  ngOnInit(): void {}

}
