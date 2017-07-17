(ns csound-notebook.routes.user
  (:require [csound-notebook.layout :as layout]
            [csound-notebook.db.core :as db]
            [compojure.core :refer [defroutes GET POST context]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [buddy.hashers :as hashers]
            [buddy.auth :refer [authenticated?]]
            [postal.core :refer [send-message]]
            ))

(def EMAIL-BASE
  {:from "noreply@csound-notebook.kunstmusik.com"
   :subject "Csound Notebook - Password Reset"
   :body "Please visit link to reset your password."
   })

(defn register-page [{:keys [flash] :as req}]
  (if (authenticated? req)
    (response/found "/")
    (layout/render "register.html"
                   (select-keys flash [:alert-message])
                   )))

(defn login-page [{:keys [flash] :as req}]
  (if (authenticated? req)
    (response/found "/")
    (layout/render "login.html"
                   (select-keys flash [:alert-message])
                   )))

(defn reset-page [req]
  (if (authenticated? req)
    (->
      (response/found "/"))
    (layout/render "reset.html")))


(defn handle-reset [{:keys [form-params] :as req}]
  (if (authenticated? req)
    (response/found "/")
    (if-let [user (db/get-user {:email (form-params "email")})]
      (do
        (send-message 
          {:to [(form-params "email")] 
                       :from "noreply@csound-notebook.kunstmusik.com"
                       :subject "Password Reset"
                       :body "Email reset code."
                       })
        (->
          (response/found "/user/login")
          (assoc :flash {:alert-message "Please check your email."})))
      (->
        (layout/render "reset.html"
                       {:alert-message "Error: no account found with that email."})
        ))))


(defn handle-login [{:keys [session form-params] :as req}]
  (if-let [user (db/get-user {:email (form-params "email")})]
    (if (hashers/check (form-params "password") (:pass user)) 
      (assoc (response/found "/")
             :session (assoc session :identity user))
      (layout/render "login.html"))
    (layout/render "login.html")))

(defn handle-logout [{session :session}]
  (-> (response/found "/")
      (assoc :session (dissoc session :identity))))


;; TODO - validation
(defn handle-register [{:keys [session form-params] :as req}]
  (if-let [user (db/get-user {:email (form-params "email")})]
    (layout/render "register.html" 
                   {:alert-message 
                    "Error: There is a user already registered with 
                    that email address."})
    (let [user {:email (form-params "email")
                :pass (hashers/encrypt (form-params "password"))
                :username (form-params "username")
                }]
      (db/create-user! user)
      (-> (response/found "/")
          (assoc :session (assoc session :identity user))))))

(defn user-page 
  [req username]
  (if-let [user (db/get-user-by-username {:username username} )]
    (let [editable (and (authenticated? req) 
                        (= username
                           (get-in req [:session :identity :username] false)))
          notes (if editable 
                  (db/get-notes-for-username {:username username})
                  (db/get-public-notes-for-username {:username username}))]
      (layout/render "user.html" {:username username 
                                  :editable editable 
                                  :notes notes
                                  :logged-in (str (authenticated? req))
                                  :identity (get-in req
                                                [:session :identity :username]
                                                "user") 
                                  }))
    (layout/render "user.html" {:username "Unknown User"
                                :notes []
                                :editable false
                                :logged-in (str (authenticated? req))
                                :identity (get-in req
                                                [:session :identity :username]
                                                "user") 
                                })))

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

    (GET "/reset" req 
         (reset-page req))

    (POST "/reset" req 
         (handle-reset req))

    (GET "/:user-id" [user-id :as req]
         (user-page req user-id) 
         )
    ))
