import { Component, Inject, Input, Output, EventEmitter  } from '@angular/core';
import {NgbModal, NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';

import {MOCK_PROJECT_DATA} from '../current-service/mock-project-data';
import {MatDialog, MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import { Http } from '@angular/http';

@Component({
  selector: 'saved-project-section',
  templateUrl:'./saved.project.section.html',
  styleUrls: ['../style.scss', 'saved.project.section.scss']
})
export class SavedProjectSection {

  defaultWeb = "http://texera.ics.uci.edu/twitter/";

  projects = MOCK_PROJECT_DATA;

  constructor(public dialog: MatDialog, private modalService: NgbModal) {}

  ascSort(): void {

    this.projects.sort((t1:any, t2:any) => {
      if (t1.name > t2.name) {
          return 1;
      }
  
      if (t1.name < t2.name) {
          return -1;
      }
      return 0;})
  }

  dscSort(): void {
    this.projects.sort((t1:any, t2:any) => {
      if (t1.name > t2.name) {
          return -1;
      }
  
      if (t1.name < t2.name) {
          return 1;
      }
      return 0;})
  }

  dateSort(): void {
    this.projects.sort((t1:any, t2:any) => {
      if (t1.creation_time > t2.creation_time) {
          return -1;
      }
  
      if (t1.creation_time < t2.creation_time) {
          return 1;
      }
      return 0;})

  }

  lastSort(): void{

    this.projects.sort((t1:any, t2:any) => {
        if (t1.last_modified_time > t2.last_modified_time) {
            return -1;
        }
    
        if (t1.last_modified_time < t2.last_modified_time) {
            return 1;
        }
        return 0;})
  }

  openDialog() {
    this.dialog.open(DictAddDialog, {
        width: '600px',
        height:'200px',
        data: {
          pj: this.projects },
    }
    );
  }//use of angular material

  open() {
    const modalRef = this.modalService.open(NgbdModalAddProject);
    modalRef.componentInstance.pj = this.projects;
  }//use of ng bootstrap

 }

 
 @Component({
    selector: 'project-section-modal',
    templateUrl:'./saved.project.section.add.html'
  })
  export class NgbdModalAddProject{
    @Input() pj;
    @Output() newProject =  new EventEmitter<any>();
    
    name: string;
    pjs = this.pj;
    new_pj: projectf;
  
    
  
    constructor(public activeModal: NgbActiveModal) {}
  
    onNoClick(): void {
      this.activeModal.close();
    }
    onClose() {
      this.activeModal.close('Close');
    }
    addProject() {
        if (this.name !== undefined) {
            this.new_pj = {  id: (this.pj[-1] + 1),
                        name: this.name,
                        creation_time: Date.now(),
                        last_modified_time: Date.now(),
            };
            this.pj.push(this.new_pj);
            this.newProject.emit(this.pj);
            this.name = undefined;
          }
        this.onClose();
      }
  }





 @Component({
    selector: 'project-section-dialog',
    template:`
    <div fxLayout = "column" fxLayoutWrap>
        <mat-dialog-content>
            <input matInput [(ngModel)]="name" placeholder="Name of New Project">
        </mat-dialog-content>
        <mat-dialog-actions>
            <button mat-raise-button (click)="addProject()" fxFlexOffset = "3%" >Add</button>
            <button mat-raise-button (click)="onClose()" fxFlexOffset = "3%" >Close</button>
        </mat-dialog-actions>
    </div>
    `,
    styleUrls: ['../style.scss']
  })
  export class DictAddDialog {
    constructor(public dialogRef: MatDialogRef<DictAddDialog>,
      @Inject(MAT_DIALOG_DATA) public data: any) {}
  
      name: string;
      pjs = this.data.pj;
      new_pj: projectf;
  
      onNoClick(): void {
        this.dialogRef.close();
      }
      onClose() {
        this.dialogRef.close('Close');
      }
  
      addProject() {
        if (this.name !== undefined) {
            this.new_pj = {  id: (this.data.pj[-1] + 1),
                        name: this.name,
                        creation_time: Date.now(),
                        last_modified_time: Date.now(),
            };
            this.data.pj.push(this.new_pj);
            this.name = undefined;
          }
        this.dialogRef.close('Close');
      }
  }

interface projectf{
    id: number;
    name: string;
    creation_time: number;
    last_modified_time: number;
}