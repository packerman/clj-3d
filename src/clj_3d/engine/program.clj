(ns clj-3d.engine.program
  (:require [clojure.tools.logging :as log]
            [clj-3d.engine.shader :as shader])
  (:import (com.jogamp.opengl GL2ES2)))

(defn program-name-for-material [material]
  (cond
    (= material :normal) "normal"
    (and (get-in material [:colors :specular]) (:smooth material))  "phong"
    (get-in material [:colors :specular]) "gouraud"
    (get-in material [:colors :diffuse]) "diffuse"
    :else "flat"))

(def programs-to-build #{
                         {:name       "flat"
                          :attributes {
                                       "position" 0
                                       }
                          :uniforms   #{"color" "model_view_projection_matrix"}}
                         {:name       "normal"
                          :attributes {
                                       "position" 0
                                       "normal"   1
                                       }
                          :uniforms   #{"model_view_projection_matrix" "normal_matrix"}}
                         {:name       "diffuse"
                          :attributes {
                                       "position" 0
                                       "normal"   1
                                       }
                          :uniforms   #{"model_view_projection_matrix" "model_view_matrix" "normal_matrix"
                                        "material_ambient" "material_diffuse"
                                        "light_color" "light_position"}}
                         {:name       "gouraud"
                          :attributes {
                                       "position" 0
                                       "normal"   1
                                       }
                          :uniforms   #{"model_view_projection_matrix" "model_view_matrix" "normal_matrix"
                                        "material_ambient" "material_diffuse" "material_specular"
                                        "specular_power"
                                        "light_color" "light_position"}}
                         {:name       "phong"
                          :attributes {
                                       "position" 0
                                       "normal"   1
                                       }
                          :uniforms   #{"model_view_projection_matrix" "model_view_matrix" "normal_matrix"
                                        "material_ambient" "material_diffuse" "material_specular"
                                        "specular_power"
                                        "light_color" "light_position"}}
                         })

(defn build-programs [^GL2ES2 gl]
  (letfn [(get-uniform-location [program-id uniform-name]
            (let [uniform-location (.glGetUniformLocation gl program-id uniform-name)]
              (when (neg? uniform-location)
                (log/warn uniform-name "location =" uniform-location "in program" program-id))
              uniform-location))]
    (into {}
          (for [program-to-build programs-to-build
                :let [{:keys [name]} program-to-build
                      program-id (shader/build-program gl name)]]
            [name {:program-id program-id
                   :name       name
                   :locations  {
                                :attributes (:attributes program-to-build)
                                :uniforms   (into {}
                                                  (for [uniform-name (:uniforms program-to-build)]
                                                    [uniform-name (get-uniform-location program-id uniform-name)]))}}]))))

(defn attribute-location [program nane]
  (get-in program [:locations :attributes name]))

(defn uniform-location [program name]
  (get-in program [:locations :uniforms name]))

(defn use-program [^GL2ES2 gl {:keys [program-id]}]
  (.glUseProgram gl program-id))

(defmulti apply-material (fn [gl program material] (:name program)))

(defmethod apply-material "normal" [_ _ _])

(defmethod apply-material "flat" [^GL2ES2 gl program material]
  (let [[r g b] (get-in material [:colors :ambient])]
    (.glUniform4f gl (get-in program [:locations :uniforms "color"]) r g b 1)))

(defmethod apply-material "diffuse" [^GL2ES2 gl program material]
  (let [[ra ga ba] (get-in material [:colors :ambient])
        [rd gd bd] (get-in material [:colors :diffuse])]
    (.glUniform3f gl (get-in program [:locations :uniforms "material_ambient"]) ra ga ba)
    (.glUniform3f gl (get-in program [:locations :uniforms "material_diffuse"]) rd gd bd)))

(defmethod apply-material "gouraud" [^GL2ES2 gl program material]
  (let [[ra ga ba] (get-in material [:colors :ambient])
        [rd gd bd] (get-in material [:colors :diffuse])
        [rs gs bs] (get-in material [:colors :specular])
        specular-power (get material :specular-power 1.0)]
    (.glUniform3f gl (get-in program [:locations :uniforms "material_ambient"]) ra ga ba)
    (.glUniform3f gl (get-in program [:locations :uniforms "material_diffuse"]) rd gd bd)
    (.glUniform3f gl (get-in program [:locations :uniforms "material_specular"]) rs gs bs)
    (.glUniform1f gl (get-in program [:locations :uniforms "specular_power"]) specular-power)))

(defmethod apply-material "phong" [^GL2ES2 gl program material]
  (let [[ra ga ba] (get-in material [:colors :ambient])
        [rd gd bd] (get-in material [:colors :diffuse])
        [rs gs bs] (get-in material [:colors :specular])
        specular-power (get material :specular-power 1.0)]
    (.glUniform3f gl (get-in program [:locations :uniforms "material_ambient"]) ra ga ba)
    (.glUniform3f gl (get-in program [:locations :uniforms "material_diffuse"]) rd gd bd)
    (.glUniform3f gl (get-in program [:locations :uniforms "material_specular"]) rs gs bs)
    (.glUniform1f gl (get-in program [:locations :uniforms "specular_power"]) specular-power)))
