import { Component, Inject, Input, Output, EventEmitter } from '@angular/core';
import {NgbModal, NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {MOCK_DICT_DATA, MOCK_DICT_DATA_LIST} from '../current-service/mock-dictionary-data';
import {MatDialog, MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import { Http } from '@angular/http';

@Component({
  selector: 'resource-section',
  templateUrl:'./resource.section.html',
  styleUrls: ['../style.scss']
})
export class ResourceSection {
  
  dictionaries = MOCK_DICT_DATA;

  distionaryList = MOCK_DICT_DATA_LIST;

  currentDict = '';

  constructor(public dialog: MatDialog, private modalService: NgbModal) {}

  openDialog(dict: any) {
    this.dialog.open(DictViewDialog, {
        width: '600px',
        height:'200px',
        data: {
          dict_name: dict,
          dict: this.dictionaries[dict] },
    }
    );
  }//use of angular material 

  open(dict: any) {
    const modalRef = this.modalService.open(NgbdModalResourceView);
    modalRef.componentInstance.dict = this.dictionaries[dict];
    modalRef.componentInstance.dictkey = dict;
  }//use of ng bootstrap

 }

 @Component({
  selector: 'resource-section-modal',
  templateUrl:'./resource.section.view.html',
})
export class NgbdModalResourceView {
  @Input() dict;
  @Input() dictkey;
  @Output() addedName =  new EventEmitter<any>();
  
  name: string;
  ifAdd = false;

  

  constructor(public activeModal: NgbActiveModal) {}

  onNoClick(): void {
    this.activeModal.close();
  }
  onClose() {
    this.activeModal.close('Close');
  }
  addKey() {

    if (this.ifAdd && this.name !== undefined) {
      console.log('add ' + this.name + ' into dict ' + this.dictkey);
      this.dict.push(this.name);
      this.addedName.emit(this.dict);
      this.name = undefined;
    }
    this.ifAdd = !this.ifAdd;

  }
}



 @Component({
  selector: 'resource-section-dialog',
  template:`
  
  <div fxLayout = "column" fxLayoutWrap>
  <mat-dialog-content>
    <mat-list fxFlex="80%">
      
        <p mat-line style="font-family:Roboto, Arial, sans-serif;color:gray" >
          {{dictKey}}&emsp;&emsp;&emsp;&emsp;&emsp;{{this.data.dict}}
        </p>

    </mat-list>
  </mat-dialog-content>
  <mat-dialog-actions>
    <input *ngIf="ifAdd" matInput [(ngModel)]="name" placeholder="Add into dictionary">
    <button mat-raise-button (click)="addKey()" fxFlexOffset = "3%" >Add</button>
    <button mat-raise-button (click)="onClose()" fxFlexOffset = "3%" >Close</button>
  </mat-dialog-actions>
  </div>
  `,
  styleUrls: ['../style.scss']
})
export class DictViewDialog {
  constructor(public dialogRef: MatDialogRef<DictViewDialog>,
    @Inject(MAT_DIALOG_DATA) public data: any) {}

    name: string;
    ifAdd = false;
    dictKey = this.data.dict_name;


    onNoClick(): void {
      this.dialogRef.close();
    }
    onClose() {
      this.dialogRef.close('Close');
    }

    addKey() {
      if (this.ifAdd && this.name !== undefined) {
        console.log('add ' + this.name + ' into dict ' + this.dictKey);
        this.data.dict.push(this.name);
        this.name = undefined;
      }
      this.ifAdd = !this.ifAdd;

    }
}