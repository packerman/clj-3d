(ns clj-3d.engine.render
  (:require [clj-3d.engine.shader :as shader]
            [clj-3d.engine.program :as program]
            [clj-3d.engine.color :as color]
            [clj-3d.engine.transform :as transform]
            [clj-3d.engine.util.nio-buffer :as nio-buffer]
            [clojure.tools.logging :as log])
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
              (-> (assoc n :mesh {:primitive     :triangles
                                  :vertex-arrays {"position" (n :vertex-array)}})
                  (dissoc :vertex-array))
              n))
          (create-vertex-array-map-if-needed [n]
            (if-not (get-in n [:mesh :vertex-arrays])
              (-> (assoc-in n [:mesh :vertex-arrays] {"position" (get-in n [:mesh :vertex-array])})
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
                                                  :else (nio-buffer/int-buffer data)))))
          (add-material-if-needed [n]
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
        add-mesh-if-needed
        create-vertex-array-map-if-needed
        create-vertex-nio-buffer-if-needed
        create-index-nio-buffer-if-needed
        add-material-if-needed
        convert-colors-if-needed)))

(defn- program-name-for-material [material]
  (cond
    (= material :normal) "normal"
    (get-in material [:colors :diffuse]) "diffuse"
    :else "flat"))

(defn- set-uniforms-for-material [^GL4 gl program material]
  (condp = (:name program)
    "normal" (do)
    "flat" (let [[r g b] (get-in material [:colors :ambient])]
             (.glUniform4f gl (get-in program [:locations :uniforms "color"]) r g b 1))
    "diffuse" (let [[ra ga ba] (get-in material [:colors :ambient])
                    [rd gd bd] (get-in material [:colors :diffuse])]
                (.glUniform4f gl (get-in program [:locations :uniforms "material_ambient"]) ra ga ba 1)
                (.glUniform4f gl (get-in program [:locations :uniforms "material_diffuse"]) rd gd bd 1))))

(defn attribute-offsets [vertex-arrys]
  (reduce (fn [result vertex-array]
            (let [[attribute vertex-array] vertex-array]
              (-> (assoc-in result [:offsets attribute] (:total-size result))
                  (update :total-size + (:byte-size vertex-array)))))
          {:total-size 0
           :offsets    {}}
          vertex-arrys))

(defn vertex-arrays-count [vertex-arrays]
  (let [counts (->> (vals vertex-arrays) (map :count))]
    (assert (apply = counts))
    (first counts)))

(defn copy-vertex-arrays-to-buffer [^GL4 gl vertex-arrays offsets]
  (.glBufferData gl GL4/GL_ARRAY_BUFFER (:total-size offsets) nil GL4/GL_STATIC_DRAW)
  (doseq [[attribute vertex-array] vertex-arrays]
    (.glBufferSubData gl GL4/GL_ARRAY_BUFFER
                      (get-in offsets [:offsets attribute])
                      (:byte-size vertex-array)
                      (:buffer vertex-array))))

(defn- create-object [^GL4 gl programs node]
  (let [{:keys [material]} node
        vertex-arrays (get-in node [:mesh :vertex-arrays])
        ^ints vbos (gl-gen-buffers gl 1)
        ^ints vaos (gl-gen-vertex-arrays gl 1)
        offets (attribute-offsets vertex-arrays)
        program (get programs (program-name-for-material material))]
    (.glBindBuffer gl GL4/GL_ARRAY_BUFFER (aget vbos 0))
    (copy-vertex-arrays-to-buffer gl vertex-arrays offets)
    (.glBindVertexArray gl (aget vaos 0))

    (doseq [[attribute location] (get-in program [:locations :attributes])
            :let [vertex-array (vertex-arrays attribute)]]
      (.glEnableVertexAttribArray gl location)
      (.glVertexAttribPointer gl location (:dimension vertex-array) (:type vertex-array) false 0
                              (get-in offets [:offsets attribute])))

    (merge {:vaos         vaos
            :vbos         vbos
            :material     material
            :count        (vertex-arrays-count vertex-arrays)
            :mode         (primitive->mode
                            (get-in node [:mesh :primitive] :triangles))
            :model-matrix (transform/multiply*
                            (:transforms node))
            :program      program}
           (when-let [index-array (get-in node [:mesh :index-array])]
             (let [^ints ibos (gl-gen-buffers gl 1)]
               (.glBindBuffer gl GL4/GL_ELEMENT_ARRAY_BUFFER (aget ibos 0))
               (.glBufferData gl GL4/GL_ELEMENT_ARRAY_BUFFER (:byte-size index-array) (:buffer index-array) GL4/GL_STATIC_DRAW)
               {:ibos      ibos
                :count     (:count index-array)
                :elem-type (:type index-array)})))))

(defn when-uniform-exists [program name action]
  (when-let [location (program/uniform-location program name)]
    (action location)))

(defn- draw-object [^GL4 gl object matrices lights]
  (let [{:keys [program ^ints vaos ^ints ibos elem-type count material mode]} object
        model-view-projection-matrix (transform/multiply (:projection-view-matrix matrices)
                                                         (:model-matrix object))
        model-view-matrix (transform/multiply (:view-matrix matrices)
                                              (:model-matrix object))]
    (program/use-program gl program)
    (set-uniforms-for-material gl program material)

    (when-let [[light & _] lights]
      (let [[x y z] (:position light)
            [r g b a] (:color light)]
        (when-uniform-exists program "light_position" (fn [location]
                                                        (.glUniform3f gl location x y z)))
        (when-uniform-exists program "light_color" (fn [location]
                                                     (.glUniform4f gl location r g b a)))))

    (when-uniform-exists program "model_view_matrix" (fn [location]
                                                       (.glUniformMatrix4fv gl location 1 false
                                                                            model-view-matrix 0)))
    (when-uniform-exists program "normal_matrix" (fn [location]
                                                   (.glUniformMatrix4fv gl location 1 false
                                                                        (-> model-view-matrix
                                                                            transform/inverse
                                                                            transform/transpose) 0)))
    (.glUniformMatrix4fv gl (program/uniform-location program "model_view_projection_matrix")
                         1 false model-view-projection-matrix 0)
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
  (let [programs (program/build-programs gl)]
    {:lights (scene :lights)
     :objects (doall
                (for [node (:nodes scene)]
                  (create-object gl programs
                                 (prepare-node node))))}))

(defn render [gl render-object camera]
  (let [matrices {:projection-view-matrix (transform/get-projection-view-matrix camera)
                  :view-matrix            (:view-matrix camera)}]
    (doseq [object (:objects render-object)]
      (draw-object gl object matrices (:lights render-object)))))

(defn dispose! [gl render-object]
  (doseq [object (:objects render-object)]
    (dispose-object! gl object)))