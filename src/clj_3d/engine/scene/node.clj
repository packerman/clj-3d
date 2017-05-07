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
              :else (error "No materials defined")))]
    (-> node
        prepare-materials)))
