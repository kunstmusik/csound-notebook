'use strict';

/* Controllers */

var notebookControllers = angular.module('notebookControllers', []);

var templateNote = {
      id: 100,
      orc: "sr=44100\nksmps=32\nnchnls=2\n0dbfs=1\n\ninstr 1\nipch = cps2pch(p4,12)\niamp = ampdbfs(p5)\naenv linsegr 0, 0.01, 1, 0.01, .9, .3, 0\naout vco2 iamp, ipch\naout = aout * aenv\naout moogladder aout, 2000, .3\nouts aout, aout\nendin",
  
      sco: "i1 0 1 8.00 -12\ni1 0 1 8.04 -12\ni1 0 1 8.07 -12",
      title: "My Note",
      public: false,
      livesco: false
};



/* NOTEBOOK CONTROLLER */

var allNotesNotebook = { id: 0, name: 'All Notes' };

notebookControllers.controller('NotebooksController', ['$scope','$http', 
  function($scope, $http) {
    $scope.note = null;
    $scope.notes = [];
    $scope.notebooks = notebooks;

    var notebooks = [ allNotesNotebook ];

    $scope.publicURL = function() {
      var l = document.location;
      return l.protocol + "//" + l.host + "/note/" + $scope.note.id;
    }

    $scope.orcTextLoaded = function(_editor){
      $scope.orcTextEditor = _editor;
    };

    $scope.scoTextLoaded = function(_editor) {
      $scope.scoTextEditor = _editor;
    }

    $scope.reloadNotebooks = function() {
      $http.get('/notebooks.json?callback=JSON_CALLBACK').success(function(data, status, headers, config) {
        $scope.notebooks = [ allNotesNotebook].concat(data);
      }).error(function(data, status, headers, config) {
        alert("Oh no!  Couldn't load my notebooks... :( ");
      });
    }; 

    $scope.reloadNotebooks();

    $scope.addNotebook = function() {
      var name = prompt("Please enter the name of your notebook.", "My Notebook");
      if(name != null) {
        $http.post('/notebooks.json', { name: name })
          .success(function(data, status, headers, config) {
            console.log(data);
            $scope.notebooks = $scope.notebooks.concat(data);
            console.log($scope.notebooks);
          }).error(function(data, status, headers, config) {
            alert("Error: Unable to add notebook. Please try again later. ");
          });
      }
    };

    $scope.deleteNotebook = function(notebook) {

      if(notebook.id <= 0) { return };

      if(confirm("Please confirm deleting notebook \"" + notebook.name + "\".")) {
        console.log(notebook);
        
        $http.delete('/notebooks/' + notebook.id + '.json')
          .success(function(data, status, headers, config) {
            $scope.reloadNotebooks();

            if($scope.currentNotebook.id == notebook.id) {
              $scope.selectNotebook(allNotesNotebook);
            }
            //note.id = note.id; // update note.id
        }).error(function(data, status, headers, config) {
           alert("Oh no!  Couldn't delete my notebook... :( ");
        });
      }
    }

    $scope.editNoteBookTitle = function() {
      alert("editing notebook title not yet implemented");
    }

    $scope.getNotesForNotebook = function(notebookId) {
      this.notes = [];
    }

    $scope.reloadNotebook = function(notebook) {
      $scope.note = null;
      $http.get('/notes.json?callback=JSON_CALLBACK&notebook_id=' + notebook.id )
        .success(function(data, status, headers, config) {
          $scope.notes = data;
        }).error(function(data, status, headers, config) {
          alert("Oh no!  Couldn't load my notebooks... :( ");
        });
    }

    $scope.selectNotebook = function(notebook) {
      if($scope.currentNotebook == notebook) {
        return;
      }
      $scope.currentNotebook = notebook;
      $scope.reloadNotebook(notebook);
    };

    $scope.selectNotebook(allNotesNotebook);

    $scope.getNotebookClass = function(notebook) { 
      if(this.currentNotebook == notebook) {
        return "active";
      }
      return "";
    }

    $scope.newNote = function(notebookId) {
      var note = {};
      note.id = 0;
      note.notebook_id = notebookId;
      note.orc = templateNote.orc;
      note.sco = templateNote.sco;
      note.title = templateNote.title;
      note.saved = false;
      note.public = false;
      note.livesco = false;
      $scope.note = note;
      $scope.notes = $scope.notes.concat(note);
    }

    $scope.selectNote = function(note) {
      $scope.note = note;
    }

    $scope.getCSD = function() {
      var csd = "<CsoundSynthesizer>\n<CsInstruments>\n"
      csd += $scope.orcTextEditor.getValue();
      csd += "\n</CsInstruments>\n<CsScore>\n"
      if(!$scope.note.livesco) {
        csd += $scope.scoTextEditor.getValue();
      }
      csd += "\n</CsScore>\n</CsoundSynthesizer>\n"

      return csd;
    }

    $scope.exportCSD = function(note) {
      var csd = $scope.getCSD();        
      var blob = new Blob([csd], {type: "text/plain;charset=utf-8"});

      var name = $scope.note.title.trim();
      if(name.length == 0) { name = "notebook"; }
      var csdName = name.replace(/ /g, "_") + ".csd";

      saveAs(blob, csdName);
    }

    $scope.saveNote = function(note) {
      //console.log("Saving note " + note.id); 
     
      if(note.id <= 0) {
        $http.post('/notes.json?callback=JSON_CALLBACK', note )
          .success(function(data, status, headers, config) {
            $scope.note.id = data.id; // update note.id
          }).error(function(data, status, headers, config) {
            alert("Oh no!  Couldn't save note... :( ");
          });
      } else {
        $http.put('/notes/' + note.id + '.json', note )
          .success(function(data, status, headers, config) {
        }).error(function(data, status, headers, config) {
           alert("Oh no!  Couldn't save note... :( ");
        });
      }
    };

    $scope.deleteNote = function(note) {
      if(confirm("Please confirm deleting this note.")) {
        console.log(note);
        
        $http.delete('/notes/' + note.id + '.json')
          .success(function(data, status, headers, config) {
            if($scope.note == note) {
              $scope.note = null;
              $scope.reloadNotebook($scope.currentNotebook);
            }
            //note.id = note.id; // update note.id
        }).error(function(data, status, headers, config) {
           alert("Oh no!  Couldn't load my notebooks... :( ");
        });
      }
    };


    $scope.evalCsoundCode = function() {
      var orcTab = $('#orcEditor');
      var scoTab = $('#scoEditor');

      if (orcTab.css("display") == "block") {
        var selection = $scope.orcTextEditor.getSelectionRange();
        if(selection.isEmpty()) {
          csound.CompileOrc($scope.orcTextEditor.getValue() );
        } else {
          csound.CompileOrc($scope.orcTextEditor.session.getTextRange(selection));
        }
      } else if (scoTab.css("display") == "block") {
        var selection = $scope.scoTextEditor.getSelectionRange();
        if(selection.isEmpty()) {
          csound.ReadScore($scope.scoTextEditor.getValue() );
        } else {
          csound.ReadScore($scope.scoTextEditor.session.getTextRange(selection));
        }
      }
    }

    $scope.handleShortcut = function(evt) {
      $scope.evalCsoundCode();
      evt.preventDefault();
    };

    $scope.togglePlay = function(){
      if(!playing) {
        if(started) csound.Play();
        else {
          csound.Play();
          csound.CompileOrc($scope.note.orc + "\n");
          if(!$scope.note.livesco) {
            csound.ReadScore($scope.note.sco + "\n");
          }
          started = true;
        }
        document.getElementById('playButton').innerText = "Pause";
        playing = true;
      } else {
        csound.Pause()
          document.getElementById('playButton').innerText ="Play";
        playing = false;
      }
    }

    $scope.selectTab = function(evt) {
      var buttons = $('#editorButtons').children(".btn");
      var panes = $('#editorPanes').children();
      var elem = evt.target || evt.srcElement;

      for(var i = 0; i < buttons.length; i++) {
        $(buttons[i]).removeClass("active");

        if (buttons[i] == elem) {
          $(buttons[i]).addClass("active");
          $(panes[i]).css("display", "block");
        } else {
          $(panes[i]).css("display", "none");
        }
      }
      $scope.scoActive = $('#scoEditor').css('display') == 'block';
    }

    //window.onbeforeunload = function() { 
    //  if (
    //  return "Please verify you wish to exit this page. (You may have unsaved work.)"; 
    //}

  }]);



