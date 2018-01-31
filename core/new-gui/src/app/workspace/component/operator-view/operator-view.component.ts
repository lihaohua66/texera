import { Component, OnInit, EventEmitter, Output } from '@angular/core';

import { OperatorSchema } from '../../model/operator-schema';
import { OperatorMetadataService } from '../../service/operator-metadata/operator-metadata.service';

import { OperatorLabelComponent } from './operator-label/operator-label.component';

@Component({
  selector: 'texera-operator-view',
  templateUrl: './operator-view.component.html',
  styleUrls: ['./operator-view.component.scss']
})
export class OperatorViewComponent implements OnInit {

  public operatorMetadataList: OperatorSchema[] = [];
  constructor(private operatorMetadataService: OperatorMetadataService) {
    operatorMetadataService.metadataChanged$.subscribe(x => this.operatorMetadataList = x);
  }

  ngOnInit() {
    this.operatorMetadataList  = this.operatorMetadataService.getOperatorMetadataList();
    (function () {
      'use strict';
    
      function DemoCtrl ($timeout, $q, $log) {
        var self = this;
    
        self.simulateQuery = false;
        self.isDisabled    = false;
    
        // list of `state` value/display objects
        self.states        = loadAll();
        self.querySearch   = querySearch;
        self.selectedItemChange = selectedItemChange;
        self.searchTextChange   = searchTextChange;
    
        self.newState = newState;
    
        function newState(state) {
          alert("Sorry! You'll need to create a Constitution for " + state + " first!");
        }
    
        // ******************************
        // Internal methods
        // ******************************
    
        /**
         * Search for states... use $timeout to simulate
         * remote dataservice call.
         */
        function querySearch (query) {
          var results = query ? self.states.filter( createFilterFor(query) ) : self.states,
              deferred;
          if (self.simulateQuery) {
            deferred = $q.defer();
            $timeout(function () { deferred.resolve( results ); }, Math.random() * 1000, false);
            return deferred.promise;
          } else {
            return results;
          }
        }
    
        function searchTextChange(text) {
          $log.info('Text changed to ' + text);
        }
    
        function selectedItemChange(item) {
          $log.info('Item changed to ' + JSON.stringify(item));
        }
    
        /**
         * Build `states` list of key/value pairs
         */
        function loadAll() {
          var allStates = 'Alabama, Alaska, Arizona, Arkansas, California, Colorado, Connecticut, Delaware,\
                  Florida, Georgia, Hawaii, Idaho, Illinois, Indiana, Iowa, Kansas, Kentucky, Louisiana,\
                  Maine, Maryland, Massachusetts, Michigan, Minnesota, Mississippi, Missouri, Montana,\
                  Nebraska, Nevada, New Hampshire, New Jersey, New Mexico, New York, North Carolina,\
                  North Dakota, Ohio, Oklahoma, Oregon, Pennsylvania, Rhode Island, South Carolina,\
                  South Dakota, Tennessee, Texas, Utah, Vermont, Virginia, Washington, West Virginia,\
                  Wisconsin, Wyoming';
    
          return allStates.split(/, +/g).map( function (state) {
            return {
              value: state.toLowerCase(),
              display: state
            };
          });
        }
    
        /**
         * Create filter function for a query string
         */
        function createFilterFor(query) {
          var lowercaseQuery = query.toLowerCase();
    
          return function filterFn(state) {
            return (state.value.indexOf(lowercaseQuery) === 0);
          };
    
        }
      }
    })();
    
  }
}

