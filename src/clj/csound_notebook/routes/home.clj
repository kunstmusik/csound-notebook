(ns csound-notebook.routes.home
  (:require [csound-notebook.layout :as layout]
            [csound-notebook.db.core :as db]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [buddy.auth :refer [authenticated?]]
            ))

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
  (layout/render "note.html" {:logged-in (str (authenticated? req))}))

(defn save-note 
  [req]
  (if-let [p (:params req)]
   (let [note-id (gen-note-id)
         user (:identity (:session req))
         user-id (:id user)
         username (:username user)]
     (db/create-note! {:orc (:orc p)
                     :sco (:sco p)
                     :note-id note-id 
                     :is-live false
                     :is-public false
                     :user-id user-id  
                     })
      {:body {:username username :noteId note-id}}) 
   (:body {:error "Invalid Parameters."})))


(defn md-page [md-file]
  (layout/render "home.html" {:contents (slurp (io/resource md-file))}))

(defn home-page [{session :session}]
  (layout/render "home.html" {:contents (str "# HI " (:name (:identity session)))}))

(defn about-page []
  (md-page "docs/about.md"))


(defroutes home-routes

  (GET "/about" [] 
       (about-page))
  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
       (response/header "Content-Type" "text/plain; charset=utf-8"))) 

  (GET "/" req 
       (note-page req))
  (GET "/:note-id" req 
       (note-page req))
  (GET "/:user-id/:note-id" req 
       (note-page req))

  (POST "/" req
        (save-note req))
  (POST "/:id" req
        (save-note req))
  (POST "/:username/:id" req
        (save-note req))

  )

