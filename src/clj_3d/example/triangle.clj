(ns clj-3d.example.triangle
  (:require [clj-3d.render :as render]
            [clj-3d.color :as color])
  (:import (clj_3d.application Application)))

(def vertices [[0 0.5]
               [-0.5 -0.5]
               [0.5 -0.5]])

(def color (color/to-rgba-float color/spring-green))

(defn make-triangle [gl]
  (let [object (render/create-object gl vertices color)]
    (reify Application
      (render [_ gl]
        (render/draw-object gl object))
      (resize [_ gl x y width height])
      (dispose [_ gl]
        (render/dispose-object gl object)))))