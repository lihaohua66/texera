import { OperatorPredicate } from './../../../types/workflow-graph';
import { WorkflowModelActionService } from './workflow-model-action.service';
import { Injectable } from '@angular/core';
import { Point } from '../../../types/common.interface';
import { Observable } from 'rxjs/Observable';

import '../../../../common/rxjs-operators';

import * as joint from 'jointjs';
import { JointUIService } from '../../joint-ui/joint-ui.service';

@Injectable()
export class JointjsModelService {

  public jointGraph = new joint.dia.Graph();
  public jointPaper: joint.dia.Paper = null;

  private jointCellAddStream = Observable
    .fromEvent(this.jointGraph, 'add')
    .map(value => <joint.dia.Cell>value);

  private jointCellDeleteStream = Observable
    .fromEvent(this.jointGraph, 'remove')
    .map(value => <joint.dia.Cell>value);



  constructor(
    private workflowModelActionService: WorkflowModelActionService,
    private jointUIService: JointUIService) {

    this.workflowModelActionService._onAddOperatorAction().subscribe(
      value => this.addOperator(value.operator, value.point)
    );

    this.workflowModelActionService._onDeleteOperatorAction().subscribe(
      value => this.deleteOperator(value.operatorID)
    );
  }

  public registerJointPaper(jointPaper: joint.dia.Paper): void {
    this.jointPaper = jointPaper;
  }

  /**
   * @package
   */
  _onJointOperatorDelete(): Observable<joint.dia.Element> {
    const jointOperatorDeleteStream = this.jointCellAddStream
      .filter(cell => cell.isElement())
      .map(cell => <joint.dia.Element>cell);
    return jointOperatorDeleteStream;
  }

  /**
   * @package
   */
  _onJointLinkAdd(): Observable<joint.dia.Link> {
    const jointLinkAddStream = this.jointCellAddStream
      .filter(cell => cell.isLink())
      .map(cell => <joint.dia.Link>cell);

    return jointLinkAddStream;
  }

  /**
   * @package
   */
  _onJointLinkDelete(): Observable<joint.dia.Link> {
    const jointLinkDeleteStream = this.jointCellDeleteStream
      .filter(cell => cell.isLink())
      .map(cell => <joint.dia.Link>cell);

    return jointLinkDeleteStream;
  }

  /**
   * @package
   */
  _onJointLinkChange(): Observable<joint.dia.Link> {
    const jointLinkChangeStream = Observable
      .fromEvent(this.jointGraph, 'change:source change:target')
      .map(value => <joint.dia.Link>value);

    return jointLinkChangeStream;
  }


  private addOperator(operator: OperatorPredicate, point: Point): void {
    const jointOffsetPoint: Point = {
      x: point.x - this.jointPaper.pageOffset().x,
      y: point.y - this.jointPaper.pageOffset().y
    };

    const operatorJointElement = this.jointUIService.getJointjsOperatorElement(
      operator.operatorType, operator.operatorID, jointOffsetPoint);

    this.jointGraph.addCell(operatorJointElement);
  }

  private deleteOperator(operatorID: string): void {
    this.jointGraph.getCell(operatorID).remove();
  }

}
