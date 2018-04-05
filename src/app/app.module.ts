import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';

import {MatMenuModule} from '@angular/material/menu';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatListModule} from '@angular/material/list';
import {MatIconModule} from '@angular/material/icon';
import {MatTableModule} from '@angular/material/table';
import {MatDialogModule} from '@angular/material/dialog';
import {MatInputModule} from '@angular/material';

import { FlexLayoutModule } from '@angular/flex-layout';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';

import { AppComponent } from './app.component';
import { CurrentService } from './current-service/current.service';
import { SelectBarComponent } from './selection-bar/select.bar.component';
/*the sub components of the selectbar*/
import { UserAccount } from './user-account/user.account.component';
import { TopBarComponent } from './top-bar/top.bar';
import { SavedProjectSection, DictAddDialog, NgbdModalAddProject} from './saved-project-section/saved.project.section';
import { RunningJobSection } from './running-jobs-section/running.job.section';
import { ResourceSection, DictViewDialog, NgbdModalResourceView} from './resource-section/resource.section';
import { DataSourceSection } from './data-source-section/data.source.section';
import { routing } from './app-routing.model';

@NgModule({
  declarations: [
    AppComponent,
    CurrentService,
    SelectBarComponent,
    UserAccount,
    TopBarComponent,
    RunningJobSection,
    SavedProjectSection,
    ResourceSection,
    DictViewDialog,
    DictAddDialog,
    NgbdModalResourceView,
    NgbdModalAddProject,
    DataSourceSection
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    routing,
    BrowserAnimationsModule,
    FlexLayoutModule,
    NgbModule.forRoot(),
    MatMenuModule,
    MatCardModule,
    MatListModule,
    MatIconModule,
    MatTableModule,
    MatButtonModule,
    MatDialogModule,
    MatInputModule
  ],
  entryComponents: [
    DictViewDialog,
    NgbdModalResourceView,
    DictAddDialog,
    NgbdModalAddProject
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
