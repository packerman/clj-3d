(ns clj-3d.engine.geometry
  (:require [clj-3d.engine.util.nio-buffer :as nio-buffer]
            [medley.core :refer :all]
            [clojure.tools.logging :as log]
            [clj-3d.engine.util.error :refer :all]
            [medley.core :refer :all])
  (:import (com.jogamp.opengl GL4 GL GL2ES2)))

(def primitive->mode {:triangles GL/GL_TRIANGLES
                      :lines     GL/GL_LINES
                      :triangle-strip GL/GL_TRIANGLE_STRIP})

(defn- gl-gen-buffers [^GL gl n]
  (let [buffers (int-array n)]
    (.glGenBuffers gl n buffers 0)
    buffers))

(defn- prepare-geometry-spec [geometry]
  (if-let [index-array (:index-array geometry)]
    (assoc geometry :index-arrays {0 index-array})
    geometry))

(defn- allocate-nio-buffers-if-needed [geometry]
  (letfn [(create-vertex-nio-buffer-if-needed [n]
            (update n :vertex-arrays (fn [vertex-arrays]
                                       (map-vals (fn [data]
                                                   (if (nio-buffer/is-nio-buffer? data)
                                                     data
                                                     (nio-buffer/float-buffer data)))
                                                 vertex-arrays))))
          (create-index-nio-buffer-if-needed [n]
            (update n :index-arrays (fn [index-arrays]
                                      (map-vals (fn [data]
                                                  (cond
                                                    (not data) data
                                                    (nio-buffer/is-nio-buffer? data) data
                                                    :else (nio-buffer/int-buffer data)))
                                                index-arrays))))]
    (-> geometry
        create-vertex-nio-buffer-if-needed
        create-index-nio-buffer-if-needed)))

(defn create-geometry [^GL2ES2 gl geometry-spec]
  (letfn [(array-offsets [arrays-map]
            (reduce (fn [result key-array-pair]
                      (let [[key array] key-array-pair]
                        (-> (assoc-in result [:offsets key] (:total-size result))
                            (update :total-size + (:byte-size array)))))
                    {:total-size 0
                     :offsets    {}}
                    arrays-map))
          (copy-arrays-to-buffer [^GL4 gl target arrays offsets]
            (doseq [[key array] arrays]
              (.glBufferSubData gl target
                                (get-in offsets [:offsets key])
                                (:byte-size array)
                                (:data array))))
          (vertex-arrays-count [vertex-arrays]
            (let [counts (->> (vals vertex-arrays) (map :count))]
              (assert (apply = counts))
              (first counts)))
          (create-buffer [target total-size]
            (let [^ints buffer-ids (gl-gen-buffers gl 1)]
              (.glBindBuffer gl target (aget buffer-ids 0))
              (.glBufferData gl target total-size nil GL4/GL_STATIC_DRAW)
              buffer-ids))
          (deref-data [arrays]
            (map-vals #(dissoc % :data) arrays))]
    (let [{:keys [vertex-arrays index-arrays]} (allocate-nio-buffers-if-needed
                                                 (prepare-geometry-spec
                                                   geometry-spec))
          vertex-offsets (array-offsets vertex-arrays)
          vbos (create-buffer GL4/GL_ARRAY_BUFFER (:total-size vertex-offsets))
          base-object {:vbos           vbos
                       :mode           (primitive->mode
                                         (get geometry-spec :primitive :triangles))
                       :vertex-arrays  (deref-data vertex-arrays)
                       :vertex-offsets vertex-offsets}]
      (copy-arrays-to-buffer gl GL4/GL_ARRAY_BUFFER vertex-arrays vertex-offsets)
      (if (not (empty? index-arrays))
        (let [index-offsets (array-offsets index-arrays)
              ibos (create-buffer GL4/GL_ELEMENT_ARRAY_BUFFER (:total-size index-offsets))]
          (copy-arrays-to-buffer gl GL4/GL_ELEMENT_ARRAY_BUFFER index-arrays index-offsets)
          (assoc base-object
            :ibos ibos
            :index-arrays (deref-data index-arrays)
            :index-offsets index-offsets))
        (assoc base-object
          :count (vertex-arrays-count vertex-arrays))))))

(defn bind-attributes [^GL2ES2 gl geometry program]
  (let [{:keys [vertex-arrays vertex-offsets ^ints vbos]} geometry]
    (.glBindBuffer gl GL2ES2/GL_ARRAY_BUFFER (aget vbos 0))
    (doseq [[attribute location] (get-in program [:locations :attributes])
            :let [vertex-array (vertex-arrays attribute)]]
      (.glEnableVertexAttribArray gl location)
      (.glVertexAttribPointer gl location (:dimension vertex-array) (:type vertex-array) false 0
                              (get-in vertex-offsets [:offsets attribute])))
    (.glBindBuffer gl GL2ES2/GL_ARRAY_BUFFER 0)))

(defn has-index-arrays? [geometry]
  (not (empty? (:index-arrays geometry))))

(defn bind-index-array [^GL2ES2 gl geometry]
  (let [{:keys [^ints ibos]} geometry]
    (.glBindBuffer gl GL4/GL_ELEMENT_ARRAY_BUFFER (aget ibos 0))))

(defn draw-index-array [^GL2ES2 gl geometry index]
  (let [{:keys [mode index-arrays index-offsets]} geometry
        index-array (get index-arrays index)]
    (.glDrawElements gl mode (:count index-array) (:type index-array)
                     (get-in index-offsets [:offsets index]))))

(defn draw-vertex-array [^GL2ES2 gl geometry]
  (let [{:keys [mode count]} geometry]
    (.glDrawArrays gl mode 0 count)))

(defn dispose-geometry [^GL2ES2 gl geometry]
  (let [{:keys [vbos ibos]} geometry]
    (.glDeleteBuffers gl 1 vbos 0)
    (when ibos
      (.glDeleteBuffers gl 1 ibos 0))))
