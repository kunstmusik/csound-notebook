(ns csound-notebook.db)

(def default-note
  {:orc 
   "sr=44100\nksmps=32\nnchnls=2\n0dbfs=1\n\ninstr 1\nipch = cps2pch(p4,12)\niamp = ampdbfs(p5)\naenv linsegr 0, 0.01, 1, 0.01, .9, .3, 0\naout vco2 iamp, ipch\naout = aout * aenv\naout moogladder aout, 2000, .3\nouts aout, aout\nendin"

   :sco "i1 0 1 8.00 -12\ni1 0 1 8.04 -12\ni1 0 1 8.07 -12"
   :title "My Note"
   :public false
   :livesco false 
   :note-id ""
   })


(def default-db
  {:page :home
   :note default-note
   })
