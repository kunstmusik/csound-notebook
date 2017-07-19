(ns csound-notebook.routes.user
  (:require [csound-notebook.layout :as layout]
            [csound-notebook.db.core :as db]
            [csound-notebook.config :refer [env]]
            [compojure.core :refer [defroutes GET POST context]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [buddy.hashers :as hashers]
            [buddy.auth :refer [authenticated?]]
            [postal.core :refer [send-message]]
            [clojure.spec.alpha :as s]
            [clojure.string :refer [trim]]
            ))

;; SPECS

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def ::email (s/and string? #(re-matches email-regex %)))
(s/def ::not-empty (s/and #(not (nil? %)) 
                          string?
                          #(pos? (.length %))))
(s/def ::no-spaces #(not (.contains % " ")))
(s/def ::valid-str (s/and ::not-empty ::no-spaces))


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

(defn send-reset-email! [email]
  (when-let [smtp (env :smtp)] 
    (send-message 
      smtp
      {:to email 
       :from "noreply@csound-notebook.kunstmusik.com"
       :subject "Password Reset"
       :body "Email reset code."
       })))

(def flash-map
  {:info "info"
   :success "success"
   :warning "warning"
   :error "danger" })

(defn add-flash
  [req flash-type message]
  (assoc req :flash
         {:alert-message message
          :alert-type (flash-map flash-type) }))

(defn handle-reset [{:keys [form-params] :as req}]
  (if (authenticated? req)
    (response/found "/")
    (if-let [user (db/get-user {:email (form-params "email")})]
      (do
        (send-reset-email! (form-params "email")) 
        (->
          (response/found "/user/login")
          (assoc :flash {:alert-message "Please check your email."
                         :alert-type "success" 
                         })))
      (->
        (layout/render "reset.html"
                       {:alert-message "Error: no account found with that email."})
        ))))


(defn handle-login [{:keys [session form-params] :as req}]
  (let [user (db/get-user {:email (form-params "email")})]
    (if (and user 
             (hashers/check (form-params "password") (:pass user)))
      (assoc (response/found "/")
             :session (assoc session :identity user))
      (layout/render "login.html" {:alert-message "Invalid login. Please try again."
                                   :alert-type (flash-map :error)}))))

(defn handle-logout [{session :session}]
  (-> (response/found "/")
      (assoc :session (dissoc session :identity))))

(defn trim-or-nil [s]
  (when (string? s)
    (trim s)))

;; REGISTRATION

(defn invalid-registration? 
  "Returns an error message if registration form parameters are invalid."
  [form-params]
  (let [email (trim-or-nil (form-params "email"))
        username (trim-or-nil (form-params "username"))
        pass (trim-or-nil (form-params "pass"))
        confirm (trim-or-nil (form-params "confirm"))]
   (cond
     (not (s/valid? ::email email))  
     "Invalid email address."
     (not (s/valid? ::valid-str username))
     "Username must not be empty or have any spaces."
     (not (s/valid? ::valid-str pass))
     "Password must not be empty or have any spaces."
     (not= pass confirm)
     "Password and confirmation password do not match."
     (db/get-user {:email email})
     "There is a user already registered with that email address."
     (db/get-user-by-username {:username username})
     "There is a user already registered with that username."
     )))

(defn handle-register [{:keys [session form-params] :as req}]
  (if-let [err (invalid-registration? form-params)]
    (layout/render "register.html" 
                   {:alert-message (str "Error: " err)
                    :alert-type (flash-map :error)})
    (let [user {:email (form-params "email")
                :pass (hashers/encrypt (form-params "password"))
                :username (form-params "username")}]
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
