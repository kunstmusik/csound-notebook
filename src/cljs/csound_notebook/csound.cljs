(ns csound-notebook.csound)

(def csoundObj (atom nil))

(defprotocol CsoundEngine
  (start-engine [cs csd-text])
  (stop-engine [cs])
  (reset-engine [cs])
  (compile-orc [cs orc-text])
  (compile-sco [cs sco-text]))

;; SCRIPT LOADING FUNCTION

(defn load-script!
  [script-file callback]
  (let [script (.createElement js/document "script")] 
    (aset script "src" script-file)
    (when callback
      (aset script "onload" callback))
    (-> (.-body js/document)
        (.appendChild script))
    ))

;; EMSCRIPTEN CSOUND LOADING

(defn- finish-emscripten-load! 
  []
  (let [csout (.getElementById js/document "console-text") 
        append (fn [t] 
                 (aset csout "value" 
                       (str (aget csout "value") t "\n")))
        ]  
    (aset js/Module "print" append) 
    (aset js/Module "printErr" append))
 
 (js/setTimeout
   #(let [cs (js/CsoundObj.)] 
      (reset!  
        csoundObj 
        (reify CsoundEngine
          (start-engine [_ csd-text]
            (.writeFile js/FS "/temp.csd" csd-text (js-obj {"encoding" "utf8"}))
            (.compileCSD cs "/temp.csd")
            (.start cs))
          (stop-engine [_] (.stop cs))       
          (reset-engine [_] (.reset cs))       
          (compile-orc [_ orc-text] (.evaluateCode cs orc-text))
          (compile-sco [_ sco-text] (.readScore cs sco-text)))))  
   500) 
  
  (.log js/console "Loaded Emscripten CsoundObj"))

(defn load-emscripten! []
  (.log js/console "Loading Emscripten CsoundObj...")
  (load-script! "/javascripts/libcsound.js" finish-emscripten-load!))


;; PNACL

(defn load-pnacl!
  []
  (.log js/console "PNACL"))

;; LOAD CSOUND

(defn load-csound! []
  (if (aget js/navigator.mimeTypes "application/x-pnacl" )
    (load-pnacl!) 
    (load-emscripten!)))

