import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {Subscription} from 'rxjs/Subscription';
import {AppState} from '../app.state';
import {AppService} from '../app.service';

@Component({
  selector: 'app-aside',
  templateUrl: './aside.component.html',
  styleUrls: ['./aside.component.css']
})
export class AsideComponent implements OnInit {
  subscriptions: Subscription[] = [];
  //  folders: string[] = [
  //  'F21944z_000661318', 'F22117z_000678390',
  //  'F10107_1z_000152680', 'F21945z_000661333', 'F22118z_000678039',
  //  'F16837_3z_000064987', 'F21946z_000661334', 'F22119z_000678891', 'F21395f_000170834', 'F21947z_000661335', 'F22120z_000679465',
  //  'F21910z_000659739', 'F21948z_000661317', 'F22121z_000679466', 'F21911z_000659731', 'F21951z_000661283', 'F22122z_000679462',
  //  'F21912z_000659738', 'F21952z_000661284', 'F22130z_000681203',
  //  'F21913z_000659737', 'F21953z_000661285', 'F22132z_000681199',
  //  'F21914z_000659736', 'F21954z_000661269', 'F22135z_000682206',
  //  'F21915z_000659733', 'F21955z_000661270', 'F22136z_000682130',
  //  'F21916z_000660050', 'F21956z_000661271', 'F22140-1z_000682126',
  //  'F21917z_000660117', 'F21957z_000661272', 'F22140-2z_000682126', 'F21918_1z_000660233', 'F21958z_000661274', 'F22141z_000659314', 'F21918_2z_000660234', 'F21959z_000660912', 'F22142z_000659315', 'F21919z_000660235', 'F21960z_000661751', 'F22143z_000682125', 'F21920z_000660236', 'F21961z_000661754', 'F22144z_000682133', 'F21922z_000660508', 'F21962z_000661755', 'F22146z_000682481', 'F21923z_000660504', 'F21963z_000661778', 'F22147z_000682483', 'F21924_2z_000660506', 'F21965z_000661781', 'F22148z_000682482', 'F21925z_000660507', 'F21966z_000661783', 'F22149z_000682484', 'F21927z_000660783', 'F21967z_000661837', 'F22170z_000684740', 'F21928z_000660785', 'F21968z_000661847', 'F22171z_000684743', 'F21929z_000660952', 'F21970z_000661872', 'F22175z_000685144', 'F21931z_000660959', 'F21971z_000662018', 'F22176z_000685151', 'F21932_1z_000660955', 'F21973z_000662023', 'F22177z_000685154', 'F21932_2z_000660956', 'F21975z_000662019', 'F22178z_000685156', 'F21932_3z_000660958', 'F21976z_000662021', 'F22179z_000685157', 'F21933z_000660960', 'F21977z_000662017', 'F22180z_000685159', 'F21934_1z_000660964', 'F21978z_000662016', 'F22214z_000947858', 'F21934_2z_000660969', 'F21979z_000662015', 'F22215z_000947860', 'F21934_3z_000660970', 'F22005z_000664168', 'F22216z_000947916',
  //  'F21935z_000660965', 'F22100z_000675629', 'F22224z_000948535', 'F21936z_000660967', 'F22107z_000678226', 'F22240z_000952734', 'F21937z_000660971', 'F22109z_000678031', 'F22242z_000953276', 'F21938z_000660972', 'F22110z_000678036', 'F22243z_000953275', 'F21939z_000660973', 'F22111z_000678040', 'F21940z_000660756', 'F22112z_000678038', 'F21941z_000660913', 'F22114z_000678391', 'F21943z_000661373', 'F22115z_000678388'
  //  ];
  sysnos: string[] = [];
  sysno: string;
  constructor(
    private router: Router,
    public state: AppState,
    private service: AppService) {

  }

  ngOnDestroy() {
    this.subscriptions.forEach((s: Subscription) => {
      s.unsubscribe();
    });
    this.subscriptions = [];
  }

  ngOnInit() {
    if (this.state.config) {
      this.setFolders();
    } else {
      this.subscriptions.push(this.state.configSubject.subscribe(st => {
        this.setFolders();
      }));
    }


    this.subscriptions.push(this.state.stateChanged.subscribe(st => {
      this.sysno = this.state.sysno;
    }));

  }

  updateFolders() {
    this.setFolders(true);
  }

  setFolders(update: boolean = false) {

    this.sysnos = [];

    this.service.getBalicky(update).subscribe(res => {
      this.sysno = this.state.sysno;
      this.state.balicky = res;
      let fs = Object.keys(res);
      fs.sort((a, b) => {
        return a.localeCompare(b);
      });
      this.sysnos = fs;

    });
  }

  setSysno(sysno: string) {
    this.router.navigate(['/sysno', sysno]);
    this.state.setSysno(sysno, 'aside');
  }

}
