(ns clj-3d.engine.color)

(def limeg-reen 0x32CD32FF)
(def lime 0x00FF00FF)
(def forest-green 0x228B22FF)
(def green 0x008000FF)
(def dark-green 0x006400FF)
(def spring-green 0x00FF7FFF)

(def blue 0x0000FFFF)
(def light-sky-blue 0x87CEFAFF)
(def sky-blue 0x87CEEBFF)
(def deep-sky-blue 0x00BFFFFF)
(def steel-blue 0x4682B4FF)
(def midnight-blue 0x191970FF)
(def blue-violet 0x8A2BE2FF)

(def red 0xFF0000FF)

(def orange 0xFFA500FF)

(def yellow 0xFFFF00FF)

(def white 0xFFFFFFFF)

(def black 0x000000FF)

(def gray 0x808080FF)
(def dim-gray 0x696969FF)

(def pink 0xFFC0CBFF)

(def magenta 0xFF00FFFF)
(def fuchsia 0xFF00FFFF)
(def darkmagenta 0x8B008BFF)
(def purple 0x800080FF)

(defn- byte-at [x n]
  (-> (bit-shift-right x n) (bit-and 0xff)))

(defn to-rgba [color-hex]
  [(byte-at color-hex 24) (byte-at color-hex 16) (byte-at color-hex 8) (byte-at color-hex 0)])

(defn to-rgba-float [color-hex]
  [(-> (byte-at color-hex 24) float (/ 0xff))
   (-> (byte-at color-hex 16) float (/ 0xff))
   (-> (byte-at color-hex 8) float (/ 0xff))
   (-> (byte-at color-hex 0) float (/ 0xff))])

(defn scale-color [c k]
  (let [[r g b a] c]
    [(* k r) (* k g) (* k b) a]))
