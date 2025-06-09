(ns main.sketches.utils)

(def palette ["#2c3f54" "#f44918" "#4646df" "#f4aaab" "#396c7d" "#ee751a" "#0a2a41"
              "#fecf1a" "#766b4f" "#ff5630" "#2c416e" "#cf3b45" "#83757d" "#feac00"
              "#e51d20" "#ff3a5c" "#ffb713" "#4f94cf" "#009f45" "#f0f0ec"])

(defn element-at-mod
  [s idx]
  (let [len (count s)
        i (mod idx len)]
    (nth s i)))

(defn magnitude
  [v]
  (Math/sqrt (reduce + (map #(* % %) v))))

