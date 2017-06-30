(ns csound-notebook.routes.note
  (:require [csound-notebook.layout :as layout]
            [csound-notebook.db.core :as db]
            [compojure.core :refer [defroutes GET POST context]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

(def valid-chars
  "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")

(def num-chars (.length valid-chars))

(defn gen-note-id
  "Generates string with 8 random alphanumeric characters."
  []  
  (str
    (.charAt valid-chars (int (* num-chars (Math/random))))
    (.charAt valid-chars (int (* num-chars (Math/random))))
    (.charAt valid-chars (int (* num-chars (Math/random))))
    (.charAt valid-chars (int (* num-chars (Math/random))))
    (.charAt valid-chars (int (* num-chars (Math/random))))
    (.charAt valid-chars (int (* num-chars (Math/random))))
    (.charAt valid-chars (int (* num-chars (Math/random))))
    (.charAt valid-chars (int (* num-chars (Math/random))))
    ))

(defn note-page 
  [req]
  (layout/render "note.html"))

(defn save-note 
  [req]
  {:body {:note_id "12345678"}})


(defroutes note-routes
  (context 
    "/note" []

    (GET "/" req 
         (layout/render "note.html"))
    (GET "/:id" req 
         (layout/render "note.html"))
    (GET "/:username/:id" req 
         (layout/render "note.html"))
    (POST "/" req
          (save-note req))
    (POST "/:id" req
          (save-note req))
    (POST "/:username/:id" req
          (save-note req))
    ))
  
