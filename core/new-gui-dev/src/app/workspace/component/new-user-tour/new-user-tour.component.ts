import { Component, OnInit } from '@angular/core';
import { NgxBootstrapProductTourService } from '../../../../lib/ngx-bootstrap-product-tour';

@Component({
  selector: 'texera-new-user-tour',
  templateUrl: './new-user-tour.component.html',
  styleUrls: ['./new-user-tour.component.scss']
})

export class NewUserTourComponent implements OnInit {

  public loaded = false;
  constructor(public tourService: NgxBootstrapProductTourService) { 
    this.loaded = false;
    this.tourService.events$.subscribe(console.log);
    this.tourService.initialize([{
      anchorId: 'start.tour',
      content: 'Welcome to the Ngx-Tour tour!',
      placement: 'right',
      title: 'Welcome',
      orphan: true,
      backdrop: true
    }]);
  }


  ngOnInit() {
  }

}
