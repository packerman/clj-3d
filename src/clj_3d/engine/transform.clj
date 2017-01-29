(ns clj-3d.engine.transform
  (:import (com.jogamp.opengl.math FloatUtil Quaternion)))

(defn make-identity []
  (let [m (float-array 16)]
    (FloatUtil/makeIdentity m)))

(defn translation
  ([dx dy dz]
   (let [m (float-array 16)]
     (FloatUtil/makeTranslation m true dx dy dz)))
  ([d axis]
   (condp axis =
     :x (translation d 0 0)
     :y (translation 0 d 0)
     :z (translation 0 0 d))))

(defn axis-rotation [angle axis]
  (let [matrix (float-array 16)
        tmp-vector (float-array 3)]
    (condp = axis
      :x (FloatUtil/makeRotationAxis matrix 0 angle 1 0 0 tmp-vector)
      :y (FloatUtil/makeRotationAxis matrix 0 angle 0 1 0 tmp-vector)
      :z (FloatUtil/makeRotationAxis matrix 0 angle 0 0 1 tmp-vector)
      (let [[x y z] axis]
        (FloatUtil/makeRotationAxis matrix 0 angle x y z tmp-vector)))))

(defn quaternion-rotation [x y z w]
  (let [matrix (float-array 16)
        q (-> (Quaternion. x y z w) .normalize)]
    (.toMatrix q matrix 0)))

(defn scale
  ([sx sy sz]
   (let [m (float-array 16)]
     (FloatUtil/makeScale m true sx sy sz)))
  ([s axis]
   (condp = axis
     :x (scale s 1 1)
     :y (scale 1 s 1)
     :z (scale 1 1 s))))

(defn multiply
  ([] (make-identity))
  ([m] m)
  ([^floats m ^floats n] (let [^floats p (float-array 16)]
                           (FloatUtil/multMatrix m n p)))
  ([m n & more] (let [^floats result (multiply m n)]
                  (doseq [^floats t more]
                    (FloatUtil/multMatrix result t))
                  result)))

(defn multiply* [ms]
  (apply multiply ms))

(defn perspective-camera [params look-at]
  (let [projection-matrix (float-array 16)
        view-matrix (float-array 16)
        camera {:type :perspective
                :params            params
                :look-at           look-at
                :projection-matrix projection-matrix
                :view-matrix       view-matrix}
        tmp-matrix (float-array 16)]
    (FloatUtil/makeIdentity projection-matrix)
    (FloatUtil/makeLookAt view-matrix 0
                          (float-array (:eye look-at)) 0
                          (float-array (:center look-at)) 0
                          (float-array (:up look-at)) 0
                          tmp-matrix)
    camera))

(defn update-projection-matrix [camera aspect-ratio]
  (condp = (:type camera)
    :identity camera
    :perspective (let [projection-matrix (float-array 16)
                       {:keys [fovy near far]} (:params camera)]
                   (FloatUtil/makePerspective projection-matrix 0 true fovy aspect-ratio near far)
                   (assoc camera :projection-matrix projection-matrix))))

(defn get-projection-view-matrix [{:keys [projection-matrix view-matrix]}]
  (multiply projection-matrix view-matrix))

(defn identity-camera []
  (let [projection-matrix (make-identity)
        view-matrix (make-identity)]
    {:type :identity
     :projection-matrix projection-matrix
     :view-matrix view-matrix}))