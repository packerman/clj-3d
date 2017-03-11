(ns clj-3d.engine.scene.node
  (:require [medley.core :refer :all]
            [clj-3d.engine.color :as color]
            [clj-3d.engine.util.error :refer :all]
            [clj-3d.engine.util.nio-buffer :as nio-buffer]))

(defn prepare-node [node]
  (letfn [(prepare-materials [n]
            (cond
              (contains? n :materials) n
              (and (not (contains? n :materials)) (contains? n :material)) (assoc n
                                                                             :materials
                                                                             {0 (:material n)})
              (and (not (contains? n :materials)) (contains? n :color)) (assoc n :materials
                                                                                 {0 {:colors {:ambient (:color n)}}})
              :else (error "No materials defined")))
          (convert-color-material [material]
            (if-not (and material (associative? material))
              material
              (-> material
                  (update-in [:colors :ambient] (fn [color]
                                                  (when color (convert-to-rgba color))))
                  (update-in [:colors :diffuse] (fn [color]
                                                  (when color (convert-to-rgba color)))))))
          (convert-colors [n]
            (update n :materials (fn [materials]
                                   (map-vals convert-color-material materials))))
          (convert-to-rgba [color]
            (if (vector? color)
              color
              (color/to-rgba-float color)))]
    (-> node
         prepare-materials
         convert-colors)))
