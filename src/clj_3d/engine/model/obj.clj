(ns clj-3d.engine.model.obj
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [medley.core :refer :all]
            [clj-3d.engine.util.math :as math]))

(defn read-obj [source]
  (letfn [(parse-face-element [face-element]
            (let [elems (string/split face-element #"/")
                  [position texture normal] elems]
              {:position (dec (Integer/parseInt position))
               :texture  (and (not (empty? texture)) (dec (Integer/parseInt texture)))
               :normal   (and normal (dec (Integer/parseInt normal)))}))
          (read-line [model line]
            (let [[command & args] (string/split line #" ")]
              (condp = command
                "v" (update model :positions conj (vec (map #(Float/parseFloat %) args)))
                "vn" (update model :normals conj (vec (map #(Float/parseFloat %) args)))
                "f" (update model :faces conj (vec (map parse-face-element args)))
                (do (log-unknown-command command) model))))
          (log-unknown-command [command]
            (log/warn "Unknown command: " command))
          (comment-line? [^String line] (.startsWith line "#"))]
    (let [reader (io/reader source)]
      (reduce read-line
              {:positions []
               :normals   []
               :faces     []}
              (->> (line-seq reader)
                   (remove comment-line?))))))

(defn map-seq-of-seqs [f seqs]
  "Map sequence of sequences. Preserves original structure"
  (map #(map f %) seqs))

(defn add-smooth-normals [obj-file]
  (letfn [(calculate-smooth-normals []
            "creates map vert index -> normal when normal is average of normals of every triangle"
            (letfn [(update-vertex-normal [map i normal]
                      (update map i (fnil math/vec3-add math/vec3-zero) normal))]
              (let [{:keys [positions faces]} obj-file]
                (->>
                  (reduce
                    (fn [vertices->normal [i j k]]
                      (let [normal (math/normal-to-triangle (get positions (:position i))
                                                            (get positions (:position j))
                                                            (get positions (:position k)))]
                        (-> vertices->normal
                            (update-vertex-normal (:position i) normal)
                            (update-vertex-normal (:position j) normal)
                            (update-vertex-normal (:position k) normal))))
                    {}
                    faces)
                  (map-vals math/vec3-normalize)))))
          (serialize-map-to-vector [m]
            "serialize map {0 -> v_0, ..., n -> v_n} to vector
            assuming that map contains all keys from 0 to n"
            (->> (range (count m))
                 (map m)
                 vec))]
    (let [position->normal (calculate-smooth-normals)]
      (-> obj-file
          (assoc :normals (serialize-map-to-vector position->normal))
          (update :faces #(map-seq-of-seqs (fn [face-elem]
                                             (assoc face-elem :normal (:position face-elem))) %))))))

(defn obj-file->geometry [obj-file]
  (letfn [(vector-to-map [v]
            "Converts vector [a b ...] to map {a -> 0, b -> 1, ...}."
            (zipmap v (iterate inc 0)))]
    (if (empty? (:normals obj-file))
      (obj-file->geometry (add-smooth-normals obj-file))
      (let [face-elements (distinct (mapcat identity (:faces obj-file)))
            face-elem->idx (vector-to-map face-elements)
            index-array (map-seq-of-seqs face-elem->idx (:faces obj-file))
            {:keys [positions normals]} obj-file]
        {:vertex-arrays (reduce
                          (fn [vertex-arrays face]
                            (-> vertex-arrays
                                (update "position" conj (get positions (:position face)))
                                (update "normal" conj (get normals (:normal face)))))
                          {"position" []
                           "normal"   []}
                          face-elements)
         :index-array   index-array}))))

(defn load-obj-geometry [source]
  (obj-file->geometry
    (read-obj source)))
