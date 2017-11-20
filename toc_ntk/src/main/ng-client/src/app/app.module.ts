import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms'; 
import { HttpModule } from '@angular/http';
import { RouterModule } from '@angular/router';

import { MaterializeModule } from 'ng2-materialize';

import { AppComponent } from './app.component';
import { AppService } from './app.service';
import { AppState } from './app.state';
import { NavbarComponent } from './navbar/navbar.component';
import { FooterComponent } from './footer/footer.component';
import { HomeComponent } from './home/home.component';
import { AsideComponent } from './aside/aside.component';

@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    FooterComponent,
    HomeComponent,
    AsideComponent
  ],
  imports: [
    BrowserModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    HttpModule,
    MaterializeModule.forRoot(),
    RouterModule.forRoot([
      { path: 'home', component: HomeComponent },
      { path: 'sysno/:sysno', component: HomeComponent},
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ])
  ],
  providers: [AppState, AppService],
  bootstrap: [AppComponent]
})
export class AppModule { }
