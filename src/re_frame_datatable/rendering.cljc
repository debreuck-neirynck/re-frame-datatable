(ns re-frame-datatable.rendering
  "Rendering functions for the table"
  (:require [re-frame.core :as re-frame]
            [re-frame-datatable.paths :as p]
            [re-frame-datatable.events :as e]
            [re-frame-datatable.subs :as subs]
            [re-frame-datatable.sorting :as sorting]))

(def enabled-key :re-frame-datatable.core/enabled?)

(defn- css-class-str [classes]
  {:class (->> classes
               (filter (complement nil?))
               (clojure.string/join \space))})

(defn- drag-drop-attrs
  "Generates the attributes according to the drag-drop configuration.  If the configuration
   is empty, this just returns an empty map.  The `-fn` arguments are functions that should 
   accept the row as argument.  All but the `draggable-fn` functions should return a new
   handler function.  The `draggable-fn` should return a boolean value, to indicate whether
   the row is draggable or not (this supersedes the `draggable?` option)."
  [{:keys [::draggable? ::draggable-fn ::drag-fn ::drop-fn ::drag-over-fn]} row]
  (cond-> {}
    ;; TODO Allow dropping something on the table itself, in addition to dropping on specific rows
    draggable? (assoc :draggable true)
    draggable-fn (assoc :draggable (draggable-fn row))
    drag-fn (assoc :on-drag-start (drag-fn row))
    drop-fn (assoc :on-drop (drop-fn row))
    drag-over-fn (assoc :on-drag-over (drag-over-fn row))))

(defn- checked? [x]
  #?(:cljs (-> x .-target .-checked)
     :clj (get-in x [:target :checked])))

(defn- header [visible-items state db-id columns-def options]
  (let [{:keys [:selection]} state
        {:keys [:re-frame-datatable.core/extra-header-row-component]} options]
    [:thead
     (when extra-header-row-component
       [extra-header-row-component])

     [:tr
      (when (enabled-key selection)
        [:th {:style {:max-width "16em"}}
         [:input {:type      "checkbox"
                  :checked   (clojure.set/subset?
                              (->> visible-items (map first) (set))
                              (:selected-indexes selection))
                  :on-change #(when-not (zero? (count visible-items))
                                (re-frame/dispatch [::change-table-selection
                                                    db-id
                                                    (->> visible-items (map first) (set))
                                                    checked?]))}]
         [:br]
         [:small (str (count (:selected-indexes selection)) " selected")]])

      (doall
       (for [{:keys [:re-frame-datatable.core/column-key
                     :re-frame-datatable.core/column-label
                     :re-frame-datatable.core/sorting]} columns-def]
         ^{:key (str column-key)}
         [:th
          (merge
           (when (enabled-key sorting)
             {:style    {:cursor "pointer"}
              :on-click #(re-frame/dispatch [::sorting/set-sort-key db-id column-key
                                             (:re-frame-datatable.core/comp-fn sorting)])
              :class    "sorted-by"})
           (when (= column-key (get-in state [::p/sort ::p/sort-key]))
             (css-class-str ["sorted-by"
                             (condp = (get-in state [::p/sort ::p/sort-comp])
                               ::sorting/sort-asc "asc"
                               ::sorting/sort-desc "desc"
                               "")])))
          (cond
            (string? column-label) column-label
            (fn? column-label) [column-label]
            :else "")]))]]))

(defn- empty-table [columns-def state options]
  (let [{:keys [:selection]} state
        {:keys [:re-frame-datatable.core/empty-tbody-component]} options]
    [:tr
     [:td {:col-span (+ (count columns-def)
                        (if (enabled-key selection) 1 0))
           :style    {:text-align "center"}}
      (if empty-tbody-component
        [empty-tbody-component]
        "no items")]]))

(defn- table-row [db-id columns-def state options [i data-entry]]
  (let [{:keys [:selection]} state
        {:keys [:re-frame-datatable.core/table-classes
                :re-frame-datatable.core/tr-class-fn
                :re-frame-datatable.core/footer-component
                :re-frame-datatable.core/empty-tbody-component
                :re-frame-datatable.core/drag-drop]} options]
  [:tr
   (merge
    {}
    (when tr-class-fn
      (css-class-str (tr-class-fn data-entry)))
    ;; Add the attributes for drag/drop operations, if any
    (drag-drop-attrs drag-drop data-entry))

   (when (enabled-key selection)
     [:td
      [:input {:type      "checkbox"
               :checked   (contains? (::selected-indexes selection) i)
               :on-change #(re-frame/dispatch [::change-row-selection db-id i checked?])}]])

   (doall
    (for [{:keys [:re-frame-datatable.core/column-key
                  :re-frame-datatable.core/render-fn
                  :re-frame-datatable.core/td-class-fn
                  :re-frame-datatable.core/td-attr-fn]} columns-def]
      ^{:key (str i \- column-key)}
      [:td
       (merge
        {}
        ;; If given, apply any custom attributes
        (when td-attr-fn
          (td-attr-fn (get-in data-entry column-key) data-entry))
        (when td-class-fn
          (css-class-str (td-class-fn (get-in data-entry column-key) data-entry))))

       (if render-fn
         [render-fn (get-in data-entry column-key) data-entry]
         (get-in data-entry column-key))]))]))

(defn- body [visible-items state db-id columns-def options]
  (if (empty? visible-items)
    [:tbody [empty-table columns-def state options]]
    ;; Non-empty table
    (->> visible-items
         (map (partial table-row db-id columns-def state options))
         (into [:tbody]))))

(defn render-table [db-id data-sub columns-def options]
  ;; Store initial configuration in db
  (re-frame/dispatch [::e/on-will-mount db-id data-sub columns-def options])
  (fn [& _]
    (let [view-data (re-frame/subscribe [::subs/data db-id data-sub (:re-frame-datatable.core/pagination options)])
          {:keys [:visible-items :state]} @view-data
          {:keys [:re-frame-datatable.core/table-classes
                  :re-frame-datatable.core/header-enabled?
                  :re-frame-datatable.core/footer-component]} options]

      [:table.re-frame-datatable
       (when table-classes
         (css-class-str table-classes))

       (when-not (= header-enabled? false)
         [header visible-items state db-id columns-def options])

       [body visible-items state db-id columns-def options]

       (when footer-component
         [:tfoot
          [footer-component]])])))
