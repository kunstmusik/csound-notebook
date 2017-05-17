# About

The Csound Notebook Server is an online notebook for you to create, perform,
and share Csound projects. There are two versions of the Notebook, one using the
<a href="https://developers.google.com/native-client/dev/">Portable Native Client (pNaCl)</a> of Csound, 
the other using the Emscripten version of Csound.  The former requires using Google Chrome or Chromium browsers that support pNaCl. 
If you are not using Chrome, please use the Emscripten version of the Notebook.  

Note: No prior installation of Csound is necessary, as the site will use these web versions of Csound to run your notes.

## User Guide

<p><i>This is a basic introduction. A more developed manual to be written shortly.</i></p>

* Create a Notebook using the + button in the left hand column.
* Delete a Notebook using the - button.
* Select a Notebook and its notes will load in the second column.
* Create a new Note using the + button. The Note's properties will be in the right-hand editor pane.
* Use the Play button to start Csound. Use the Evaluate button to load the ORC code or SCO code, 
  depending on what tab is currently visible. If there is a selection of text, only the selected text will be evaluated. You can also evaluate using the shift-enter shortcut.
* Be sure to Save the note or it will be lost! (The app will eventually track if things have been edited and if so, to warn the user to save the note.  This is not yet implemented.)

## Change Log

**0.3** - 2015.02.21

* Added Emscripten version of Notebook
* Added "Save CSD" button to editor to download a note as CSD

**0.2** - 2014.04.18

* Added ACE text editor

**0.1** - 2014.04.16

* Initial Release

## Credits

* Steven Yi
