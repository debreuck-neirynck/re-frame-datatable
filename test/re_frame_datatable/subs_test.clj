(ns re-frame-datatable.subs-test
  (:require [re-frame-datatable
             [subs :as sut]
             [paths :as p]]
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
               (reset! app-db (assoc-in {} (p/state-db-path id) ..state..)) => truthy
               @s => ..state..)))

(facts "about `data`"
       (let [id ::test-id
             sub-id ::test-sub]
         (rf/reg-sub sub-id (fn [db _] (::items db)))

         (let [d (rf/subscribe [::sut/data id [sub-id]])]
           
           (fact "exists"
                 d => some?)

           (fact "provides state"
                 (reset! app-db (assoc-in {} (p/state-db-path id) ..state..)) => truthy
                 (:state @d) => ..state..)

           (fact "provides visible items as indexed vector"
                 (swap! app-db assoc ::items [..item-1.. ..item-2..])
                 (:visible-items @d) => [[0 ..item-1..]
                                         [1 ..item-2..]]))))
