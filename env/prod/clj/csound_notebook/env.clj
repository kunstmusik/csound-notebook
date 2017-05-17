(ns csound-notebook.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[csound-notebook started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[csound-notebook has shut down successfully]=-"))
   :middleware identity})
