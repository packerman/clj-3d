(ns clj-3d.example.cube-from-grids
  (:gen-class)
  (:require [clj-3d.application :as application]
            [clj-3d.example.common :as common]
            [clj-3d.engine.color :as color]
            [clj-3d.engine.transform :refer [translation axis-rotation scale]]
            [clj-3d.engine.transform :as transform]))

(def grid (common/grid-vertex-array 6 6))

(def scene [
            ;back
            {:mesh       {:primitive    :lines
                          :vertex-array grid}
             :color      color/lime
             :transforms []}
            ;left
            {:mesh       {:primitive    :lines
                          :vertex-array grid}
             :color      color/lime
             :transforms [(translation -0.5 0 0.5)
                          (axis-rotation (Math/toRadians 90) :y)]}
            ;right
            {:mesh       {:primitive    :lines
                          :vertex-array grid}
             :color      color/lime
             :transforms [(translation 0.5 0 0.5)
                          (axis-rotation (Math/toRadians 90) :y)]}
            ;top
            {:mesh       {:primitive    :lines
                          :vertex-array grid}
             :color      color/lime
             :transforms [(translation 0.0 0.5 0.5)
                          (axis-rotation (Math/toRadians 90) :x)]}
            ;down
            {:mesh       {:primitive    :lines
                          :vertex-array grid}
             :color      color/lime
             :transforms [(translation 0.0 -0.5 0.5)
                          (axis-rotation (Math/toRadians 90) :x)]}
            ])

(def camera (transform/perspective-camera {:fovy (Math/toRadians 45.0)
                                           :near 0.1
                                           :far  100.00}
                                          {:eye    [0 0 1.8]
                                           :center [0 0 0]
                                           :up     [0 1 0]}))

(def app
  (common/make-application-for-scene scene camera {:clear-color color/black}))

(defn -main
  [& args]
  (application/launch app))
