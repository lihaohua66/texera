import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'texera-product-tour',
  templateUrl: './product-tour.component.html',
  styleUrls: ['./product-tour.component.scss']
})
export class ProductTourComponent implements OnInit {

  steps = [
    {
      intro: '<h1>Welcome to Texera!</h1><br><p style="font-size:15px;">We will go through the whole process of a workflow</p>',
    },
    {
      element: '.texera-operator-view-grid-container',
      intro:'This is the operators that you can use. Now open the first section named <b>Source</b>',
      position: 'right'
    },
    {
      element: '#mat-expansion-panel-header-0',
      intro:'Now open this section',
      position:'right',
    },
    {
      element: '#texera-operator-label-ScanSource',
      intro:'Drag this and drop to workflow',
      position:'right'
    }
  ];
  intro = require('../../../../../node_modules/intro.js/intro.js').introJs();

  inTour = false;


  constructor() {
  }

  ngOnInit() {
  }

  

  startTour() {
    // Start tutorial
    this.intro.setOptions({
      steps: this.steps,
      showStepNumbers: true,
    });
    this.intro.start();
    this.inTour = true;
    this.intro.onexit(function() {
      this.inTour = false;
    });
    this.intro.onbeforechange(function() {
      if(this._currentStep === 3) {
        console.log(3);
        document.getElementById('mat-expansion-panel-header-0').click();
      }
  });

  }

}
