(ns main.sketches.sketch-03
  (:require [main.sketches.utils :as utils]))

(def opts {:noloop false})

(defn draw
  [{:keys [x y frame]}]
  (let [alphabet "-+/0+*#(){}[]"
        t (* 0.01 frame)
        a (.sin js/Math (* t (.sin js/Math (* 0.02 (+ t (.sin js/Math (* 0.6 x)))))))
        b (.cos js/Math (* t (.sin js/Math (* 0.03 (+ t (.cos js/Math (* 0.7 y)))))))
        c (.cos js/Math (* y (.cos js/Math (* 0.04 (+ t (.sin js/Math (* 0.8 t)))))))
        i (+ a b c y)
        i (* 40 i)]
    {:ch (utils/element-at-mod alphabet i)
     :color (utils/element-at-mod utils/palette i)
     :bg-color "white"}))

