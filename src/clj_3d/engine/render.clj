(ns clj-3d.engine.render
  (:require [clj-3d.engine.shader :as shader]
            [clj-3d.engine.color :as color]
            [clj-3d.engine.transform :as transform]
            [clj-3d.engine.util.nio-buffer :as nio-buffer])
  (:import (com.jogamp.opengl GL4 GL GL3)
           (java.nio Buffer)
           (com.jogamp.common.nio Buffers)))

(def primitive->mode {:triangles GL/GL_TRIANGLES
                      :lines     GL/GL_LINES})

(defn- gl-gen-buffers [^GL gl n]
  (let [buffers (int-array n)]
    (.glGenBuffers gl n buffers 0)
    buffers))

(defn- gl-gen-vertex-arrays [^GL3 gl n]
  (let [arrays (int-array n)]
    (.glGenVertexArrays gl n arrays 0)
    arrays))

(defn- prepare-node [node]
  (letfn [(add-mesh-if-needed [n]
            (if-not (contains? n :mesh)
              (-> (assoc n :mesh {:primitive    :triangles
                                  :vertex-arrays {
                                                  :position (n :vertex-array)
                                                  }})
                  (dissoc :vertex-array))
              n))
          (create-vertex-array-map-if-needed [n]
            (if-not (get-in n [:mesh :vertex-arrays])
              (-> (assoc-in n [:mesh :vertex-arrays] {:position (get-in n [:mesh :vertex-array])})
                  (update :mesh #(dissoc % :vertex-array)))
              n))
          (create-vertex-nio-buffer-if-needed [n]
            (letfn [(update-vertex-array [vertex-arrays]
                      (into {}
                            (for [[name data] vertex-arrays]
                              [name (if (nio-buffer/is-nio-buffer? data)
                                      data
                                      (nio-buffer/float-buffer data))])))]
              (update-in n [:mesh :vertex-arrays] update-vertex-array)))
          (create-index-nio-buffer-if-needed [n]
            (update-in n [:mesh :index-array] (fn [data]
                                                (cond
                                                  (not data) data
                                                  (nio-buffer/is-nio-buffer? data) data
                                                  :else (nio-buffer/int-buffer data)))))]
    (-> node
        add-mesh-if-needed
        create-vertex-array-map-if-needed
        create-vertex-nio-buffer-if-needed
        create-index-nio-buffer-if-needed)))

(defn- create-object [^GL4 gl node]
  (let [{:keys [color]} node
        vertex-array (get-in node [:mesh :vertex-arrays :position])
        ^ints vbos (gl-gen-buffers gl 1)
        ^ints vaos (gl-gen-vertex-arrays gl 1)]
    (.glBindBuffer gl GL4/GL_ARRAY_BUFFER (aget vbos 0))
    (.glBufferData gl GL4/GL_ARRAY_BUFFER (:byte-size vertex-array) (:buffer vertex-array) GL4/GL_STATIC_DRAW)

    (.glBindVertexArray gl (aget vaos 0))

    (.glEnableVertexAttribArray gl 0)
    (.glVertexAttribPointer gl 0 (:dimension vertex-array) (:type vertex-array) false 0 0)
    (merge {:vaos         vaos
            :vbos         vbos
            :count        (:count vertex-array)
            :color        color
            :mode (primitive->mode
                    (get-in node [:mesh :primitive] :triangles))
            :model-matrix (transform/multiply*
                            (:transforms node))
            :program      (shader/build-program gl "flat")}
           (when-let [index-array (get-in node [:mesh :index-array])]
             (let [^ints ibos (gl-gen-buffers gl 1)]
               (.glBindBuffer gl  GL4/GL_ELEMENT_ARRAY_BUFFER (aget ibos 0))
               (.glBufferData gl GL4/GL_ELEMENT_ARRAY_BUFFER (:byte-size index-array) (:buffer index-array) GL4/GL_STATIC_DRAW)
               {:ibos ibos
                :count (:count index-array)
                :elem-type (:type index-array)})))))

(defn- convert-to-rgba [color]
  (if (vector? color)
    color
    (color/to-rgba-float color)))

(defn- draw-object [^GL4 gl object projection-view-matrix]
  (let [{:keys [program ^ints vaos ^ints ibos elem-type count color mode]} object
        [r g b] (convert-to-rgba color)
        color-location (.glGetUniformLocation gl program "color")
        mvp-location (.glGetUniformLocation gl program "mvp")
        model-view-projection-matrix (transform/multiply projection-view-matrix (:model-matrix object))]
    (.glUseProgram gl program)
    (.glUniform4f gl color-location r g b 1)
    (.glUniformMatrix4fv gl mvp-location 1 false model-view-projection-matrix 0)
    (.glBindVertexArray gl (aget vaos 0))
    (if ibos
      (do
        (.glBindBuffer gl GL4/GL_ELEMENT_ARRAY_BUFFER (aget ibos 0))
        (.glDrawElements gl mode count elem-type 0)
        (.glBindBuffer gl GL4/GL_ELEMENT_ARRAY_BUFFER 0))
      (.glDrawArrays gl mode 0 count))
    (.glBindVertexArray gl 0)))

(defn- dispose-object! [^GL4 gl object]
  (let [{:keys [vaos vbos]} object]
    (.glDeleteVertexArrays gl 1 vaos 0)
    (.glDeleteBuffers gl 1 vbos 0)))

(defn create-render-object [gl scene]
  (doall
    (for [node scene]
      (create-object gl
                     (prepare-node node)))))

(defn render [gl render-object camera]
  (let [projection-view-matrix (transform/get-projection-view-matrix camera)]
    (doseq [object render-object]
      (draw-object gl object projection-view-matrix))))

(defn dispose! [gl render-object]
  (doseq [object render-object]
    (dispose-object! gl object)))