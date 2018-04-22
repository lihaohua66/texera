import { Component } from '@angular/core';

import { WorkspaceComponent } from './workspace/component/workspace.component';

@Component({
  selector: 'texera-root',
  template: `
    <ngx-bootstrap-product-tour>
    </ngx-bootstrap-product-tour>
    <texera-workspace></texera-workspace>
  `,
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'texera';
}
