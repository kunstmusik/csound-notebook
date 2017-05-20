(ns csound-notebook.routes.notebook
  (:require [csound-notebook.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [buddy.auth :refer [authenticated?]]
            ))

(defn notebook-page [req]
  (if (authenticated? req)
    (response/found "/")
    (response/found "/")))


(defroutes notebook-routes
  (GET "/notebook" req
      (notebook-page req)))
