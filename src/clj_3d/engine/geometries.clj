(ns clj-3d.engine.geometries)

(defn plane-geomentry
  ([width height width-segments height-segments]
   (let [x-min (- (/ width 2.0)) x-max (/ width 2.0)
         z-min (- (/ height 2.0)) z-max (/ height 2.0)]
     {:primitive :triangle-strip
      :vertex-arrays {
                      "position" (for [j (range (inc height-segments))
                                       i (range (inc width-segments))]
                                   [(+ (* x-min (- 1.0 (/ i width-segments))) (* x-max (/ i width-segments)))
                                    0.0
                                    (+ (* z-min (- 1.0 (/ j height-segments))) (* z-max (/ j height-segments)))])
                      "normal" (for [j (range (inc height-segments))
                                     i (range (inc width-segments))]
                                 [0.0 1.0 0.0])
                      }
      :index-array (let [index (fn [i j] (+ (* j (inc width-segments)) i))
                         strips (for [j (range height-segments)]
                                  (interleave (range (index 0 j) (inc (index width-segments j)) )
                                              (range (index 0 (inc j)) (inc (index width-segments (inc j))))))
                         inter-strips (for [j (range (dec height-segments))]
                                        [(index width-segments (inc j)) (index 0 (inc j))])]
                     (concat
                       (apply concat (interleave strips inter-strips))
                       (last strips)))}
     ))
  ([width height] (plane-geomentry width height 1 1)))
