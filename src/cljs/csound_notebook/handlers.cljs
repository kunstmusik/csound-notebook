(ns ^:figwheel-always csound-notebook.handlers
  (:require [csound-notebook.db :as db]
            [re-frame.core :refer [dispatch reg-event-db]]))

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(reg-event-db
  :set-active-page
  (fn [db [_ page]]
    (assoc db :page page)))

(reg-event-db
  :set-active-note
  (fn [db [_ note]]
    (assoc db :note note)))

(reg-event-db
  :set-docs
  (fn [db [_ docs]]
    (assoc db :docs docs)))
