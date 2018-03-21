import { BrowserModule } from '@angular/platform-browser';
import { CommonModule, DecimalPipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms'; 
import {HttpClientModule, HttpClient} from '@angular/common/http';
import { RouterModule } from '@angular/router';

import { MaterializeModule } from 'ng2-materialize';

import { AppComponent } from './app.component';
import { AppService } from './app.service';
import { AppState } from './app.state';
import { NavbarComponent } from './navbar/navbar.component';
import { FooterComponent } from './footer/footer.component';
import { HomeComponent } from './home/home.component';
import { AsideComponent } from './aside/aside.component';
import { BlacklistComponent } from './blacklist/blacklist.component';
import { AnalyzeComponent } from './analyze/analyze.component';
import { HelpComponent } from './help/help.component';
import { ToolbarComponent } from './toolbar/toolbar.component';
import { TocModalComponent } from './toc-modal/toc-modal.component';
import { ExportModalComponent } from './export-modal/export-modal.component';

@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    FooterComponent,
    HomeComponent,
    AsideComponent,
    BlacklistComponent,
    AnalyzeComponent,
    HelpComponent,
    ToolbarComponent,
    TocModalComponent,
    ExportModalComponent
  ],
  imports: [
    BrowserModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    MaterializeModule.forRoot(),
    RouterModule.forRoot([
      { path: 'home', component: HomeComponent },
      { path: 'help', component: HelpComponent },
      { path: 'analyze', component: AnalyzeComponent },
      { path: 'blacklist', component: BlacklistComponent },
      { path: 'sysno/:sysno', component: AnalyzeComponent},
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ])
  ],
  entryComponents: [TocModalComponent, ExportModalComponent],
  providers: [HttpClient, DecimalPipe, AppState, AppService],
  bootstrap: [AppComponent]
})
export class AppModule { }
