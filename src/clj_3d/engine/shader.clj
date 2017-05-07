(ns clj-3d.engine.shader
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log])
  (:import (com.jogamp.opengl GL2ES2)))

(def extensions->types {"vert" GL2ES2/GL_VERTEX_SHADER
                        "frag" GL2ES2/GL_FRAGMENT_SHADER})

(defn- log-and-exit [message]
  (log/error message)
  0)

(defn compile-shader [^GL2ES2 gl type ^String code]
  (letfn [(compile [shader]
            (.glShaderSource gl shader 1 (into-array String [code]) (int-array [(.length code)]) 0)
            (.glCompileShader gl shader)
            (if (zero? (get-shader-parameter shader GL2ES2/GL_COMPILE_STATUS))
              (exit-on-compile-error shader)
              shader))
          (exit-on-compile-error [shader]
            (log/error "Could not compile shader:" (get-shader-info-log shader))
            (.glDeleteShader gl shader)
            0)
          (get-shader-parameter [shader param-name]
            (let [params (int-array 1)]
              (.glGetShaderiv gl shader param-name params 0)
              (aget params 0)))
          (get-shader-info-log [shader]
            (let [^int info-length (get-shader-parameter shader GL2ES2/GL_INFO_LOG_LENGTH)
                  ^bytes info-log (byte-array info-length)]
              (.glGetShaderInfoLog gl shader info-length nil 0 info-log 0)
              (String. info-log 0 info-length)))]
    (let [shader (.glCreateShader gl type)]
      (if (zero? shader)
        (log-and-exit "Could not create shader")
        (compile shader)))))

(defn link-program [^GL2ES2 gl shaders]
  (letfn [(link [program]
            (doseq [shader shaders]
              (.glAttachShader gl program shader))
            (.glLinkProgram gl program)
            (if (zero? (get-program-parameter program GL2ES2/GL_LINK_STATUS))
              (exit-on-link-error program)
              program))
          (exit-on-link-error [program]
            (log/error "Could not link program:" (get-program-info-log program))
            (.glDeleteProgram gl program)
            0)
          (get-program-parameter [program param-name]
            (let [params (int-array 1)]
              (.glGetProgramiv gl program param-name params 0)
              (aget params 0)))
          (get-program-info-log [program]
            (let [^int info-length (get-program-parameter program GL2ES2/GL_INFO_LOG_LENGTH)
                  ^bytes info-log (byte-array info-length)]
              (.glGetProgramInfoLog gl program info-length nil 0 info-log 0)
              (String. info-log 0 info-length)))]
    (let [program (.glCreateProgram gl)]
      (if (zero? program)
        (log-and-exit "Could not create program")
        (link program)))))

(defn build-program [^GL2ES2 gl name]
  (letfn [(compile-shaders []
            (doall
              (for [[extension type] extensions->types
                    :let [shader-url (io/resource
                                       (str "shader/" name "." extension))]
                    :when shader-url]
                (compile-shader gl type (slurp shader-url)))))]
    (let [program-id (link-program gl
                                   (compile-shaders))]
      (log/debug "Linked program" name ": id" program-id)
      program-id)))