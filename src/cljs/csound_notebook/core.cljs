(ns csound-notebook.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [csound-notebook.ajax :refer [load-interceptors!]]
            [csound-notebook.handlers]
            [csound-notebook.subscriptions :as s]
            [csound-notebook.db :as db]
            [csound-notebook.csound :as cs]
            [cljs.pprint :refer [pprint]]
            )
  (:import goog.History))

(defn log [s] (.log js/console s))

;; ORC/SCO LIVE EVALUATION

(defn eval-orc [orc]
  (when-let [cs-obj @cs/csoundObj]
    (cs/compile-orc cs-obj orc)))

(defn eval-sco [sco]
  (when-let [cs-obj @cs/csoundObj]
    (cs/compile-sco cs-obj sco)))

(defn eval-code [eval-type cm]
  (when-let [selection (.getSelection cm)]
    (if (= :orc eval-type)
      (eval-orc selection)
      (eval-sco selection))))

;; CSD 

(defn get-csd 
  ([] (get-csd false))
  ([process-score]
  (let [orcEd (.getElementById js/document "csoundOrcEditor")
        scoEd (.getElementById js/document "csoundScoEditor")
        orc (.-value orcEd)
        sco (if process-score (.-value scoEd) "")
        csd (str "<CsoundSynthesizer>\n<CsInstruments>\n"
                 orc 
                 "\n</CsInstruments>\n<CsScore>\n"
                 sco 
                 "\n</CsScore>\n</CsoundSynthesizer>\n")]
      csd
    )))

;; BUTTON CALLBACKS

(defn handle-export-csd [e]
  (let [note @(rf/subscribe [:note])
        csd (get-csd (:livesco note))
        blob (js/Blob. [csd] (js-obj {"type" "text/plain;charset=utf-8"}))
        csd-name (:title note) 
        file-name (str (clojure.string/replace csd-name #" " "_") ".csd")]
    (js/saveAs blob file-name)))

(defn handle-play [e]
  (when-let [cs-obj @cs/csoundObj]
    (cs/start-engine cs-obj (get-csd))))

(defn handle-eval [e])
(defn handle-save [e])
(defn handle-delete [e])


;; VIEWS

(defn orc-editor-did-mount  [input]
  (fn  [this]
    (let  [cm  (.fromTextArea  js/CodeMirror
                              (r/dom-node this)
                              #js  {:mode "javascript"
                                    :lineNumbers true
                                    })]
      (.setSize cm "100%" "100%")
      (.setOption cm "extraKeys"
                  (js-obj "Ctrl-E" (partial eval-code :orc)))
      ;(.on cm "change" #(reset! input  (.getValue %)))
      
      ))) 

(defn orc-editor  [input]
  (r/create-class
    {:render  (fn  []  [:textarea.csound-editor
                        {:default-value (:orc input) 
                         :auto-complete "off"
                         :id "csoundOrcEditor"}])
     :component-did-mount  (orc-editor-did-mount input)}))

(defn sco-editor-did-mount  [input]
  (fn  [this]
    (let  [cm  (.fromTextArea  js/CodeMirror
                              (r/dom-node this)
                              #js  {:mode "javascript"
                                    :lineNumbers true
                                    })]
      (.setSize cm "100%" "100%")
      (.setOption cm "extraKeys"
                  (js-obj "Ctrl-E" (partial eval-code :sco)))
      ;(.on cm "change" #(reset! input  (.getValue %)))
      
      ))) 

(defn sco-editor  [input]
  (r/create-class
    {:render  (fn  []  [:textarea.csound-editor
                        {:default-value (:sco input) 
                         :auto-complete "off"
                         :id "csoundScoEditor"}])
     :component-did-mount  (sco-editor-did-mount input)}))

(defn tab-show [e]
  #_(.preventDefault e)
  #_(this-as 
    this
    (.tab this "show")))

(defn home-page []
  [:div.csound-editor
   [:div.btn-toolbar {:role "toolbar"}
    ;[:div.btn-group.btn-group-sm.mr-2 {:role "group"}
    ; [:button.btn.btn-default.active 
    ;  {:data-toggle "tab" :href "#orc" :type "button"} 
    ;  "ORC"]
    ; [:button.btn.btn-default 
    ;  {:data-toggle "tab" :href "#sco" :type "button"} 
    ;  "SCO"]
    ; [:button.btn.btn-default 
    ;  {:data-toggle "tab" :href "#console" :type "button"} 
    ;  "Console"]
    ; [:button.btn.btn-default 
    ;  {:data-toggle "tab" :href "#help" :type "button"} 
    ;  "Help"]
    ; ]
    [:div.btn-group.btn-group-sm.mr-2 {:role "group"}
     [:button.btn.btn-default 
      {:type "button" :on-click handle-export-csd} 
      "Download CSD"] 
     ]
    [:div.btn-group.btn-group-sm {:role "group"}
     [:button.btn.btnsecondary {:type "button" :on-click handle-play} "Play"]
     [:button.btn.btnsecondary {:type "button" :on-click handle-eval} "Evaluate"]
     [:button.btn.btnsecondary {:type "button" :on-click handle-save} "Save"]
     [:button.btn.btnsecondary {:type "button" :on-click handle-delete} "Delete"]
     ]
    ]

    [:ul.nav.nav-tabs {:role "tablist"}
     [:li.nav-item [:a.nav-link.active 
                    {:data-toggle "tab" :href "#orc" :role "tab" }  
                    "ORC"]]
     [:li.nav-item [:a.nav-link 
                    {:data-toggle "tab" :href "#sco" :role "tab" }
                    "SCO"]]
     [:li.nav-item [:a.nav-link 
                    {:data-toggle "tab" :href "#console" :role "tab" } 
                    "Console"]]
     [:li.nav-item [:a.nav-link 
                    {:data-toggle "tab" :href "#help" :role "tab" }
                    "Help" ]]]

   [:div.container-fluid.tab-content
    {:style {:height "calc(100% - 52px)"}}
    ;[:pre  (with-out-str  (pprint @re-frame.db/app-db))]
    (when-let [n (rf/subscribe [:note]) ]
      [:div.tab-pane.csound-editor.active {:id "orc" :role "tabpanel"} 
       [orc-editor @n]])
    (when-let [n (rf/subscribe [:note]) ]
      [:div.tab-pane.csound-editor {:id "sco" :role "tabpanel"}  
       [sco-editor @n]])

    [:div.tab-pane.csound-editor {:id "console" :role "tabpanel"}  
     [:textarea.csound-editor 
      {:id "console-text" :style {:width "100%" :height "100%"}}
      ]]
    [:div.tab-pane {:id "help" :role "tabpanel"}
     "help"
     ]
    ]]
  
  )

(def pages
  {:home #'home-page })

(defn page []
  [:div.csound-editor
   [(pages @(rf/subscribe [:page]))]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :home]))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET "/docs" {:handler #(rf/dispatch [:set-docs %])}))

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (cs/load-csound!)
  (load-interceptors!)
  ;(fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
