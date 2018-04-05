import { NgModule, ModuleWithProviders } from '@angular/core';
import { RouterModule, Routes, ActivatedRoute } from '@angular/router';

import { SavedProjectSection} from './saved-project-section/saved.project.section';
import { RunningJobSection } from './running-jobs-section/running.job.section';
import { ResourceSection} from './resource-section/resource.section';
import { DataSourceSection } from './data-source-section/data.source.section';

//constructor(private route: ActivatedRoute){
  //this.route.params
    //.subscribe(params => console.log(params));
//}

const appRoutes: Routes = [
  {
    path : 'savedProjects',
    component : SavedProjectSection
  },
  {
    path : 'savedProjects/:user',
    component : SavedProjectSection
  },
  {
    path : 'runningJobs',
    component : RunningJobSection
  },
  {
    path : 'runningJobs/:user',
    component : RunningJobSection
  },
  {
    path : 'resource',
    component : ResourceSection
  },
  {
    path : 'resource/:user',
    component : ResourceSection
  },
  {
    path : 'dataSource',
    component : DataSourceSection
  },
  {
    path : 'dataSource/:user',
    component : DataSourceSection
  }
];

export const routing: ModuleWithProviders = RouterModule.forRoot(appRoutes);