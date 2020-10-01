(ns re-frame-datatable.rendering
  "Rendering functions for the table"
  (:require [re-frame.core :as re-frame]
            [re-frame-datatable.subs :as subs]
            [re-frame-datatable.sorting :as sorting]))

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

(defn render-table [db-id data-sub columns-def & [options]]
  (let [view-data (re-frame/subscribe [::subs/data db-id data-sub])
        {:keys [::visible-items ::state]} @view-data
        {:keys [::selection]} state
        {:keys [::table-classes
                ::tr-class-fn
                ::header-enabled?
                ::extra-header-row-component
                ::footer-component
                ::empty-tbody-component
                ::drag-drop]} options]

    [:table.re-frame-datatable
     (when table-classes
       (css-class-str table-classes))

     (when-not (= header-enabled? false)
       [:thead
        (when extra-header-row-component
          [extra-header-row-component])

        [:tr
         (when (::enabled? selection)
           [:th {:style {:max-width "16em"}}
            [:input {:type      "checkbox"
                     :checked   (clojure.set/subset?
                                 (->> visible-items (map first) (set))
                                 (::selected-indexes selection))
                     :on-change #(when-not (zero? (count visible-items))
                                   (re-frame/dispatch [::change-table-selection
                                                       db-id
                                                       (->> visible-items (map first) (set))
                                                       (-> % .-target .-checked)]))}]
            [:br]
            [:small (str (count (::selected-indexes selection)) " selected")]])

         (doall
          (for [{:keys [::column-key ::column-label ::sorting]} columns-def]
            ^{:key (str column-key)}
            [:th
             (merge
              (when (::enabled? sorting)
                {:style    {:cursor "pointer"}
                 :on-click #(re-frame/dispatch [::sorting/set-sort-key db-id column-key (::comp-fn sorting)])
                 :class    "sorted-by"})
              (when (= column-key (get-in state [::sort ::sort-key]))
                (css-class-str ["sorted-by"
                                (condp = (get-in state [::sort ::sort-comp])
                                  ::sort-asc "asc"
                                  ::sort-desc "desc"
                                  "")])))
             (cond
               (string? column-label) column-label
               (fn? column-label) [column-label]
               :else "")]))]])


     [:tbody
      (if (empty? visible-items)
        [:tr
         [:td {:col-span (+ (count columns-def)
                            (if (::enabled? selection) 1 0))
               :style    {:text-align "center"}}
          (if empty-tbody-component
            [empty-tbody-component]
            "no items")]]

        (doall
         (for [[i data-entry] visible-items]
           ^{:key i}
           [:tr
            (merge
             {}
             (when tr-class-fn
               (css-class-str (tr-class-fn data-entry)))
             ;; Add the attributes for drag/drop operations, if any
             (drag-drop-attrs drag-drop data-entry))

            (when (::enabled? selection)
              [:td
               [:input {:type      "checkbox"
                        :checked   (contains? (::selected-indexes selection) i)
                        :on-change #(re-frame/dispatch [::change-row-selection db-id i (-> % .-target .-checked)])}]])

            (doall
             (for [{:keys [::column-key ::render-fn ::td-class-fn ::td-attr-fn]} columns-def]
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
                  (get-in data-entry column-key))]))])))]

     (when footer-component
       [:tfoot
        [footer-component]])]))
