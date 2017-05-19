(ns csound-notebook.routes.home
  (:require [csound-notebook.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

(defn md-page [md-file]
  (layout/render "home.html" {:contents (slurp (io/resource md-file))}))

(defn home-page [{session :session}]
  (layout/render "home.html" {:contents (str "# HI " (:name (:identity session)))}))

(defn about-page []
  (md-page "docs/about.md"))

(defn note-page [req]
  (layout/render "note.html"))


(defroutes home-routes
  (GET "/" req 
       (home-page req))

  (GET "/note" req 
       (note-page req))

  (GET "/about" [] 
       (about-page))
  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
       (response/header "Content-Type" "text/plain; charset=utf-8"))))

