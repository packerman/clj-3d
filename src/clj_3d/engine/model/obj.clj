(ns clj-3d.engine.model.obj
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [medley.core :refer :all]
            [clj-3d.engine.util.math :as math]))

(defn read-obj [source]
  (letfn [(read-line [model line]
            (let [[command & args] (string/split line #" ")]
              (condp = command
                "v" (update model :vertices conj (vec (map #(Float/parseFloat %) args)))
                "f" (update model :faces conj (vec (map #(dec (Integer/parseInt %)) args)))
                (do (log-unknown-command command) model))))
          (log-unknown-command [command]
            (log/warn "Unknown command: " command))
          (comment-line? [^String line] (.startsWith line "#"))]
    (let [reader (io/reader source)]
      (reduce read-line
              {:vertices []
               :faces    []}
              (->> (line-seq reader)
                   (remove comment-line?))))))

; creates map vert index -> normal when normal is average of normals of every triangle
(defn- calculate-smooth-normals [obj-file]
  (letfn [(update-vertex-normal [map i normal]
            (update map i (fnil math/vec3-add math/vec3-zero) normal))]
    (let [{:keys [vertices faces]} obj-file]
      (->>
        (reduce
          (fn [vertices->normal [i j k]]
            (let [normal (math/normal-to-triangle (vertices i)
                                                  (vertices j)
                                                  (vertices k))]
              (-> vertices->normal
                  (update-vertex-normal i normal)
                  (update-vertex-normal j normal)
                  (update-vertex-normal k normal))))
          {}
          faces)
        (map-vals math/vec3-normalize)))))

; serialize map {0 -> v_0, ..., n -> v_n} to vector
; assuming that map contains all keys from 0 to n
(defn serialize-map-to-vector [m]
  (->> (range (count m))
       (map m)
       (reduce conj [])))

(defn obj-file->geometry [obj-file]
  {:vertex-arrays {
                   "position" (:vertices obj-file)
                   "normal" (-> (calculate-smooth-normals obj-file)
                                serialize-map-to-vector)}
   :index-array (:faces obj-file)})

(defn load-obj-geometry [source]
  (obj-file->geometry
    (read-obj source)))
