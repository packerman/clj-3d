(ns clj-3d.example.many-lights
  (:require [clj-3d.engine.color :as color]
            [clj-3d.engine.transform :as transform]
            [clj-3d.engine.geometries :as geometries]
            [clj-3d.application :as application]
            [clj-3d.example.common :as common])
  (:gen-class))

(def scene {
            :geometries {
                         "plane" (geometries/plane-geomentry 10.0 10.0 10 10)

                         }
            :nodes      [{:geometry   "plane"
                          :material   {:colors         {:ambient  color/black
                                                        :diffuse  color/dark-green
                                                        :specular color/white}
                                       :specular-power 10.0
                                       :smooth         true}
                          :transforms []}]
            :lights     [{
                          :position [0 1 -7]
                          :color    color/white}]
            })

(def camera (transform/perspective-camera {:fovy (Math/toRadians 60.0)
                                           :near 0.1
                                           :far  20.00}
                                          {:eye    [0 1.5 7]
                                           :center [0 0 0]
                                           :up     [0 1 0]}))

(def app
  (common/make-application-for-scene scene camera {:clear-color color/deep-sky-blue}))

(defn -main
  [& args]
  (application/launch app {:width 1280 :height 800}))
