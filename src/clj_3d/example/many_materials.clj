(ns clj-3d.example.many-materials
  (:gen-class)
  (:require [clj-3d.example.common :as common]
            [clj-3d.engine.color :as color]
            [clj-3d.engine.transform :as transform]
            [clj-3d.application :as application]
            [clj-3d.engine.util.nio-buffer :refer [int-bits->float-buffer]]))

(def scene {:geometries {
                         "cube50" {
                                   :vertex-arrays {
                                                   "position" (int-bits->float-buffer [[0xC2480000 0xC2480000 0x00000000] [0xC2480000 0x42480000 0x00000000]
                                                                                       [0x42480000 0x42480000 0x00000000] [0x42480000 0xC2480000 0x00000000]
                                                                                       [0xC2480000 0xC2480000 0x42C80000] [0x42480000 0xC2480000 0x42C80000]
                                                                                       [0x42480000 0x42480000 0x42C80000] [0xC2480000 0x42480000 0x42C80000]
                                                                                       [0xC2480000 0xC2480000 0x00000000] [0x42480000 0xC2480000 0x00000000]
                                                                                       [0x42480000 0xC2480000 0x42C80000] [0xC2480000 0xC2480000 0x42C80000]
                                                                                       [0x42480000 0xC2480000 0x00000000] [0x42480000 0x42480000 0x00000000]
                                                                                       [0x42480000 0x42480000 0x42C80000] [0x42480000 0xC2480000 0x42C80000]
                                                                                       [0x42480000 0x42480000 0x00000000] [0xC2480000 0x42480000 0x00000000]
                                                                                       [0xC2480000 0x42480000 0x42C80000] [0x42480000 0x42480000 0x42C80000]
                                                                                       [0xC2480000 0x42480000 0x00000000] [0xC2480000 0xC2480000 0x00000000]
                                                                                       [0xC2480000 0xC2480000 0x42C80000] [0xC2480000 0x42480000 0x42C80000]])
                                                   "normal"   (int-bits->float-buffer [[0x00000000 0x00000000 0xBF800000] [0x00000000 0x00000000 0xBF800000]
                                                                                       [0x00000000 0x00000000 0xBF800000] [0x00000000 0x00000000 0xBF800000]
                                                                                       [0x00000000 0x00000000 0x3F800000] [0x00000000 0x00000000 0x3F800000]
                                                                                       [0x00000000 0x00000000 0x3F800000] [0x00000000 0x00000000 0x3F800000]
                                                                                       [0x00000000 0xBF800000 0x00000000] [0x00000000 0xBF800000 0x00000000]
                                                                                       [0x00000000 0xBF800000 0x00000000] [0x80000000 0xBF800000 0x00000000]
                                                                                       [0x3F800000 0x00000000 0x00000000] [0x3F800000 0x00000000 0x00000000]
                                                                                       [0x3F800000 0x00000000 0x00000000] [0x3F800000 0x00000000 0x00000000]
                                                                                       [0x00000000 0x3F800000 0x00000000] [0x00000000 0x3F800000 0x00000000]
                                                                                       [0x00000000 0x3F800000 0x00000000] [0x80000000 0x3F800000 0x00000000]
                                                                                       [0xBF800000 0x00000000 0x00000000] [0xBF800000 0x00000000 0x00000000]
                                                                                       [0xBF800000 0x00000000 0x00000000] [0xBF800000 0x00000000 0x00000000]])
                                                   }
                                   :index-arrays  {0 [[0 1 2] [2 3 0]]
                                                   1 [[4 5 6] [6 7 4]]
                                                   2 [[8 9 10] [10 11 8]]
                                                   3 [[12 13 14] [14 15 12]]
                                                   4 [[16 17 18] [18 19 16]]
                                                   5 [[20 21 22] [22 23 20]]}}
                         "grid"   {:primitive     :lines
                                   :vertex-arrays {
                                                   "position" (common/grid-vertex-array 11 11)}}}
            :nodes      [{:geometry   "grid"
                          :material   {:colors {:ambient color/white}}
                          :transforms [(transform/axis-rotation (Math/toRadians 90) :x)
                                       (transform/scale 10 10 10)]}

                         {:geometry   "cube50"
                          :materials  {
                                       0 {:colors {
                                                   :ambient color/red
                                                   :diffuse color/red}}
                                       1 {:colors {
                                                   :ambient color/green
                                                   :diffuse color/green}}
                                       2 {:colors {
                                                   :ambient color/blue
                                                   :diffuse color/blue}}
                                       3 {:colors {
                                                   :ambient color/yellow
                                                   :diffuse color/yellow}}
                                       4 {:colors {
                                                   :ambient color/orange
                                                   :diffuse color/orange}}
                                       5 {:colors {
                                                   :ambient color/magenta
                                                   :diffuse color/magenta}}}

                          :transforms [(transform/translation 2.5 0.5 2)
                                       (transform/scale 0.01 0.01 0.01)]
                          }]
            :lights     [{
                          :position [2 4 -2]
                          :color    color/white}]
            })

(def camera (transform/perspective-camera {:fovy (Math/toRadians 60.0)
                                           :near 0.1
                                           :far  100.00}
                                          {:eye    [0 2 7]
                                           :center [0 0 0]
                                           :up     [0 1 0]}))

(def app
  (common/make-application-for-scene scene camera))

(defn -main
  [& args]
  (application/launch app #_{:width 1280 :height 800}))
