(ns ^:dev/always re-frame-datatable.core
  (:require [cljs.spec.alpha :as s]
            [re-frame-datatable.events :as e]
            [re-frame-datatable.subs :as subs]
            [re-frame-datatable.sorting :as sorting]
            [re-frame-datatable.pagination :as p]
            [re-frame-datatable.selection]
            [re-frame-datatable.rendering :as r]))


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


; --- Views ---
; ----------------------------------------------------------------------------------------------------------------------

(defn datatable [db-id data-sub columns-def & [options]]
  {:pre [(or (s/valid? ::db-id db-id)
             (js/console.error (s/explain-str ::db-id db-id)))

         (or (s/valid? ::columns-def columns-def)
             (js/console.error (s/explain-str ::columns-def columns-def)))

         (or (s/valid? ::options options)
             (js/console.error (s/explain-str ::options options)))]}

  (r/render-table db-id data-sub columns-def options))
