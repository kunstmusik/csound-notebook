'use strict';

/* Services */

var notebookServices = angular.module('notebookServices', ['ngResource']);

notebookServices.factory('Phone', ['$resource',
  function($resource){
    return $resource('phones/:phoneId.json', {}, {
      query: {method:'GET', params:{phoneId:'phones'}, isArray:true}
    });
  }]);
