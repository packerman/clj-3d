(ns clj-3d.example.camera
  (:require [clj-3d.example.mesh-lines :as mesh-lines]
            [clj-3d.application :as application]
            [clj-3d.example.common :as common]
            [clj-3d.engine.color :as color]
            [clj-3d.engine.transform :as transform :refer [translation axis-rotation scale]]))

(def scene [{:mesh       {:primitive    :lines
                          :vertex-array (mesh-lines/grid-vertex-array 8 5)}
             :color      color/white
             :transforms [(scale 1.6 1.6 1)]}])

(def camera (transform/perspective-camera {:fovy (Math/toRadians 45.0)
                                           :near 1
                                           :far  100.00}
                                          {:eye    [0 0 -1]
                                           :center [0 0 0]
                                           :up     [0 1 0]}))

(def app
  (common/make-application-for-scene scene))

(defn -main
  [& args]
  (application/launch app))
