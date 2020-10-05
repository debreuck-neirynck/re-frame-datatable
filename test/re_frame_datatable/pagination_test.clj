(ns re-frame-datatable.pagination-test
  (:require [midje.sweet :refer :all]
            [re-frame-datatable
             [core :as dt]
             [pagination :as pag]
             [paths :as p]]
            [re-frame
             [core :as rf]
             [db :refer [app-db]]]))

(rf/clear-subscription-cache!)

(rf/reg-sub
 ::test-items
 (fn [_ _]
   []))

(facts "about `pagination-state`"
       (let [s (rf/subscribe [::dt/pagination-state ::test-id [::test-items]])]

         (fact "exists"
               s => some?)

         (fact "returns pagination info"
               @s => map?
               (keys @s) => (just [::dt/pages ::dt/cur-page ::dt/per-page] :in-any-order))

         (fact "default number of items per page"
               (::dt/per-page @s) => number?)

         (fact "uses settings from state"
               (reset! app-db (assoc {}
                                     (p/state-db-path ::test-id)
                                     {::dt/per-page 10
                                      ::p/cur-page 7}))
               @s => (contains {::dt/per-page 10
                                ::dt/cur-page 7}))))
