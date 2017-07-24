import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, FormBuilder, Validators }            from '@angular/forms';

import { AppService } from '../app.service';
import { ScoreConfig } from '../models/score-config';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  foldername : string = '/home/alberto/Projects/NTK/balicky/F21395f_000170834/';
  candidates: any[] = [];
  loading : boolean = false;
  
  scoreConfig: FormGroup;
  
  constructor(
  private service: AppService,
  private fb: FormBuilder) {
    this.createForm();
  }

  createForm() {
    this.scoreConfig = this.fb.group(new ScoreConfig());
  }
  ngOnInit() {
  }
  
  analyze(){
    console.log(this.scoreConfig.value);
    this.loading = true;
    this.service.processFolder(this.foldername, this.scoreConfig.value).subscribe(res => {
      this.candidates = res['candidates'];
      this.loading = false;
    });
    
  }

}
