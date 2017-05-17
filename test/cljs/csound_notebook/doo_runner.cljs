(ns csound-notebook.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [csound-notebook.core-test]))

(doo-tests 'csound-notebook.core-test)

