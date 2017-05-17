(ns ^:figwheel-no-load csound-notebook.app
  (:require [csound-notebook.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
