(ns clj-3d.example.common
  (:require [clj-3d.engine.render :as render])
  (:import (clj_3d.application Application)))

(defn make-application-for-scene [gl scene]
  (let [object (render/create-render-object gl scene)]
    (reify Application
      (render [this gl]
        (render/render gl object))
      (resize [this gl x y width height])
      (dispose [this gl]
        (render/dispose! gl object)))))
