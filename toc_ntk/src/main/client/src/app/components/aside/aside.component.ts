import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AppService } from 'src/app/app.service';
import { AppState } from 'src/app/app.state';

@Component({
  selector: 'app-aside',
  templateUrl: './aside.component.html',
  styleUrls: ['./aside.component.scss']
})
export class AsideComponent implements OnInit {

  sysnos: string[] = [];
  sysno: string;

  constructor(
    private router: Router,
    private service: AppService,
    public state: AppState
  ) { }

  ngOnInit(): void {
    this.setFolders();
  }

  updateFolders(): void {
    this.setFolders(true);
  }

  setFolders(update: boolean = false): void {

    this.sysnos = [];

    this.service.getBalicky(update).subscribe(res => {
      this.sysno = this.state.sysno;
      this.state.balicky = res;
      const fs = Object.keys(res);
      fs.sort((a, b) => {
        return a.localeCompare(b);
      });
      this.sysnos = fs;

    });
  }

  setSysno(sysno: string): void {
    this.router.navigate(['/sysno', sysno]);
    this.state.setSysno(sysno, 'aside');
  }

}
