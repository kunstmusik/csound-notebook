(ns csound-notebook.routes.note
  (:require [csound-notebook.layout :as layout]
            [csound-notebook.db.core :as db]
            [compojure.core :refer [defroutes GET POST context]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [buddy.auth :refer [authenticated?]]
            ))

;; TODO - check if user is authenticated or that note is public
(defn get-note 
  [req username id]
  (if-let [note 
           (if username
             (db/get-note-for-user {:username username :note-id id})
             (db/get-note {:note-id id}))]
    {:body note}
    {:body {:error "Unable to access Csound note."}}))


(defroutes note-routes
  (context "/note" []
    (GET "/:id" [id :as req]  
         (get-note req nil id))
    (GET "/:username/:id" [username id :as req] 
         (get-note req username id)))
  )
  
