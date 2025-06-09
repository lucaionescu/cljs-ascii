(ns main.sketches.sketch-02
  (:require [main.sketches.utils :as utils]))

(def opts {:noloop true})

(defn setup
  [])

(defn draw
  [{:keys [x y rows cols]}]
  (let [alphabet "0123456789"
        pos [(- y (/ rows 2)) (- x (/ cols 2))]
        circle (utils/magnitude pos)]
    {:ch (str (utils/char-at-mod alphabet (int circle)))
     :color "white"
     :bg-color "black"}))

