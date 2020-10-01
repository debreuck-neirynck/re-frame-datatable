(ns re-frame-datatable.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame :refer [trim-v]]
            [cljs.spec.alpha :as s]
            [re-frame-datatable.events :as e]
            [re-frame-datatable.subs :as subs]
            [re-frame-datatable.sorting :as sorting]
            [re-frame-datatable.pagination :as p]
            [re-frame-datatable.selection]
            [re-frame-datatable.paths :refer :all]))


; --- Model (spec) ---

(s/def ::db-id keyword?)
(s/def ::enabled? boolean?)
(s/def ::css-classes (s/coll-of string?))


; columns-def

(s/def ::column-key (s/coll-of #(or (keyword? %) (int? %)) :kind vector))
(s/def ::column-label (s/or :string string?
                            :component fn?))
(s/def ::comp-fn fn?)
(s/def ::sorting (s/keys :req [::enabled?]
                         :opt [::comp-fn]))
(s/def ::td-class-fn fn?)


(s/def ::column-def
  (s/keys :req [::column-key]
          :opt [::sorting ::render-fn ::td-class-fn ::column-label]))

(s/def ::columns-def (s/coll-of ::column-def :min-count 1))


; options

(s/def ::table-classes ::css-classes)

(s/def ::per-page (s/and integer? pos?))
(s/def ::cur-page (s/and integer? (complement neg?)))
(s/def ::total-pages (s/and integer? pos?))
(s/def ::pagination
  (s/keys :req [::enabled?]
          :opt [::per-page ::cur-page ::total-pages]))

(s/def ::selected-indexes (s/coll-of nat-int? :kind set))
(s/def ::selection
  (s/keys :req [::enabled?]
          :opt [::selected-indexes]))

(s/def ::extra-header-row-component fn?)
(s/def ::footer-component fn?)
(s/def ::header-enabled? ::enabled?)

(s/def ::draggable? ::enabled?)
(s/def ::draggable-fn fn?)
(s/def ::drag-fn fn?)
(s/def ::drop-fn fn?)
(s/def ::drag-over-fn fn?)
(s/def ::drag-drop
  (s/keys :opt [::draggable? ::draggable-fn ::drag-fn ::drop-fn ::drag-over-fn]))

(s/def ::options
  (s/nilable
    (s/keys :opt [::pagination
                  ::header-enabled?
                  ::table-classes
                  ::selection
                  ::extra-header-row-component
                  ::footer-component
                  ::drag-drop])))


; --- Re-frame database paths ---

(def root-db-path [::re-frame-datatable])
(defn db-path-for [db-path db-id]
  (vec (concat (conj root-db-path db-id)
               db-path)))

(def columns-def-db-path (partial db-path-for [::columns-def]))
(def options-db-path (partial db-path-for [::options]))
(def state-db-path (partial db-path-for [::state]))
(def sort-key-db-path (partial db-path-for [::state ::sort ::sort-key]))
(def sort-comp-order-db-path (partial db-path-for [::state ::sort ::sort-comp]))
(def sort-comp-fn-db-path (partial db-path-for [::state ::sort ::sort-fn]))


; --- Utils ---
(defn css-class-str [classes]
  {:class (->> classes
               (filter (complement nil?))
               (clojure.string/join \space))})


; --- Views ---
; ----------------------------------------------------------------------------------------------------------------------

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

(defn datatable [db-id data-sub columns-def & [options]]
  {:pre [(or (s/valid? ::db-id db-id)
             (js/console.error (s/explain-str ::db-id db-id)))

         (or (s/valid? ::columns-def columns-def)
             (js/console.error (s/explain-str ::columns-def columns-def)))

         (or (s/valid? ::options options)
             (js/console.error (s/explain-str ::options options)))]}

  (let [view-data (re-frame/subscribe [::subs/data db-id data-sub])]

    (reagent/create-class
      {:component-will-mount
       #(re-frame/dispatch [::on-will-mount db-id data-sub columns-def options])


       :component-did-update
       (fn [this]
         (let [[_ db-id data-sub columns-def options] (reagent/argv this)]
           (re-frame/dispatch [::on-did-update db-id data-sub columns-def options])
           (when (not= (get-in @view-data [::state ::total-items]) (count @(re-frame/subscribe data-sub)))
             (re-frame/dispatch [::select-page db-id @(re-frame/subscribe [::pagination-state db-id data-sub]) 0]))))


       :component-will-unmount
       #(re-frame/dispatch [::on-will-unmount db-id])


       :reagent-render
       (fn [db-id data-sub columns-def & [options]]
         (let [{:keys [::visible-items ::state]} @view-data
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
               [footer-component]])]))})))
