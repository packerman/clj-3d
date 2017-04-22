(ns clj-3d.engine.util.math)

(def vec3-zero [0 0 0])

(defn vec3-add [a b]
  [(+ (a 0) (b 0))
   (+ (a 1) (b 1))
   (+ (a 2) (b 2))])

(defn vec3-sub [a b]
  [(- (a 0) (b 0))
   (- (a 1) (b 1))
   (- (a 2) (b 2))])

(defn vec3-normalize [a]
  (let [[x y z] a
        l (Math/sqrt (+ (* x x) (* y y) (* z z)))]
    [(/ x l) (/ y l) (/ z l)]))

(defn vec3-cross [a b]
  [(- (* (a 1) (b 2)) (* (a 2) (b 1)))
   (- (* (a 2) (b 0)) (* (a 0) (b 2)))
   (- (* (a 0) (b 1)) (* (a 1) (b 0)))])

(defn normal-to-triangle [a b c]
  (vec3-normalize
    (vec3-cross
      (vec3-sub b a)
      (vec3-sub c a))))
