(ns clj-3d.application
  (:require [clojure.tools.logging :as log])
  (:import (com.jogamp.opengl GLEventListener GLAutoDrawable GL4)
           (com.jogamp.newt.event KeyListener KeyEvent)))

(defprotocol Application
  (render [app gl])
  (resize [app gl x y width height])
  (dispose [app gl]))

(defn get-gl4 ^GL4 [^GLAutoDrawable drawable]
  (-> drawable .getGL .getGL4))

(defn make-application [create-application-fn & {:keys [on-exit-hook]}]
  (let [app (atom nil)]
    (reify
      GLEventListener

      (init [_ drawable]
        (let [gl (get-gl4 drawable)]
          (log/info "GL version =" (.glGetString gl GL4/GL_VERSION))
          (log/info "GL renderer =" (.glGetString gl GL4/GL_RENDERER))
          (reset! app (create-application-fn gl))))

      (dispose [_ drawable]
        (let [gl (get-gl4 drawable)]
          (dispose @app gl))
        (System/exit 0))

      (display [_ drawable]
        (let [gl (get-gl4 drawable)]
          (.glClearColor gl 0.4 0.4 0.4 1.0)
          (.glClear gl GL4/GL_COLOR_BUFFER_BIT)
          (render @app gl)))

      (reshape [_ drawable x y width height]
        (let [gl (get-gl4 drawable)]
          (.glViewport gl x y width height)
          (resize @app gl x y width height)))

      KeyListener

      (keyPressed [_ e]
        (condp = (.getKeyCode e)
          KeyEvent/VK_ESCAPE (when on-exit-hook (on-exit-hook))))

      (keyReleased [_ e]))))
