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

(defonce orcEditor (atom nil))
(defonce scoEditor (atom nil))

;; store JQuery as jq
(def jq (js* "$"))

;; ORC/SCO LIVE EVALUATION

(defn eval-orc [orc]
  (when-let [cs-obj @cs/csoundObj]
    (cs/compile-orc cs-obj orc)))

(defn eval-sco [sco]
  (when-let [cs-obj @cs/csoundObj]
    (cs/compile-sco cs-obj sco)))


;; HELP

(defn show-help []
  (.modal (jq "#helpModal") "show"))

;; CSD 

(defn get-csd 
  ([] (get-csd false))
  ([process-score]
  (let [orc (.getValue @orcEditor)
        sco (if process-score (.getValue @scoEditor) "")
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
        blob (js/Blob. #js [csd] (js-obj {"type" "text/plain;charset=utf-8"}))
        csd-name (:title note) 
        file-name (str (clojure.string/replace csd-name #" " "_") ".csd")]
    (js/saveAs blob file-name)))

(defn handle-play [e]
  (when-let [cs-obj @cs/csoundObj]
    (cs/start-engine cs-obj (get-csd))))

(defn handle-eval [e]
  (let [orc-ed (jq "#csoundOrcEditor")
        sco-ed (jq "#csoundScoEditor")])
  ;(log (.-visible orc-ed))
  ;(log (.-visible sco-ed))
  )

(defn update-url
  [username note-id]
  (if username
    (.replaceState js/history "" "" (str "/" username "/" note-id))
    (.replaceState js/history "" "" (str "/" note-id))))

(defn handle-save [e]
  (let [orc (.getValue @orcEditor)
        sco (.getValue @scoEditor) 
        note {:orc orc :sco sco} ] 
    (POST "/" 
        {:handler #(update-url (get % "username") (get % "noteId") )
         :error-handler #(js/alert "Error: Unable to save note.")
         :format :json
         :response-format :json
         :params note
         }
        )))

;(defn handle-delete [e])


;; VIEWS

(defn orc-editor-did-mount  [input]
  (fn  [this]
    (let  [cm  (.fromTextArea  js/CodeMirror
                              (r/dom-node this)
                              #js  {:mode "javascript"
                                    :lineNumbers true
                                    :autofocus false 
                                    :autoRefresh true
                                    })]
      (.setSize cm "100%" "100%")
      (.setOption cm "extraKeys"
                  (js-obj "Ctrl-E" #(eval-orc (.getSelection cm))))
      (reset! orcEditor cm)
      ;(.on cm "change" #(reset! input  (.getValue %)))
      ))) 

(defn orc-editor  [input]
  (r/create-class
    {:render  (fn  []  [:textarea.h-100
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
                                    :autofocus false 
                                    :autoRefresh true
                                    })]
      (.setSize cm "100%" "100%")
      (.setOption cm "extraKeys"
                  (js-obj "Ctrl-E" #(eval-sco (.getSelection cm))))
      (reset! scoEditor cm)
      ;(.on cm "change" #(reset! input  (.getValue %)))
      ))) 

(defn sco-editor  [input]
  (r/create-class
    {:render  (fn  []  [:textarea.h-100
                        {:default-value (:sco input) 
                         :auto-complete "off"
                         :id "csoundScoEditor"}])
     :component-did-mount  (sco-editor-did-mount input)}))

;(defn tab-show [e]
;  #_(.preventDefault e)
;  #_(this-as 
;    this
;    (.tab this "show")))

(defn home-page []

  [:div.container-fluid.h-100

    [:ul.nav.nav-tabs.row
     [:li.nav-item [:a.nav-link.active 
                    {:data-toggle "tab" :href "#orc" :role "tab" }  
                    "ORC"]]
     [:li.nav-item [:a.nav-link 
                    {:data-toggle "tab" :href "#sco" :role "tab" }
                    "SCO"]]
     [:li.nav-item [:a.nav-link 
                    {:data-toggle "tab" :href "#console" :role "tab" } 
                    "Console"]]
     ]

   [:div.tab-content.row
    {:style {:height "calc(100% - 100px)"}}
    ;[:pre  (with-out-str  (pprint @re-frame.db/app-db))]
    (when-let [n (rf/subscribe [:note]) ]
      [:div.tab-pane.h-100.w-100.active {:id "orc" :role "tabpanel"} 
       [orc-editor @n]])
    (when-let [n (rf/subscribe [:note]) ]
      [:div.tab-pane.h-100.w-100 {:id "sco" :role "tabpanel"}  
       [sco-editor @n]])

    [:div.tab-pane.h-100.w-100 {:id "console" :role "tabpanel"}  
     [:textarea.h-100
      {:id "console-text" :style {:width "100%"}}
      ]]
    ]]
  
  )

(defn top-nav []
  [:ul.navbar-nav.mr-auto 
  [:li.nav-item
    [:a.nav-link {:href "/"} 
     [:i.fa.fa-plus {:aria-hidden "true"}] " New"]]

   [:li.nav-item
    [:a.nav-link {:href "javascript:void(0);" :on-click handle-play}
     [:i.fa.fa-play {:aria-hidden "true"}] " Play"]]
   [:li.nav-item  
    [:a.nav-link {:href "javascript:void(0);" :on-click handle-eval}
     [:i.fa.fa-repeat {:aria-hidden "true"}] " Evaluate"]]
   [:li.nav-item  
    [:a.nav-link {:href "javascript:void(0);"  :on-click handle-save}
     [:i.fa.fa-floppy-o {:aria-hidden "true"}] " Save"]]
   [:li.nav-item  
    [:a.nav-link {:href "javascript:void(0);" :on-click handle-export-csd}
     [:i.fa.fa-cloud-download {:aria-hidden "true"}] " Download CSD"]]
   [:li.nav-item  
    [:a.nav-link {:href "javascript:void(0);" :on-click show-help}
     [:i.fa.fa-info-circle {:aria-hidden "true"}] " Help"]]
   ])

(def pages
  {:home #'home-page })

(defn page []
  [(pages @(rf/subscribe [:page]))]
  )

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
  (r/render [#'page] (.getElementById js/document "app"))
  (r/render [#'top-nav] (.getElementById js/document "navigation")))

(defn load-note
  []

  )

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (cs/load-csound!)
  (load-interceptors!)
  ;(fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
