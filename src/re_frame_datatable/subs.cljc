(ns re-frame-datatable.subs
  (:require [re-frame.core :as re-frame]
            [re-frame-datatable.paths :as p]
            [re-frame-datatable.sorting :as s]
            [re-frame-datatable.defaults :as d]
            [re-frame-datatable.db :as db]
            [re-frame-datatable.utils :as u]))

(re-frame/reg-sub
 ::state
 (fn [db [_ db-id]]
   (db/state db db-id)))

(re-frame/reg-sub
 ::options
 (fn [db [_ db-id]]
   (db/options db db-id)))

(defn- filter-row
  "Using search phrase filters row-map `{:name John, :city Brussels}`
   by passing map values to a filter function.

   Filter function should accept a search-phrase and return a function that takes in a string to compare."
  [search-phrase row-map filter-fn]
  (some (filter-fn search-phrase)
    (vals row-map)))

(re-frame/reg-sub
 ::data
 (fn data-gatherer [[_ db-id data-sub] _]
   [(re-frame/subscribe data-sub)
    (re-frame/subscribe [::state db-id])
    (re-frame/subscribe [::options db-id])])

 (fn data-provider [[items state options] _]
   (let [filter-data (fn [coll]
                       (let [{:keys [:re-frame-datatable.core/enabled?
                                     :re-frame-datatable.core/filtering-fn]}
                             (:re-frame-datatable.core/filtering options)
                             {:keys [search-phrase]} (:filtering state)]
                         (if (and enabled? search-phrase)
                           (filter #(filter-row search-phrase (second %) (or filtering-fn u/case-sensitive-filtering-fn)) coll)
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
