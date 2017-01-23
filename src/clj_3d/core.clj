(ns clj-3d.core
  (:gen-class)
  (:require [clj-3d.application :as application]
            [clj-3d.example.triangle :as triangle]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (application/launch triangle/app))
