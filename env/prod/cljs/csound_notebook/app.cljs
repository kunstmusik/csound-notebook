(ns csound-notebook.app
  (:require [csound-notebook.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
