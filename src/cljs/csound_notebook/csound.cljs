(ns csound-notebook.csound
 (:require
    [goog.string :as gstring]
    goog.string.format)) 

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
      (.log js/console (str "CSD TExt: " csd-text))
            (.compileCSD cs csd-text)
            (.start cs))
          (stop-engine [_] (.stop cs))       
          (reset-engine [_] (.reset cs))       
          (compile-orc [_ orc-text] (.evaluateCode cs orc-text))
          (compile-sco [_ sco-text] (.readScore cs sco-text)))))  
   500) 
  
  (.log js/console "Finished Loading Emscripten CsoundObj."))

(defn load-csoundObj [version]
  (js/setTimeout
    #(load-script! (gstring/format "/%s/CsoundObj.js" version) finish-emscripten-load!)
    4000))

(defn load-emscripten! []
  (let [version (if (exists? js/WebAssembly) "wasm" "asmjs")]
    (.log js/console (gstring/format "Loading %s CsoundObj..." version))
    (load-script! (gstring/format "/%s/libcsound.js" version) 
                  #(load-csoundObj version))))


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

