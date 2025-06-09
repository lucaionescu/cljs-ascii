(ns main.index
  (:require
   [shadow.lazy :as lazy]))

(def sketches
  {"sketch-01" (lazy/loadable [main.sketches.sketch-01/opts
                               main.sketches.sketch-01/setup
                               main.sketches.sketch-01/draw])
   "sketch-02" (lazy/loadable [main.sketches.sketch-02/opts
                               main.sketches.sketch-02/setup
                               main.sketches.sketch-02/draw])
   "sketch-03" (lazy/loadable [main.sketches.sketch-03/opts
                               main.sketches.sketch-03/setup
                               main.sketches.sketch-03/draw])})

(defonce opts {:fps 30
               :noloop false})

(defonce state (atom {:rows 0 :cols 0
                      :aspect-ratio 2.2
                      :frame 0
                      :cell-width 0 :cell-height 0
                      :mouse {:x 0 :y 0 :clicked false}
                      :prev-mouse {:x 0 :y 0 :clicked false}
                      :touch {:x 0 :y 0}
                      :pointer {:x 0 :y 0}}))
(defonce cells (atom []))
(defonce root-element (atom nil))

(defn- get-root-element
  []
  (or @root-element
      (reset! root-element (.getElementById js/document "app"))))

(defn- get-font-metrics
  []
  (let [span (.createElement js/document "span")]
    (.appendChild (get-root-element) span)
    (set! (.-innerHTML span) "0")
    (let [cell-width (.-width (.getBoundingClientRect span))
          cell-height (* (:aspect-ratio @state) cell-width)]
      (.removeChild (get-root-element) span)
      [cell-width cell-height])))

(defn- setup-cells []
  (set! (.-display (.-style (get-root-element))) "grid")
  (let [{:keys [rows cols]} @state]
    (set! (.-gridTemplateRows (.-style (get-root-element))) (str "repeat(" rows ", 1fr)"))
    (set! (.-gridTemplateColumns (.-style (get-root-element))) (str "repeat(" cols ", 1fr)"))
    (reset! cells
            (vec (for [_ (range rows)]
                   (vec (for [_ (range cols)]
                          (let [cell (.createElement js/document "span")]
                            (set! (.-display (.-style cell)) "block")
                            (.appendChild (get-root-element) cell)
                            cell))))))))

(defn- reset
  []
  (set! (.-innerHTML (get-root-element)) "")
  (let [[cell-width cell-height] (get-font-metrics)
        cols (js/Math.floor (/ js/window.innerWidth cell-width))
        rows (js/Math.floor (/ js/window.innerHeight cell-height))
        rows (inc rows)
        cols (inc cols)]
    (reset! state
            (-> @state
                (assoc :rows rows
                       :cols cols
                       :cell-width cell-width
                       :cell-height cell-height))))
  (setup-cells))

(defn- update-cell!
  [cell {:keys [ch color bg-color]}]
  (when (not= (.-textContent cell) ch)
    (set! (.-textContent cell) (str (or ch " "))))
  (when (not= (.-color (.-style cell)) color)
    (set! (.-color (.-style cell)) (or color "black")))
  (when (not= (.-backgroundColor (.-style cell)) bg-color)
    (set! (.-backgroundColor (.-style cell)) (or bg-color "white"))))

(defn- run
  [opts setup-fn draw-fn]
  (reset)
  (setup-fn)
  (let [fps (:fps opts)
        fps-interval (/ 1000 fps)
        last-time (atom (.now js/Date))
        frame (atom 0)]
    (defn draw-loop []
      (let [now (.now js/Date)
            elapsed (- now @last-time)]
        (when (or (:noloop opts) (>= elapsed fps-interval))
          (reset! last-time (- now (mod elapsed fps-interval)))
          (swap! frame inc)
          (let [{:keys [rows cols]} @state]
            (doseq [r (range rows)
                    c (range cols)]
              (let [context (assoc @state :x c :y r :frame @frame)
                    cell (get-in @cells [r c])
                    cell-state (draw-fn context)]
                (update-cell! cell cell-state)))))
        (when-not (:noloop opts)
          (js/requestAnimationFrame draw-loop))))
    (js/requestAnimationFrame draw-loop)))

(defn- on-resize
  []
  (reset))

(defn- on-mouse-move
  [event]
  (let [current-x (get-in @state [:mouse :x])
        current-y (get-in @state [:mouse :y])
        new-x (js/Math.floor (/ (.-clientX event) (:cell-width @state)))
        new-y (js/Math.floor (/ (.-clientY event) (:cell-height @state)))]
    (swap! state
           (fn [state]
             (-> state
                 (assoc-in [:mouse :x] new-x)
                 (assoc-in [:mouse :y] new-y)
                 (assoc-in [:pointer :x] new-x)
                 (assoc-in [:pointer :y] new-y))))
    (when (or
           (not= new-x current-x)
           (not= new-y current-y))
      (swap! state
             (fn [state]
               (-> state
                   (assoc-in [:prev-mouse :x] current-x)
                   (assoc-in [:prev-mouse :y] current-y)))))))

(defn- on-mouse-down
  [_]
  (swap! state assoc-in [:mouse :clicked] true))

(defn- on-mouse-up
  [_]
  (swap! state assoc-in [:mouse :clicked] false))

(defn- on-touch-move
  [event]
  (let [first-touch (aget (.-changedTouches event) 0)
        x (js/Math.floor (/ (.-clientX first-touch) (:cell-width @state)))
        y (js/Math.floor (/ (.-clientY first-touch) (:cell-height @state)))]
    (swap! state
           (fn [state]
             (-> state
                 (assoc-in [:touch :x] x)
                 (assoc-in [:touch :y] y)
                 (assoc-in [:pointer :x] x)
                 (assoc-in [:pointer :y] y))))))

(defn- on-sketch-select
  [e]
  (let [sketch (.. e -target -value)
        sketch (get sketches sketch)]
    (when-not (nil? sketch)
      (let  [el (.getElementById js/document "sketches-dropdown")]
        (set! (.-display (.-style el)) "none"))
      (lazy/load sketch
                 (fn [x]
                   (let [[sketch-opts setup draw] x
                         opts (merge opts (or sketch-opts {}))
                         setup (or setup #())]
                     (run opts setup draw)))))))

(defn- render-dropdown
  []
  (set! (.-innerHTML (get-root-element)) "")
  (let [el (.createElement js/document "select")
        default-option (.createElement js/document "option")]
    (set! (.-id el) "sketches-dropdown")
    (set! (.-value default-option) "")
    (set! (.-text default-option) "select sketch")
    (.appendChild el default-option)
    (doseq [k (keys sketches)]
      (let [opt (.createElement js/document "option")]
        (set! (.-value opt) k)
        (set! (.-text opt) k)
        (.appendChild el opt)))
    (.addEventListener el "change" on-sketch-select)
    (.appendChild (get-root-element) el)))

(defn ^:dev/after-load start
  []
  (aset js/window "onresize" on-resize)
  (.addEventListener js/document "mousemove" on-mouse-move)
  (.addEventListener js/document "mousedown" on-mouse-down)
  (.addEventListener js/document "mouseup" on-mouse-up)
  (.addEventListener js/document "touchmove" on-touch-move)
  (render-dropdown))

(defn init
  []
  (start))
