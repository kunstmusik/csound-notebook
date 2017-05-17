(ns ^:figwheel-always csound-notebook.subscriptions
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :page
  (fn [db _]
    (:page db)))

(reg-sub
  :note
  (fn [db _]
    (:note db)))

(reg-sub
  :docs
  (fn [db _]
    (:docs db)))
