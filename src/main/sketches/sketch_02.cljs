(ns main.sketches.sketch-02
  (:require [main.sketches.utils :as utils]))

(def opts {:noloop false})

(defn- circle-sdf
  [pos r]
  (- (utils/magnitude pos) r))

(defn draw
  [{:keys [x y rows cols aspect-ratio frame]}]
  (let [alphabet "-–—‐_‚„“”‘’«»‹›'⟨·;#․‾/'|{}‿(){}[]"
        m (min rows cols)
        x (/ (- (* 2.0 x) cols) m)
        y (/ (- (* 2.0 y) rows) m)
        x (/ x aspect-ratio)
        radius (+ 0.5 (.cos js/Math (* 0.03 frame)))
        d (circle-sdf [x y] radius)
        d (int (* d (count alphabet)))]
    {:ch (utils/element-at-mod alphabet d)
     :color (utils/element-at-mod utils/palette d)
     :bg-color "black"}))

