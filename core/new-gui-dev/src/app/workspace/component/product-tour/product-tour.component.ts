import { Component, OnInit } from '@angular/core';

import { OperatorMetadataService } from '../../service/operator-metadata/operator-metadata.service';

import { FormControl } from '@angular/forms';
import { Observable } from 'rxjs/Observable';
import { startWith } from 'rxjs/operators/startWith';
import { map } from 'rxjs/operators/map';

import { WorkflowModelActionService } from '../../service/workflow-graph/model-action/workflow-model-action.service';
import { WorkflowGraphUtilsService } from '../../service/workflow-graph/utils/workflow-graph-utils.service';
import { WorkflowViewEventService } from '../../service/workflow-graph/view-event/workflow-view-event.service';
import { SmartOperatorLocation } from '../operator-view/smart-operator-location';
import { WorkflowTexeraGraphService } from '../../service/workflow-graph/model/workflow-texera-graph.service';
import { WorkflowJointGraphService } from '../../service/workflow-graph/model/workflow-joint-graph.service';

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
      intro: '<h1>Properties</h1><br><p style="font-size:15px;">You can edit the property of each operator here.</p>',
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
    {
      element: '.texera-workflow-editor-grid-container',
      intro: 'Click on this operator and see the next step',
      position: 'right'
    }
  ];
  intro = require('../../../../../node_modules/intro.js/intro.js').introJs();

  inTour = false;

  private smartOperatorLocation: SmartOperatorLocation;

  constructor(
    private operatorMetadataService: OperatorMetadataService,
    private workflowModelActionService: WorkflowModelActionService,
    private workflowGraphUtilsService: WorkflowGraphUtilsService,
    private workflowViewEventService: WorkflowViewEventService,
    private workflowTexeraGraphService: WorkflowTexeraGraphService,
    private workflowJointGraphService: WorkflowJointGraphService
  ) {
    this.smartOperatorLocation = new SmartOperatorLocation(this.workflowTexeraGraphService, this.workflowJointGraphService);
  }

  ngOnInit() {
  }

  private createScanSourceOperator(): void {
    //if (introJsObject._currentStep === 7){
    //  console.log(7);
    //    document.getElementById('mat-expansion-panel-header-0').click();
    //} else if (introJsObject._currentStep === 9) {
      const operatorUIElement = this.workflowGraphUtilsService.getNewOperatorPredicate('ScanSource');
      const smartLocation = this.smartOperatorLocation.suggestNextLocation('ScanSource');
      this.workflowModelActionService.addOperator(
            operatorUIElement, smartLocation.x, smartLocation.y);
      this.workflowViewEventService.operatorSelectedInEditor.next({operatorID: operatorUIElement.operatorID});
    //}
  };

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
    let self = this;
    this.intro.onbeforechange(function() 
    {
      if (this._currentStep === 7) {
        console.log(7);
        document.getElementById('mat-expansion-panel-header-0').click();
      } else if (this._currentStep === 9) {
        console.log(9);
        self.createScanSourceOperator();
      }
  }
);

  }

}
