(ns clj-3d.engine.color)

(defn- to-rgba [color-hex]
  (letfn [(byte-at [x n]
            (-> (bit-shift-right x n) (bit-and 0xff)))]
    [(-> (byte-at color-hex 24) float (/ 0xff))
     (-> (byte-at color-hex 16) float (/ 0xff))
     (-> (byte-at color-hex 8) float (/ 0xff))
     (-> (byte-at color-hex 0) float (/ 0xff))]))

(def lime-green (to-rgba 0x32CD32FF))
(def lime (to-rgba 0x00FF00FF))
(def forest-green (to-rgba 0x228B22FF))
(def green (to-rgba 0x008000FF))
(def dark-green (to-rgba 0x006400FF))
(def spring-green (to-rgba 0x00FF7FFF))

(def blue (to-rgba 0x0000FFFF))
(def light-sky-blue (to-rgba 0x87CEFAFF))
(def sky-blue (to-rgba 0x87CEEBFF))
(def deep-sky-blue (to-rgba 0x00BFFFFF))
(def steel-blue (to-rgba 0x4682B4FF))
(def midnight-blue (to-rgba 0x191970FF))
(def blue-violet (to-rgba 0x8A2BE2FF))

(def red (to-rgba 0xFF0000FF))

(def orange (to-rgba 0xFFA500FF))

(def yellow (to-rgba 0xFFFF00FF))

(def white (to-rgba 0xFFFFFFFF))

(def black (to-rgba 0x000000FF))

(def gray (to-rgba 0x808080FF))
(def dim-gray (to-rgba 0x696969FF))

(def pink (to-rgba 0xFFC0CBFF))

(def magenta (to-rgba 0xFF00FFFF))
(def fuchsia (to-rgba 0xFF00FFFF))
(def darkmagenta (to-rgba 0x8B008BFF))
(def purple (to-rgba 0x800080FF))

(defn scale-color [c k]
  (let [[r g b a] c]
    [(* k r) (* k g) (* k b) a]))