/* NOTE CONTROLLER */

notebookControllers.controller('NoteController', ['$scope','$http', 
  function($scope, $http) {

    $scope.note = GLOBAL_NOTE;

    $scope.switchNoteURL= function() {
      var l = document.location;
      return l.protocol + "//" + l.host + "/notejs/" + $scope.note.id;
    }

    $scope.orcTextLoaded = function(_editor){
      $scope.orcTextEditor = _editor;
    };

    $scope.scoTextLoaded = function(_editor) {
      $scope.scoTextEditor = _editor;
    }

    $scope.setNote = function(n) {
      $scope.note = n;
    }

    $scope.getCSD = function() {
      var csd = "<CsoundSynthesizer>\n<CsInstruments>\n"
      csd += $scope.orcTextEditor.getValue();
      csd += "\n</CsInstruments>\n<CsScore>\n"
      if(!$scope.note.livesco) {
        csd += $scope.scoTextEditor.getValue();
      }
      csd += "\n</CsScore>\n</CsoundSynthesizer>\n"

      return csd;
    }

    $scope.exportCSD = function(note) {
      var csd = $scope.getCSD();        
      var blob = new Blob([csd], {type: "text/plain;charset=utf-8"});

      var name = $scope.note.title.trim();
      if(name.length == 0) { name = "notebook"; }
      var csdName = name.replace(/ /g, "_") + ".csd";

      saveAs(blob, csdName);
    }

    $scope.evalCsoundCode = function() {
      var orcTab = $('#orcEditor');
      var scoTab = $('#scoEditor');

      if (orcTab.css("display") == "block") {
        var selection = $scope.orcTextEditor.getSelectionRange();
        if(selection.isEmpty()) {
          csound.CompileOrc($scope.orcTextEditor.getValue() );
        } else {
          csound.CompileOrc($scope.orcTextEditor.session.getTextRange(selection));
        }
      } else if (scoTab.css("display") == "block") {
        var selection = $scope.scoTextEditor.getSelectionRange();
        if(selection.isEmpty()) {
          csound.ReadScore($scope.scoTextEditor.getValue() );
        } else {
          csound.ReadScore($scope.scoTextEditor.session.getTextRange(selection));
        }
      }
    }

    $scope.handleShortcut = function(evt) {
      $scope.evalCsoundCode();
      evt.preventDefault();
    };

    $scope.togglePlay = function(){
      if(!playing) {
        if(started) csound.Play();
        else {
          csound.Play();
          csound.CompileOrc($scope.note.orc + "\n");
          if(!$scope.note.livesco) {
            csound.ReadScore($scope.note.sco + "\n");
          }
          started = true;
        }
        document.getElementById('playButton').innerText = "Pause";
        playing = true;
      } else {
        csound.Pause()
          document.getElementById('playButton').innerText ="Play";
        playing = false;
      }
    }

    $scope.selectTab = function(evt) {
      var buttons = $('#editorButtons').children(".btn");
      var panes = $('#editorPanes').children();
      var elem = evt.target || evt.srcElement;

      for(var i = 0; i < buttons.length; i++) {
        $(buttons[i]).removeClass("active");

        if (buttons[i] == elem) {
          $(buttons[i]).addClass("active");
          $(panes[i]).css("display", "block");
        } else {
          $(panes[i]).css("display", "none");
        }
      }
      $scope.scoActive = $('#scoEditor').css('display') == 'block';
    }

  }]);

