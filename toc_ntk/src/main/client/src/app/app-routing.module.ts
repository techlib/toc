import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AnalyzeComponent } from './components/analyze/analyze.component';
import { BlackListComponent } from './components/black-list/black-list.component';
import { HelpComponent } from './components/help/help.component';
import { HomeComponent } from './components/home/home.component';

const routes: Routes = [
    { path: 'home', component: HomeComponent },
    { path: 'help', component: HelpComponent },
    { path: 'analyze', component: AnalyzeComponent },
    { path: 'blacklist', component: BlackListComponent },
    { path: 'sysno/:sysno', component: AnalyzeComponent},
    { path: '', redirectTo: 'home', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
