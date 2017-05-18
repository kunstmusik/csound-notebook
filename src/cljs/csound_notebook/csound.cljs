(ns csound-notebook.csound)

(def csoundObj (atom nil))

(defprotocol CsoundEngine
  (start-engine [cs csd-text])
  (stop-engine [cs])
  (reset-engine [cs])
  (compile-orc [cs orc-text])
  (compile-sco [cs sco-text]))

;; EMSCRIPTEN CSOUND LOADING

(defn set-emscripten-callbacks 
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

(defn load-emscripten!  
  []
  (let [script (.createElement js/document "script")] 
    (aset script "src" "/javascripts/libcsound.js")
    (aset script "onload" set-emscripten-callbacks)
    (-> (.-body js/document)
        (.appendChild script))
    ))


;; PNACL

(defn load-pnacl!
  []
  (.log js/console "PNACL"))

;; LOAD CSOUND

(defn load-csound! []
  (if (aget js/navigator.mimeTypes "application/x-pnacl" )
    (load-pnacl!) 
    (load-emscripten!)
    ))

