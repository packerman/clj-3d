(ns clj-3d.example.obj-model
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clj-3d.engine.color :as color]
            [clj-3d.engine.transform :as transform]
            [clj-3d.application :as application]
            [clj-3d.example.common :as common]
    [clj-3d.engine.model.obj :as obj]))

(def scene {
            :geometries {
                         "monkey" (obj/load-obj-geometry (io/resource "model/obj/monkey.obj"))
                         }
            :nodes [{:geometry "monkey"
                     :material {:colors {:ambient (color/scale-color (color/to-rgba-float color/yellow) 0.4)
                                         :diffuse (color/to-rgba-float color/yellow)}}
                     :transforms []}]
            :lights [{
                      :position [2 3 -2]
                      :color    (color/to-rgba-float color/white)}]
            })

(def camera (transform/perspective-camera {:fovy (Math/toRadians 60.0)
                                           :near 0.1
                                           :far  100.00}
                                          {:eye    [0 1 4]
                                           :center [0 0 0]
                                           :up     [0 1 0]}))

(def app
  (common/make-application-for-scene scene camera))

(defn -main
  [& args]
  (application/launch app #_{:width 1280 :height 800}))
