(ns main.sketches.utils)

(defn char-at-mod
  [s idx]
  (let [len (count s)
        i (mod idx len)]
    (nth s i)))

(defn magnitude
  [v]
  (Math/sqrt (reduce + (map #(* % %) v))))

