(ns clj-3d.core
  (:gen-class)
  (:require [clj-3d.application :as application]
            [clj-3d.triangle :as triangle])
  (:import (com.jogamp.newt NewtFactory)
           (com.jogamp.newt.opengl GLWindow)
           (com.jogamp.opengl.util Animator)
           (com.jogamp.opengl GLProfile GLCapabilities)))

(defn make-gl-window []
  (let [screen (-> (NewtFactory/createDisplay nil)
                   (NewtFactory/createScreen 0))
        gl-capabilities (-> (GLProfile/get GLProfile/GL4)
                            (GLCapabilities.))]
    (GLWindow/create screen gl-capabilities)))

(defn configure-gl-window [^GLWindow gl-window]
  (doto gl-window
    (.setSize 800 600)
    (.setTitle "clj-3d")
    (.setVisible true)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [^GLWindow gl-window (-> (make-gl-window) configure-gl-window)
        animator (Animator. gl-window)
        on-exit-hook (fn []
                       (.remove animator gl-window)
                       (.destroy gl-window))
        application (application/make-application
                      triangle/make-triangle
                      :on-exit-hook on-exit-hook)]
    (doto gl-window
      (.addGLEventListener application)
      (.addKeyListener application))
    (.start animator)))
