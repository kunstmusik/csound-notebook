(ns csound-notebook.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [csound-notebook.layout :refer [error-page]]
            [csound-notebook.routes.home :refer [home-routes]]
            [csound-notebook.routes.user :refer [user-routes]]
            [csound-notebook.routes.note :refer [note-routes]]
            [compojure.route :as route]
            [csound-notebook.env :refer [defaults]]
            [mount.core :as mount]
            [csound-notebook.middleware :as middleware]
            ))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    (-> (routes #'user-routes #'note-routes #'home-routes)
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))


(defn app [] (middleware/wrap-base #'app-routes))
