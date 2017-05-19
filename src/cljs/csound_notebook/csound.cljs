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
  
  (.log js/console "Finished Loading Emscripten CsoundObj."))

(defn load-emscripten! []
  (.log js/console "Loading Emscripten CsoundObj...")
  (load-script! "/javascripts/libcsound.js" finish-emscripten-load!))


;; PNACL


(defn create-module
  []
  (let [module (.createElement js/document "embed")] 
    (.setAttribute module"name" "csound_module")
    (.setAttribute module"id" "csound_module")
    (.setAttribute module "path" "/Release")
    (.setAttribute module "src" "/Release/csound.nmf")
    (.setAttribute module "type" "application/x-pnacl")
    (-> (.-body js/document)
        (.appendChild module))
    module))

(def counter (atom 0))

(defn progress-handler
  [evt]
  (if (and (aget evt "lengthComputable") 
           (pos? (aget evt "total")))
    (let [percent (* 100.0 (/ (aget evt "loaded") (aget evt "total")))]
      (.log js/console (str "Loading: " percent " %")))
    (.log js/console (str "Loading: (count=" (swap! counter inc) ")"))))


(defn finish-pnacl [module]
  (.log js/console "Finished Loading PNACL Csound.")
  (reset! 
      csoundObj
      (reify CsoundEngine
        (start-engine [_ csd-text]
          (.postMessage module (str "csd:" csd-text)))
        (stop-engine [_] (.log js/console "stop-engine not implemented"))       
        (reset-engine [_] (.log js/console "reset-engine not implemented"))       
        (compile-orc [_ orc-text] (.postMessage module (str "orchestra:" orc-text)))
        (compile-sco [_ sco-text] (.postMessage module (str "score:" sco-text)))))
  )

(defn pnacl-message-handler 
  [evt] 
  (let [csout (.getElementById js/document "console-text")]  
    (aset csout "value" 
          (str (aget csout "value") (.-data evt)))))

(defn load-pnacl!
  []
  (.log js/console "Loading PNACL Csound...")
  (let [module (create-module)] 
    (.addEventListener module "message" pnacl-message-handler true)
    (.addEventListener module "progress" progress-handler true)
    (.addEventListener module "load" (partial finish-pnacl module) true)))

;; LOAD CSOUND

(defn load-csound! []
  (if (aget js/navigator.mimeTypes "application/x-pnacl" )
    (load-pnacl!) 
    (load-emscripten!)))

