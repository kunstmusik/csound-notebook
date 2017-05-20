(ns csound-notebook.routes.note
  (:require [csound-notebook.layout :as layout]
            [compojure.core :refer [defroutes GET POST context]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))



(defn note-page [req]
  (layout/render "note.html"))


(defroutes note-routes
  (context 
    "/note" []

    (GET "/" req 
         (note-page req))
    (GET "/:id" req 
         (note-page req))
    ))
  
