(ns clj-3d.example.many-lights
  (:require [clj-3d.engine.color :as color]
            [clj-3d.engine.transform :as transform]
            [clj-3d.engine.geometries :as geometries]
            [clj-3d.application :as application]
            [clj-3d.example.common :as common])
  (:gen-class))

(def scene {
            :geometries {
                         "plane" (geometries/plane-geomentry 20.0 20.0 10 10)

                         }
            :nodes      [{:geometry   "plane"
                          :material   {:colors         {:ambient  color/black
                                                        :diffuse  color/dark-green
                                                        :specular color/white}
                                       :specular-power 100.0
                                       :smooth         true}
                          :transforms []}]
            :lights     [{:position [-8 0.5 -16]
                          :color    color/white}
                         {:position [8 0.5 -16]
                          :color    color/yellow}
                         {:position [-8 0.5 -32]
                          :color    color/red}
                         {:position [8 0.5 -32]
                          :color    color/blue}]
            })

(def camera (transform/perspective-camera {:fovy (Math/toRadians 60.0)
                                           :near 0.1
                                           :far  100.00}
                                          {:eye    [0 2.5 15.0]
                                           :center [0 2.5 0]
                                           :up     [0 1 0]}))

(def app
  (common/make-application-for-scene scene camera {:clear-color color/deep-sky-blue}))

(defn -main
  [& args]
  (application/launch app {:width 1280 :height 800}))
