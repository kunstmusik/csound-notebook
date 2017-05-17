(ns user
  (:require [mount.core :as mount]
            [csound-notebook.figwheel :refer [start-fw stop-fw cljs]]
            csound-notebook.core))

(defn start []
  (mount/start-without #'csound-notebook.core/repl-server))

(defn stop []
  (mount/stop-except #'csound-notebook.core/repl-server))

(defn restart []
  (stop)
  (start))


