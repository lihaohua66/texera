import { Component } from '@angular/core';

import { WorkspaceComponent } from './workspace/component/workspace.component';

import { TourService } from 'ngx-tour-ngx-popper';

@Component({
  selector: 'texera-root',
  template: `
    <texera-workspace></texera-workspace>
    <router-outlet></router-outlet>
    <tour-step-template></tour-step-template>
  `,
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'texera';
}
