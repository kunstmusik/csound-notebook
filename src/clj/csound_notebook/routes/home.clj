(ns csound-notebook.routes.home
  (:require [csound-notebook.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

(defn md-page [md-file]
  (layout/render "home.html" {:contents (slurp (io/resource md-file))}))

(defn home-page [{session :session}]
  (layout/render "home.html" {:contents (str "# HI " (:name (:user session)))}))

(defn about-page []
  (md-page "docs/about.md"))

(defn register-page []
  (layout/render "register.html"))

(defn login-page []
  (layout/render "login.html"))

(defn reset-page []
  (layout/render "reset.html"))

(defn note-page [req]
  (layout/render "note.html"))


(defn handle-login [{session :session}]
  (-> (response/found "/")
      (assoc :session (assoc session :user {:name "Steven"}))))

(defn handle-logout [{session :session}]
  (-> (response/found "/")
      (dissoc :user)))

(defroutes home-routes
  (GET "/" req 
       (home-page req))

  (GET "/note" req 
       (note-page req))

  (GET "/about" [] 
       (about-page))
  (GET "/user/login" []
       (login-page))

  (GET "/user/logout" req 
       (login-page req))

  (POST "/user/login" req 
       (handle-login req))

  (GET "/user/register" []
       (register-page))
  (GET "/user/reset" []
       (reset-page))
  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
       (response/header "Content-Type" "text/plain; charset=utf-8"))))

