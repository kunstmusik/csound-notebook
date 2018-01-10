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
  [script-file]
  (let [script (.createElement js/document "script")] 
    (aset script "src" script-file)
    (aset script "async" false)
    (-> (.-body js/document)
        (.appendChild script))
    ))

;; EMSCRIPTEN CSOUND LOADING

(defn- finish-csound-load! []

  ;;(load-script! (gstring/format "/%s/CsoundObj.js" version))
  (let [cs (js/CsoundObj.)] 
    (reset!  
      csoundObj 
      (reify CsoundEngine
        (start-engine [_ csd-text]
          (.compileCSD cs csd-text)
          (.start cs))
        (stop-engine [_] (.stop cs))       
        (reset-engine [_] (.reset cs))       
        (compile-orc [_ orc-text] (.evaluateCode cs orc-text))
        (compile-sco [_ sco-text] (.readScore cs sco-text))))

    (.log js/console "Finished Loading CsoundObj.")))

;; LOAD CSOUND

(set! js/Module (js-obj))

(defn load-csound! []

  (let [csout (.getElementById js/document "console-text") 
        append (fn [t] 
                 (aset csout "value" 
                       (str (aget csout "value") t "\n")))]  
    (aset js/Module "print" append) 
    (aset js/Module "printErr" append)
    (aset js/Module "onRuntimeInitialized" finish-csound-load!))
  (let [version (if (exists? js/WebAssembly) "wasm" "asmjs")]
    (.log js/console (gstring/format "Loading %s CsoundObj..." version))
    (load-script! (gstring/format "/%s/libcsound.js" version))
    (load-script! (gstring/format "/%s/CsoundObj.js" version))
    ))

