(ns main.sketches.sketch-01
  (:require [main.sketches.utils :as utils]))

(defn draw
  [{:keys [x y mouse pointer]}]
  (let [alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        cursor-x (:x pointer)
        cursor-y (:y pointer)
        color (if (or (= x cursor-x) (= y cursor-y)) "red" "black")
        color (if (and (:clicked mouse) (= x cursor-x) (= y cursor-y)) "green" color)]
    {:ch (utils/element-at-mod alphabet x)
     :color color}))

