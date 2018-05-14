import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'texera-product-tour',
  templateUrl: './product-tour.component.html',
  styleUrls: ['./product-tour.component.scss']
})
export class ProductTourComponent implements OnInit {

  constructor() { }

  ngOnInit() {
  }

  startTour() {
    console.log("Starting tour");
    const IntroJs = require('../../../../../node_modules/intro.js/intro.js');
    let intro = IntroJs.introJs();

    // Initialize steps
    intro.setOptions({
        steps: [
            {
                element: '.texera-navigation-grid-container',
                intro: "Welcome!",
                position: 'bottom'
            }
        ]
    });

    // Start tutorial
    intro.start();
  }

}
