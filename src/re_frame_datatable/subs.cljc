(ns re-frame-datatable.subs
  (:require [re-frame.core :as re-frame]
            [re-frame-datatable.paths :as p]
            [re-frame-datatable.sorting :as s]
            [re-frame-datatable.defaults :as d]
            [re-frame-datatable.db :as db]))

(re-frame/reg-sub
 ::state
 (fn [db [_ db-id]]
   (db/state db db-id)))

(re-frame/reg-sub
 ::options
 (fn [db [_ db-id]]
   (db/options db db-id)))

(defn- apply-case-sensitivity
  "If case-insensitivity param is set to true lowercase passed string"
  [s case-insensitive]
  (if case-insensitive
    (clojure.string/lower-case s)
    s))

(defn- filter-row
  "Filter row data by checking if some value in a collection matches passed string

  Matching is adjusted using case sensitivity
  "
  [string coll case-insensitive]
  (some #(re-find (re-pattern (apply-case-sensitivity string case-insensitive))
           (apply-case-sensitivity (str %) case-insensitive))
    (vals (second coll))))

(re-frame/reg-sub
 ::data
 (fn data-gatherer [[_ db-id data-sub] _]
   [(re-frame/subscribe data-sub)
    (re-frame/subscribe [::state db-id])
    (re-frame/subscribe [::options db-id])])

 (fn data-provider [[items state options] _]
   (let [filter-data (fn [coll]
                       (let [{:keys [:re-frame-datatable.core/case-insensitive-filtering]} (:re-frame-datatable.core/filtering options)
                             {:keys [search-phrase]} (:filtering state)]
                         (if search-phrase
                           (filter #(filter-row search-phrase % case-insensitive-filtering) coll)
                           coll)))
         sort-data (fn [coll]
                     (let [{:keys [sort-key sort-comp sort-fn]} (:sorting state)]
                       (if sort-key
                         (cond->> coll
                           true (sort-by #(get-in (second %) sort-key) (or sort-fn compare))
                           (= ::s/sort-desc sort-comp) (reverse))
                         coll)))

         paginate-data (fn [coll]
                         ;; TODO Merge with pagination state
                         (let [{:keys [:re-frame-datatable.core/per-page
                                       :re-frame-datatable.core/enabled?]
                                :or {per-page d/default-per-page}} (:re-frame-datatable.core/pagination options)
                               {:keys [cur-page] :or {cur-page 0}} (:pagination state)
                               per-page (or (get-in state [:pagination :per-page])
                                            per-page)]
                           (if enabled?
                             (->> coll
                                  (drop (* (or cur-page 0) (or per-page 0)))
                                  (take (or per-page 0)))
                             coll)))]

     {:visible-items (->> items
                          (map-indexed vector)
                          (filter-data)
                          (sort-data)
                          (paginate-data))
      :state         state})))
