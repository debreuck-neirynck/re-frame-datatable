(ns re-frame-datatable.subs-test
  (:require [re-frame-datatable
             [core :as dt]
             [subs :as sut]
             [paths :as p]
             [db :as db]]
            [midje.sweet :refer :all]
            [re-frame
             [core :as rf]
             [db :refer [app-db]]]))

(rf/clear-subscription-cache!)

(facts "about `state`"
       (let [id ::test-id
             s (rf/subscribe [::sut/state id])]
         
         (fact "exists"
               s => some?)

         (fact "returns current component state"
               (let [state {:key :value}]
                 (reset! app-db (db/set-state {} id state)) => map?
                 @s => state))))

         ;; (fact "passes pagination info"
         ;;       (let [s (rf/subscribe [::sut/state id])]
         ;;         (::p/pagination @s) => {:key :pagination}))

         ;; (fact "keeps existing pagination info"
         ;;       (let [s (rf/subscribe [::sut/state id {:key :pagination}])]
         ;;         (reset! app-db (assoc-in {} (p/db-path-for [::p/state ::p/pagination :existing] id) :value))
         ;;         (::p/pagination @s) => (contains {:existing :value})))))

(facts "about `data`"
       (let [id ::test-id
             sub-id ::test-sub]
         (rf/reg-sub sub-id (fn [db _] (::items db)))

         (let [d (rf/subscribe [::sut/data id [sub-id]])]
           
           (fact "exists"
                 d => some?)

           (fact "provides state"
                 (reset! app-db (db/set-state {} id {:key :value})) => truthy
                 (:state @d) => {:key :value})

           (fact "provides visible items as indexed vector"
                 (swap! app-db assoc ::items [..item-1.. ..item-2..])
                 (:visible-items @d) => [[0 ..item-1..]
                                         [1 ..item-2..]])

           (fact "sorts data by key"
                 (swap! app-db (fn [db]
                                 (-> db
                                     (assoc ::items [{:value 2} {:value 1}])
                                     (db/set-sorting id {:sort-key [:value]})))) => map?
                 (:visible-items @d) => [[1 {:value 1}]
                                         [0 {:value 2}]])

           (fact "applies sort-fn if given"
                 (swap! app-db (fn [db]
                                 (-> db
                                     (assoc ::items [{:value "first"} {:value "second"}])
                                     (db/set-sorting id {:sort-key [:value]
                                                         :sort-fn (fn [a b]
                                                                    (compare (last a) (last b)))})))) => map?
                 (:visible-items @d) => [[1 {:value "second"}]
                                         [0 {:value "first"}]])

           (let [d (rf/subscribe [::sut/data id [sub-id]])]
             (fact "applies pagination"
                   (reset! app-db (-> {}
                                      (assoc ::items (->> (range 100)
                                                          (map (fn [v] {:item v}))))
                                      (db/set-options id {::dt/pagination {::dt/enabled? true
                                                                           ::dt/per-page 5}}))) => map?
                   (count (:visible-items @d)) => 5)

             (fact "returns current page"
                   (swap! app-db (fn [db]
                                   (-> db
                                       (assoc ::items (->> (range 100)
                                                           (map (fn [v] {:item v}))))
                                       (db/set-options id {::dt/pagination {::dt/enabled? true
                                                                            ::dt/per-page 5}})
                                       (db/set-state id {:pagination {:cur-page 1}})))) => map?
                   (-> (:visible-items @d)
                       (first)) => [5 {:item 5}])))))
