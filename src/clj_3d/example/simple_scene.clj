(ns clj-3d.example.simple-scene
  (:gen-class)
  (:require [clj-3d.engine.color :as color]
            [clj-3d.application :as application]
            [clj-3d.example.common :as common])
  (:import (clj_3d.application Application)))

(defn rectangle-vertex-array [x-min y-min x-max y-max]
  [[x-min y-max] [x-min y-min] [x-max y-min]
   [x-min y-max] [x-max y-min] [x-max y-max]])

(defn circle-vertex-array
  ([center-x center-y radius n]
   (letfn [(alpha [k]
             (/ (* 2 Math/PI k) n))
           (dx [alpha]
             (* radius (Math/cos alpha)))
           (dy [alpha]
             (* radius (Math/sin alpha)))]
     (->>
       (for [k (range n)]
         (list
           [center-x center-y]
           [(+ center-x (dx (alpha k))) (+ center-y (dy (alpha k)))]
           [(+ center-x (dx (alpha (inc k)))) (+ center-y (dy (alpha (inc k))))]))
       (mapcat identity))))
  ([center-x center-y radius] (circle-vertex-array center-x center-y radius 100)))

(def scene [{:vertex-array [[-0.7 0.9] [-0.9 0.1] [-0.1 0.1]]
             :color        color/orange}
            {:vertex-array (rectangle-vertex-array 0.1 0.1 0.9 0.9)
             :color        color/lime}
            {:vertex-array (circle-vertex-array -0.5 -0.5 0.4)
             :color color/red}
            ;{:vertex-array [] ; polygon
            ; :color color/yellow }
            ])

(defn make [gl]
  (common/make-application-for-scene gl scene))

(defn -main
  [& args]
  (application/launch make))
