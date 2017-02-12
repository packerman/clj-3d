(ns clj-3d.engine.scene.node
  (:require [clj-3d.engine.color :as color]
            [clj-3d.engine.util.nio-buffer :as nio-buffer]))

(defn prepare-node [node]
  (letfn [(add-material-if-needed [n]
            (if-not (contains? n :material)
              (-> n
                  (assoc :material {:colors {:ambient (convert-to-rgba (:color n))}})
                  (dissoc :color))
              n))
          (convert-colors-if-needed [n]
            (if-not (and (:material n) (associative? (:material n)))
              n
              (-> n
                  (update-in [:material :colors :ambient] (fn [color]
                                                            (when color (convert-to-rgba color))))
                  (update-in [:material :colors :diffuse] (fn [color]
                                                            (when color (convert-to-rgba color)))))))
          (convert-to-rgba [color]
            (if (vector? color)
              color
              (color/to-rgba-float color)))]
    (-> node
        add-material-if-needed
        convert-colors-if-needed)))
