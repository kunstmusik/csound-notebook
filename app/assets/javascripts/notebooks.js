// Place all the behaviors and hooks related to the matching controller here.
// All this logic will automatically be available in application.js.
// You can use CoffeeScript in this file: http://coffeescript.org/
//


function NotebookController($scope) {

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
}

