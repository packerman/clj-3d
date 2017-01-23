(ns clj-3d.example.simple-scene
  (:gen-class)
  (:require [clj-3d.engine.color :as color]
            [clj-3d.application :as application]
            [clj-3d.example.common :as common]))

(defn rectangle-vertex-array [x-min y-min x-max y-max]
  [[x-min y-max] [x-min y-min] [x-max y-min]
   [x-min y-max] [x-max y-min] [x-max y-max]])

(defn circle-vertex-array
  ([[center-x center-y :as center] radius n]
   (->>
     (iterate #(+ (/ (* 2 Math/PI) n) %) 0)
     (take (inc n))
     (map #(vector (+ center-x (* radius (Math/cos %)))
                   (+ center-y (* radius (Math/sin %)))))
     (partition 2 1)
     (map (fn [[p1 p2]] (list center p1 p2)))
     (mapcat identity)))
  ([center radius] (circle-vertex-array center radius 96)))

(defn polygon-vertex-array [points]
  (let [[first-point & rest-points] points]
    (->>
      (for [[p1 p2] (partition 2 1 rest-points)]
        (list first-point p1 p2))
      (mapcat identity))))

(def scene [{:vertex-array [[-0.7 0.9] [-0.9 0.1] [-0.1 0.1]]
             :color        color/orange}
            {:vertex-array (rectangle-vertex-array 0.1 0.1 0.9 0.9)
             :color        color/lime}
            {:vertex-array (circle-vertex-array [-0.5 -0.5] 0.4)
             :color color/red}
            {:vertex-array (polygon-vertex-array [[0.1 -0.4] [0.1 -0.9] [0.9 -0.9] [0.9 -0.4] [0.5 -0.1]])
             :color color/yellow}])

(def app
  (common/make-application-for-scene scene))

(defn -main
  [& args]
  (application/launch app))
