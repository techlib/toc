import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';

@Injectable()
export class AppState {
  

  private _stateSubject = new Subject();
  public stateChanged: Observable<any> = this._stateSubject.asObservable();
  
  foldername: string;
  
  setFoldername(f: string){
    this.foldername = f;
    this._stateSubject.next(this);
  }
}
