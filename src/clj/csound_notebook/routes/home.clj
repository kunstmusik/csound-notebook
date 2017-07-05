(ns csound-notebook.routes.home
  (:require [csound-notebook.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

;; Note pages

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


(defn md-page [md-file]
  (layout/render "home.html" {:contents (slurp (io/resource md-file))}))

(defn home-page [{session :session}]
  (layout/render "home.html" {:contents (str "# HI " (:name (:identity session)))}))

(defn about-page []
  (md-page "docs/about.md"))


(defroutes home-routes
  (GET "/" req 
       (note-page req))
  (GET "/:note-id" req 
       (note-page req))
  (GET "/:user-id/:note-id" req 
       (note-page req))

  (GET "/about" [] 
       (about-page))
  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
       (response/header "Content-Type" "text/plain; charset=utf-8"))))

