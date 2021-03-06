(ns clj-3d.example.transforms
  (:require [clj-3d.example.common :as common]
            [clj-3d.engine.color :as color]
            [clj-3d.engine.transform :refer [translation axis-rotation scale]]
            [clj-3d.application :as application]))

(def square-vertex-array
  [[-0.5 0.5] [-0.5 -0.5] [0.5 -0.5]
   [-0.5 0.5] [0.5 -0.5] [0.5 0.5]])

(def scene {
            :geometries {
                         "square" (common/simple-geometry square-vertex-array)
                         }
            :nodes      [{:geometry   "square"
                          :color      color/orange
                          :transforms [(translation -0.5 0.5 0)
                                       (axis-rotation (Math/toRadians 30.0) :z)
                                       (scale 0.4 0.2 1)]}
                         {:geometry   "square"
                          :color      color/lime
                          :transforms [(translation 0.5 0.5 0)
                                       (axis-rotation (Math/toRadians -30.0) :z)
                                       (scale 0.4 0.2 1)]}
                         {:geometry   "square"
                          :color      color/red
                          :transforms [(translation -0.5 -0.5 0)
                                       (axis-rotation (Math/toRadians -30.0) :z)
                                       (scale 0.4 0.2 1)]}
                         {:geometry   "square"
                          :color      color/yellow
                          :transforms [(translation 0.5 -0.5 0)
                                       (axis-rotation (Math/toRadians 30.0) :z)
                                       (scale 0.4 0.2 1)]}]
            })

(def app
  (common/make-application-for-scene scene))

(defn -main
  [& args]
  (application/launch app))
