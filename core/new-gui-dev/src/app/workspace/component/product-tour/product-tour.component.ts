import { Component, EventEmitter, OnInit, Output } from '@angular/core';

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
    // 1
    {
      intro: '<h1>Welcome to Texera!</h1><br><p style="font-size:15px;">Let\'s get familiar with the website first!</p>',
    },
    // 2
    {
      element: '.texera-operator-view-grid-container',
      intro: '<h1>Operation List</h1><br><p style="font-size:15px;">Here\'s all the operations you can use in Texera.</p>',
      position: 'right'
    },
    // 3
    {
      element: '.texera-property-editor-grid-container',
      intro: '<h1>Properties</h1><br><p style="font-size:15px;">You can edit the property of each operator here.</p>',
      position: 'right'
    },
    // 4
    {
      element: '.texera-workflow-editor-grid-container',
      intro: '<h1>Workflow editor</h1><br><p style="font-size:15px;">Here\'s where you can edit the workfolw.</p>',
      position: 'right'
    },
    // 5
    {
      element: '.texera-result-view-grid-container',
      intro: '<h1>Results</h1><br><p style="font-size:15px;">The results will be shown in this box.</p>',
      position: 'right'
    },
    // 6
    {
      intro: '<h1>Let\s start!</h1><br><p style="font-size:15px;">let\'s go through the whole process of a workflow right now!</p>',
    },
    // 7
    {
      element: '.texera-operator-view-grid-container',
      intro: 'First, We go to operator lists. Now open the first section named <b>Source</b>',
      position: 'right'
    },
    // 8
    {
      element: '#mat-expansion-panel-header-0',
      intro: 'Now open this section',
      position: 'right',
    },
    // 9
    {
      element: '#texera-operator-label-ScanSource',
      intro: 'Drag this and drop to workflow',
      position: 'right'
    },
    // 10
    {
      element: '.texera-workflow-editor-grid-container',
      intro: 'You can see the operator on the workflow editor. <br> Next, let\'s choose the source to use',
      position: 'right'
    },
    // 11
    {
      element: '.texera-property-editor-grid-container',
      intro: 'Type in database <b>twitter_sample</b>',
      position: 'right'
    },
    // 12
    {
      element: '.texera-operator-view-grid-container',
      intro: 'Now, back to the operator view.<br> We are going to use <b>View Result</b> operator.',
      position: 'right'
    },
    // 13
    {
      element: '#texera-operator-label-ViewResult',
      intro: 'Now, back to the operator view.<br> We are going to use <b>View Result</b> operator.',
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
    private workflowJointGraphService: WorkflowJointGraphService,
  ) {
    this.smartOperatorLocation = new SmartOperatorLocation(this.workflowTexeraGraphService, this.workflowJointGraphService);
  }

  ngOnInit() {
  }

  private createScanSourceOperator(OperatorType: string): void {
      const operatorUIElement = this.workflowGraphUtilsService.getNewOperatorPredicate(OperatorType);
      const smartLocation = this.smartOperatorLocation.suggestNextLocation(OperatorType);
      this.workflowModelActionService.addOperator(
            operatorUIElement, smartLocation.x, smartLocation.y);
      this.workflowViewEventService.operatorSelectedInEditor.next({operatorID: operatorUIElement.operatorID});
  }


  //@Output() eventEmitterChange = new EventEmitter();
  
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
    const self = this;
    this.intro.onbeforechange(function() {
      if (this._currentStep === 7) {
        console.log(7);
        document.getElementById('mat-expansion-panel-header-0').click();
      } else if (this._currentStep === 9) {
        console.log(9);
        if (! self.workflowTexeraGraphService.texeraWorkflowGraph.hasOperator('operator-1')) {
          self.createScanSourceOperator('ScanSource');
        }
      } else if (this._currentStep === 11) {
        console.log(11);
        const event = {tableName: 'twitter_sample'};
        //self.eventEmitterChange.emit(event);
        //self.workflowModelActionService.changeOperatorProperty('operator-1', event);
        //self.workflowViewEventService.operatorSelectedInEditor.next({operatorID: 'operator-1'});
      } else if (this._currentStep === 12) {
        console.log(12);
        document.getElementById('mat-expansion-panel-header-0').click();
        document.getElementById('mat-expansion-panel-header-7').click();
      }
  }
);

  }

}
