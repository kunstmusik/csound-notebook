

function showTab(index) {

  $( "#editorTab0").attr("class", (index == 0) ? "active" : "");
  $( "#editorTab1").attr("class", (index == 1) ? "active" : "");
  $( "#editorTab2").attr("class", (index == 2) ? "active" : "");
  $( "#editorTab3").attr("class", (index == 3) ? "active" : "");

  $( "#orc_code").attr("class", (index == 0) ? "ng-show" : "ng-hide");
  $( "#sco_code").attr("class", (index == 1) ? "ng-show" : "ng-hide");
  $( "#console").attr("class", (index == 2) ? "ng-show" : "ng-hide");
  $( "#help").attr("class", (index == 3) ? "ng-show" : "ng-hide");
}

function evalCode() {
  var orcText = $("#orc_code");
  var scoText = $("#sco_code");

  if (orcText.hasClass("ng-show")) {
    var selection = orcText.textrange('get');
    if(selection.length > 0) {
      csound.CompileOrc( selection.text );
    } else {
      csound.CompileOrc( orcText.val() + "\n");
    }
  } else if ( scoText.hasClass("ng-show")) {
    var selection = scoText.textrange('get');
    if(selection.length > 0) {
      csound.ReadScore( selection.text );
    } else {
      csound.ReadScore( scoText.val() + "\n");
    }
  }
}

// CSOUND RELATED FUNCTIONS


// called by csound.js
function moduleDidLoad() {

}
function attachListeners() { 
  document.getElementById('playButton').addEventListener('click', togglePlay);
  document.getElementById('evalButton').addEventListener('click', evalCode);
//  document.getElementById('files').addEventListener('change', handleFileSelect, false);
  $("html").keydown(function() {
    if(event.shiftKey && (event.which == 10 || event.which == 13)) {
      event.preventDefault();
      evalCode();
    }
  });

  //$("#sco_code").keydown(function() {
  //  if(event.shiftKey && (event.which == 10 || event.which == 13)) {
  //    event.preventDefault();
  //    evalCode();
  //  }
  //});
}

var count = 0;
function handleMessage(message) {
  var element = document.getElementById('console');
  element.value += message.data;
  element.scrollTop = 99999; // focus on bottom
  count += 1;
  if(count == 1000) {
    element.value = ' ';
    count = 0;
  }
}


var playing = false;
var started = false;
var loaded = false;
var fname;

function togglePlay(){
    if(!playing) {
      if(started) csound.Play();
      else {
        csound.Play();
        //csound.CompileOrc($( "#orc_code").val() + "\n");
        //csound.ReadScore($( "#sco_code").val() + "\n");
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

function handleFileSelect(evt) {
  if(!loaded) {
    var files = evt.target.files; 
    var f = files[0];
    var objectURL = window.webkitURL.createObjectURL(f);
    csound.CopyUrlToLocal(objectURL, f.name);
    fname = f.name;
    loaded = true;
  } else {
    csound.updateStatus("to load a new CSD, first refresh page!")
  }
}
