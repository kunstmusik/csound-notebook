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

(defn- finish-csound-load! 
  []
  (let [csout (.getElementById js/document "console-text") 
        append (fn [t] 
                 (aset csout "value" 
                       (str (aget csout "value") t "\n")))]  
    (aset js/Module "print" append) 
    (aset js/Module "printErr" append))
 
 (js/setTimeout
   #(let [cs (js/CsoundObj.)] 
      (reset!  
        csoundObj 
        (reify CsoundEngine
          (start-engine [_ csd-text]
            (.compileCSD cs csd-text)
            (.start cs))
          (stop-engine [_] (.stop cs))       
          (reset-engine [_] (.reset cs))       
          (compile-orc [_ orc-text] (.evaluateCode cs orc-text))
          (compile-sco [_ sco-text] (.readScore cs sco-text)))))  
   500) 
  
  (.log js/console "Finished Loading CsoundObj."))

(defn load-csoundObj [version]
  ;; TODO - need to figure out a way to get callback when module is loaded
  ;; rather than use this timeout hack
  (js/setTimeout
    #(load-script! (gstring/format "/%s/CsoundObj.js" version) finish-csound-load!)
    4000))

;; LOAD CSOUND

(defn load-csound! []
  (let [version (if (exists? js/WebAssembly) "wasm" "asmjs")]
    (.log js/console (gstring/format "Loading %s CsoundObj..." version))
    (load-script! (gstring/format "/%s/libcsound.js" version) 
                  #(load-csoundObj version))))

