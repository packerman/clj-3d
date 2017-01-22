(ns clj-3d.example.triangle
  (:gen-class)
  (:require [clj-3d.engine.color :as color]
            [clj-3d.application :as application]
            [clj-3d.example.common :as common]))

(def triangle {:vertex-array [[0 0.5]
                             [-0.5 -0.5]
                             [0.5 -0.5]]
              :color (color/to-rgba-float color/spring-green)})

(defn make-triangle [gl]
  (common/make-application-for-scene gl [triangle]))

(defn -main
  [& args]
  (application/launch make-triangle))
