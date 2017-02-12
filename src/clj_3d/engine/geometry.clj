(ns clj-3d.engine.geometry
  (:require [clj-3d.engine.util.nio-buffer :as nio-buffer]
            [clojure.tools.logging :as log])
  (:import (com.jogamp.opengl GL4 GL GL2ES2)))

(def primitive->mode {:triangles GL/GL_TRIANGLES
                      :lines     GL/GL_LINES})

(defn- gl-gen-buffers [^GL gl n]
  (let [buffers (int-array n)]
    (.glGenBuffers gl n buffers 0)
    buffers))

(defn- allocate-nio-buffers-if-needed [geometry]
  (letfn [(create-vertex-nio-buffer-if-needed [n]
            (letfn [(update-vertex-array [vertex-arrays]
                      (into {}
                            (for [[name data] vertex-arrays]
                              [name (if (nio-buffer/is-nio-buffer? data)
                                      data
                                      (nio-buffer/float-buffer data))])))]
              (update n :vertex-arrays update-vertex-array)))
          (create-index-nio-buffer-if-needed [n]
            (update n :index-array (fn [data]
                                                (cond
                                                  (not data) data
                                                  (nio-buffer/is-nio-buffer? data) data
                                                  :else (nio-buffer/int-buffer data)))))]
    (-> geometry
        create-vertex-nio-buffer-if-needed
        create-index-nio-buffer-if-needed)))

(defn create-geometry [^GL2ES2 gl geometry-spec]
  (letfn [(attribute-offsets [vertex-arrays]
            (reduce (fn [result vertex-array]
                      (let [[attribute vertex-array] vertex-array]
                        (-> (assoc-in result [:offsets attribute] (:total-size result))
                            (update :total-size + (:byte-size vertex-array)))))
                    {:total-size 0
                     :offsets    {}}
                    vertex-arrays))
          (copy-vertex-arrays-to-buffer [^GL4 gl vertex-arrays offsets]
            (doseq [[attribute vertex-array] vertex-arrays]
              (.glBufferSubData gl GL4/GL_ARRAY_BUFFER
                                (get-in offsets [:offsets attribute])
                                (:byte-size vertex-array)
                                (:buffer vertex-array))))
          (vertex-arrays-count [vertex-arrays]
            (let [counts (->> (vals vertex-arrays) (map :count))]
              (assert (apply = counts))
              (first counts)))
          (create-vertex-buffer [total-size]
            (let [^ints vbos (gl-gen-buffers gl 1)]
              (.glBindBuffer gl GL4/GL_ARRAY_BUFFER (aget vbos 0))
              (.glBufferData gl GL4/GL_ARRAY_BUFFER total-size nil GL4/GL_STATIC_DRAW)
              vbos))
          (create-index-buffer [index-array]
            (let [^ints ibos (gl-gen-buffers gl 1)]
              (.glBindBuffer gl GL4/GL_ELEMENT_ARRAY_BUFFER (aget ibos 0))
              (.glBufferData gl GL4/GL_ELEMENT_ARRAY_BUFFER (:byte-size index-array) (:buffer index-array) GL4/GL_STATIC_DRAW)
              ibos))]
    (let [{:keys [vertex-arrays index-array]} (allocate-nio-buffers-if-needed geometry-spec)
          offsets (attribute-offsets vertex-arrays)
          vbos (create-vertex-buffer (:total-size offsets))
          base-object {:vbos vbos
                       :mode (primitive->mode
                               (get geometry-spec :primitive :triangles))
                       :vertex-arrays vertex-arrays
                       :offsets offsets}]
      (copy-vertex-arrays-to-buffer gl vertex-arrays offsets)
      (if index-array
        (assoc base-object
          :ibos (create-index-buffer index-array)
          :count     (:count index-array)
          :elem-type (:type index-array))
        (assoc base-object
          :count (vertex-arrays-count vertex-arrays))))))

(defn bind-attributes [^GL2ES2 gl geometry program]
  (let [{:keys [vertex-arrays offsets]} geometry]
    (doseq [[attribute location] (get-in program [:locations :attributes])
            :let [vertex-array (vertex-arrays attribute)]]
      (.glEnableVertexAttribArray gl location)
      (.glVertexAttribPointer gl location (:dimension vertex-array) (:type vertex-array) false 0
                              (get-in offsets [:offsets attribute])))))

(defn draw-geometry [^GL2ES2 gl geometry]
  (let [{:keys [mode ^ints ibos elem-type count]} geometry]
    (if ibos
      (do
        (.glBindBuffer gl GL4/GL_ELEMENT_ARRAY_BUFFER (aget ibos 0))
        (.glDrawElements gl mode count elem-type 0)
        (.glBindBuffer gl GL4/GL_ELEMENT_ARRAY_BUFFER 0))
      (.glDrawArrays gl mode 0 count))))

(defn dispose-geometry [^GL2ES2 gl geometry]
  (let [{:keys [vbos ibos]} geometry]
    (.glDeleteBuffers gl 1 vbos 0)
    (when ibos
      (.glDeleteBuffers gl 1 ibos 0))))
