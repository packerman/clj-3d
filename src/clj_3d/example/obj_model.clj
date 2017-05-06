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
                         "torus"  (obj/load-obj-geometry (io/resource "model/obj/torus_with_normals.obj"))
                         }
            :nodes      [{:geometry   "monkey"
                          :material   {:colors {:ambient (color/scale-color color/orange 0.4)
                                                :diffuse color/orange}}
                          :transforms []}
                         {:geometry   "torus"
                          :material   {:colors {:ambient (color/scale-color color/red 0.4)
                                                :diffuse color/red}}
                          :transforms [(transform/scale 1.5 1.5 1.5)
                                       (transform/translation -3 0 -2)
                                       (transform/axis-rotation (Math/toRadians 73) :x)]}]
            :lights     [{
                          :position [4 3 4]
                          :color    color/white}]
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
  (application/launch app {:width 1280 :height 800}))