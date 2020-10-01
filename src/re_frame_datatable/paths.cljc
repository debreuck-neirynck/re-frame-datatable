(ns re-frame-datatable.paths
  "Re-frame database path functions")

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
