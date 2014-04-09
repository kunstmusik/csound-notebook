'use strict';

/* Controllers */

var notebookControllers = angular.module('notebookControllers', []);

notebookControllers.controller('NotebooksController', ['$scope',
  function($scope) {
    $scope.notebooks = [
        {
            id: 1,
            name:'My Notebook',
            seen:true
        },
        {
          id: 2,
          name:'Inbox',
          seen:false
        }
      ];
  }]);


notebookControllers.controller('NotesController', ['$scope',
  function($scope) {
    $scope.note = {
      orc: "sr=44100\nksmps=32\nnchnls=2\n0dbfs=1\n\ninstr 1\nipch = cps2pch(p4,12)\naout vco2 .5, ipch\naout moogladder aout, 2000, .3\nouts aout, aout\nendin",
  
      sco: "i1 0 1 8.00\ni1 0 1 8.04\ni1 0 1 8.07",
      title: "My Note"
  };
  }]);

//notebookControllers.controller('PhoneDetailCtrl', ['$scope', '$routeParams', 'Phone',
//  function($scope, $routeParams, Phone) {
//    $scope.phone = Phone.get({phoneId: $routeParams.phoneId}, function(phone) {
//      $scope.mainImageUrl = phone.images[0];
//    });

//    $scope.setImage = function(imageUrl) {
//      $scope.mainImageUrl = imageUrl;
//    }
//  }]);



