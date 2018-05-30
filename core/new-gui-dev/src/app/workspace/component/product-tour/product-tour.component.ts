import { Component, OnInit } from '@angular/core';
import {} from 

@Component({
  selector: 'texera-product-tour',
  templateUrl: './product-tour.component.html',
  styleUrls: ['./product-tour.component.scss']
})
export class ProductTourComponent implements OnInit {

  steps = [
    {
      intro: '<h1>Welcome to Texera!</h1><br><p style="font-size:15px;">Let\'s get familiar with the website first!</p>',
    },
    {
      element: '.texera-operator-view-grid-container',
      intro: '<h1>Operation List</h1><br><p style="font-size:15px;">Here\'s all the operations you can use in Texera.</p>',
      position: 'right'
    },
    {
      element: '.texera-property-editor-grid-container',
      intro: '<h1>Properties</h1><br><p style="font-size:15px;">You can edit the propertyof each operator here.</p>',
      position: 'right'
    },
    {
      element: '.texera-workflow-editor-grid-container',
      intro: '<h1>Workflow editor</h1><br><p style="font-size:15px;">Here\'s where you can edit the workfolw.</p>',
      position: 'right'
    },
    {
      element: '.texera-result-view-grid-container',
      intro: '<h1>Results</h1><br><p style="font-size:15px;">The results will be shown in this box.</p>',
      position: 'right'
    },
    {
      intro: '<h1>Let\s start!</h1><br><p style="font-size:15px;">let\'s go through the whole process of a workflow right now!</p>',
    },
    {
      element: '.texera-operator-view-grid-container',
      intro: 'First, We go to operator lists. Now open the first section named<b>Source</b>',
      position: 'right'
    },
    {
      element: '#mat-expansion-panel-header-0',
      intro: 'Now open this section',
      position: 'right',
    },
    {
      element: '#texera-operator-label-ScanSource',
      intro: 'Drag this and drop to workflow',
      position: 'right'
    },
    // {
    //   element: '',
    //   intro: '',
    //   position: 'right'
    // }
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
      if (this._currentStep === 7) {
        console.log(7);
        document.getElementById('mat-expansion-panel-header-0').click();
      } else if (this._currentStep === 9){
        console.log(9);
        const operatorUIElement = this.operatorUIElementService.getOperatorUIElement(
          'texera-operator-label-ScanSource', 'temporary-dragging-operator');
        this.workflowModelActionService.addOperator(operatorUIElement, 140, 20);
      }
  });

  }

}
