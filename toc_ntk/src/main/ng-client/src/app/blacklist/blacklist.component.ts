import {Component, OnInit} from '@angular/core';
import {Subscription} from 'rxjs/Subscription';
import {AppState} from '../app.state';
import {AppService} from '../app.service';

@Component({
    selector: 'app-blacklist',
    templateUrl: './blacklist.component.html',
    styleUrls: ['./blacklist.component.scss']
})
export class BlacklistComponent implements OnInit {

    subscriptions: Subscription[] = [];
    public words: any[];
    constructor(
        public state: AppState,
        public service: AppService) {
    }

    ngOnDestroy() {
        this.subscriptions.forEach((s: Subscription) => {
            s.unsubscribe();
        });
        this.subscriptions = [];
    }

    ngOnInit() {
        this.getWords();
    }
    getWords() {
        this.words = [];
        this.service.getBlacklist().subscribe(res => {
            this.words = res['response']['docs'];
        });
    }

    removeFromBlackList(key: string) {
        this.service.removeFromBlackList(key).subscribe(res => {
            this.state.removedFromBlacklist.push(key);
            this.getWords();
        });
    }

    restore(key: string) {
        this.service.addToBlackList(key).subscribe(res => {
            this.getWords();
            let idx = this.state.removedFromBlacklist.indexOf(key);
            this.state.removedFromBlacklist.splice(idx, 1);
        });
    }

}
