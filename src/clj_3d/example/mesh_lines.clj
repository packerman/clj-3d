(ns clj-3d.example.mesh-lines
  (:gen-class)
  (:require [clj-3d.application :as application]
            [clj-3d.example.common :as common]
            [clj-3d.engine.color :as color]
            [clj-3d.engine.transform :refer [translation axis-rotation scale]]))

(def scene {:geometries {
                         "grid" (common/simple-geometry :lines (common/grid-vertex-array 8 5))
                         }
            :nodes      [{:geometry   "grid"
                          :color      color/white
                          :transforms [(scale 1.6 1.6 1)]}]})

(def app
  (common/make-application-for-scene scene))

(defn -main
  [& args]
  (application/launch app))
