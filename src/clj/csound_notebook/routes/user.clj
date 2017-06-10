(ns csound-notebook.routes.user
  (:require [csound-notebook.layout :as layout]
            [csound-notebook.db.core :as db]
            [compojure.core :refer [defroutes GET POST context]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [buddy.hashers :as hashers]
            [buddy.auth :refer [authenticated?]]
            ))

(defn register-page [req]
  (if (authenticated? req)
    (response/found "/")
    (layout/render "register.html")))

(defn login-page [req]
  (if (authenticated? req)
    (response/found "/")
    (layout/render "login.html")))

(defn reset-page []
  (layout/render "reset.html"))


(defn handle-login [{:keys [session form-params] :as req}]
  (if-let [user (db/get-user {:email (form-params "email")})]
    (-> (response/found "/")
      (assoc :session (assoc session :identity user)))
    (layout/render "login.html")))

(defn handle-logout [{session :session}]
  (-> (response/found "/")
      (assoc :session (dissoc session :identity))))

(defn handle-register [{session :session :as req}]
  (let [params (:params req)]
     
    )
  (-> (response/found "/")
      (assoc :session (assoc session :identity))))

(defroutes user-routes
  (context 
    "/user" []
    (GET "/login" req 
         (login-page req))

    (GET "/logout" req 
         (handle-logout req))

    (POST "/login" req 
          (handle-login req))

    (GET "/register" req 
         (register-page req))

    (POST "/register" req
          (handle-register req))

    (GET "/reset" []
         (reset-page))
    ))
