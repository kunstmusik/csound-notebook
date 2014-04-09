
function NotesController($scope) {

  $scope.note = {
      orc: "sr=44100
ksmps=32
nchnls=2
0dbfs=1

instr 1
aout vco2 .5, 440
aout moogladder aout, 2000, .3
outs aout, aout
endin",
  
      sco: "i1 0 1"

  };
}

