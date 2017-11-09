import { Component, OnInit } from '@angular/core';
import { AppState } from '../app.state';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {

  constructor(public state: AppState) { }

  ngOnInit() {
  }
  
  setThreshold(e){
    this.state.threshold = parseInt(e) / 100.0;
  }

}
