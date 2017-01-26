(ns clj-3d.example.mesh-lines
  (:require [clj-3d.application :as application]
            [clj-3d.example.common :as common]
            [clj-3d.engine.color :as color]
            [clj-3d.engine.transform :refer [translation axis-rotation scale]]))

(defn grid-vertex-array [m n]
  (letfn [(steps [k a b]
            {:pre [(> k 1)]}
            (let [step (/ (- b a) (dec k))]
              (->>
                (iterate #(+ % step) a)
                (take k))))]
    (let [[x-min y-min x-max y-max] [-0.5 -0.5 0.5 0.5]]
      (mapcat identity
              (-> []
                  (into (map #(list [% y-min] [% y-max]) (steps m x-min x-max)))
                  (into (map #(list [x-min %] [x-max %]) (steps n y-min y-max))))))))

(def scene [{:mesh       {:primitive    :lines
                          :vertex-array (grid-vertex-array 20 10)}
             :color      color/white
             :transforms [(scale 1.8 1.8 1)]}])

(def app
  (common/make-application-for-scene scene {:clear-color color/dim-gray}))

(defn -main
  [& args]
  (application/launch app {:fullscreen true}))
