'use strict';

/* App Module */
var GLOBAL_NOTE = null;

var notebookApp = angular.module('notebookApp', [
  'ui.ace',
  'ui.keypress',
  'notebookControllers',
  'ui.bootstrap'
]);

notebookApp.config([
    "$httpProvider", function($httpProvider) {
      $httpProvider.defaults.headers.common['X-CSRF-Token'] = $('meta[name=csrf-token]').attr('content');
    }
    ]);

