(ns clj-3d.engine.transform
  (:import (com.jogamp.opengl.math FloatUtil Quaternion)))

(defn make-identity []
  (let [m (float-array 16)]
    (FloatUtil/makeIdentity m)))

(defn make-translation
  ([type d]
   (condp type =
     :x (make-translation [d 0 0])
     :y (make-translation [0 d 0])
     :z (make-translation [0 0 d])))
  ([[dx dy dz]]
   (let [m (float-array 16)]
     (FloatUtil/makeTranslation m true dx dy dz))))

(defn make-rotation [type value]
  (letfn [(make-from-axis [angle x y z]
            (let [matrix (float-array 16)
                  tmp-vector (float-array 3)]
              (FloatUtil/makeRotationAxis matrix 0 angle x y z tmp-vector)))
          (make-from-quaternion []
            (let [matrix (float-array 16)
                  [x y z w] value
                  q (-> (Quaternion. x y z w) .normalize)]
              (.toMatrix q matrix 0)))]
    (condp type =
      :x (make-from-axis value 1 0 0)
      :y (make-from-axis value 0 1 0)
      :z (make-from-axis value 0 0 1)
      :axis (let [[angle x y z] value]
              (make-from-axis angle x y z))
      :quaternion (make-from-quaternion))))

(defn make-scale
  ([type s]
   (condp type =
     :x (make-scale [s 1 1])
     :y (make-scale [1 s 1])
     :z (make-scale [1 1 s])))
  ([[sx sy sz]]
   (let [m (float-array 16)]
     (FloatUtil/makeScale m true sx sy sz))))

(defn multiply [transforms]
  (let [^floats m (make-identity)]
    (doseq [^floats t transforms]
      (FloatUtil/multMatrix m t))
    m))
