'use strict';

/* App Module */

var notebookApp = angular.module('notebookApp', [
  //'ngRoute',
  //'phonecatAnimations',

  'notebookControllers',
  //'phonecatFilters',
  //'phonecatServices'
  'ui.bootstrap'
]);

notebookApp.config([
    "$httpProvider", function($httpProvider) {
      $httpProvider.defaults.headers.common['X-CSRF-Token'] = $('meta[name=csrf-token]').attr('content');
    }
    ]);

//notebookApp.config(['$routeProvider',
//  function($routeProvider) {
//    $routeProvider.
//      when('/phones', {
//        templateUrl: 'partials/phone-list.html',
//        controller: 'PhoneListCtrl'
//      }).
//      when('/phones/:phoneId', {
//        templateUrl: 'partials/phone-detail.html',
//        controller: 'PhoneDetailCtrl'
//      }).
//      otherwise({
//        redirectTo: '/phones'
//      });
//  }]);
