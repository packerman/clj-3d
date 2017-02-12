(ns clj-3d.example.triangle
  (:gen-class)
  (:require [clj-3d.engine.color :as color]
            [clj-3d.application :as application]
            [clj-3d.example.common :as common]))

(def scene {
            :geometries {
                         "triangle" (common/simple-geometry [[0 0.5]
                                                             [-0.5 -0.5]
                                                             [0.5 -0.5]])}
            :nodes      [{:geometry "triangle"
                          :color    (color/to-rgba-float color/spring-green)}]
            })

(def app
  (common/make-application-for-scene scene))

(defn -main
  [& args]
  (application/launch app))
