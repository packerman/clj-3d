(ns clj-3d.core
  (:gen-class)
  (:import (com.jogamp.newt NewtFactory)
           (com.jogamp.opengl GLProfile GLCapabilities GLEventListener GLAutoDrawable GL4)
           (com.jogamp.newt.opengl GLWindow)
           (com.jogamp.newt.event KeyListener KeyEvent)
           (com.jogamp.opengl.util Animator)))

(defn make-gl-window []
  (let [screen (-> (NewtFactory/createDisplay nil)
                   (NewtFactory/createScreen 0))
        gl-capabilities (-> (GLProfile/get GLProfile/GL4)
                            (GLCapabilities.))]
    (GLWindow/create screen gl-capabilities)))

(defn configure-gl-window [gl-window]
  (doto gl-window
    (.setSize 1024 768)
    (.setTitle "clj-3d")
    (.setVisible true)))

(defn get-gl4 ^GL4 [^GLAutoDrawable drawable]
  (-> drawable .getGL .getGL4))

(defn make-application [& {:keys [on-exit-hook]}]
  (reify
    GLEventListener

    (init [_ drawable]
      (let [gl (get-gl4 drawable)]
        (println "GL version =" (.glGetString gl GL4/GL_VERSION))
        (println "GL renderer =" (.glGetString gl GL4/GL_RENDERER))))

    (dispose [_ drawable]
      (System/exit 0))

    (display [_ drawable]
      (let [gl (get-gl4 drawable)]
        (.glClearColor gl 0.4 0.4 0.4 1.0)
        (.glClear gl GL4/GL_COLOR_BUFFER_BIT)))

    (reshape [_ drawable x y width height]
      (let [gl (get-gl4 drawable)]
        (.glViewport gl x y width height)))

    KeyListener

    (keyPressed [_ e]
      (condp = (.getKeyCode e)
        KeyEvent/VK_ESCAPE (when on-exit-hook (on-exit-hook))))

    (keyReleased [_ e])))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [^GLWindow gl-window (-> (make-gl-window) configure-gl-window)
        animator (Animator. gl-window)
        on-exit-hook (fn []
                       (.remove animator gl-window)
                       (.destroy gl-window))
        application (make-application :on-exit-hook on-exit-hook)]
    (doto gl-window
      (.addGLEventListener application)
      (.addKeyListener application))
    (.start animator)))
