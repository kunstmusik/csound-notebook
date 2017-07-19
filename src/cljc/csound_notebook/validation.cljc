(ns csound-notebook.validation
  (:require [struct.core :as st]))


(def registration-schema
  [["email" st/required st/email]
   ["user" st/required st/string]
   ["password" st/required st/string]
   ["confirm" st/required st/string]
   ]
  )

