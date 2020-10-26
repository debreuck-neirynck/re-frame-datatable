(ns re-frame-datatable-docs.sections.filtering
  (:require [re-frame-datatable-docs.components :as components]
            [re-frame-datatable-docs.formatters :as formatters]
            [re-frame-datatable-docs.table-views :as table-views]
            [cljs.repl :as r]

            [re-frame-datatable-docs.subs :as subs]
            [re-frame-datatable.core :as dt]
            [re-frame-datatable.views :as dt-views]
            [re-frame-datatable.utils :as dt-utils]))

(defn enabling-filtering []
  (let [dt-key :filtering-basic
        dt-sub [::subs/pagination-data]]

    [:div
     [:div
      "Filtering can be enabled via " [:code.inline-code "::filtering"] " key. There are 2 options:"
      [:ul.ui.list
       [:li [:code.inline-code "::enabled?"] " - boolean to define if filtering should be enabled"]
       [:li [:code.inline-code "::filtering-fn"] " - function to define how to filter row data (default is case sensitive)"]]]

     [components/warning-message
      [:div
       [:p
        "DataTable's filtering controls are not rendered together with DataTable. There are 2 reasons for it:"]
       [:ul
        [:li "DataTable is not opinionated about where to put filtering controls"]
        [:li "DataTable is not opinionated how filtering controls will look like"]]]
      "Important"]

     [:p
      "DataTable ships with additional component " [:code.inline-code "default-filtering-controls"]
      ", that accepts 2 arguments: " [:code.inline-code "datatable-key"] " and " [:code.inline-code "data-sub"]
      " which should be the same as passed to " [:code.inline-code "datatable"] " component itself"]

     [components/tabs-wrapper
      dt-key
      dt-sub
      [{::dt/column-key   [:index]
        ::dt/column-label "#"}
       {::dt/column-key   [:name]
        ::dt/column-label "Name"}
       {::dt/column-key   [:stats :play_count]
        ::dt/column-label "Play count"}]
      {::dt/filtering    {::dt/enabled? true}
       ::dt/table-classes ["ui" "table"]}
      [{:data-tab  "default-filtering-controls-usage"
        :label     "Default Filtering Controls Usage"
        :component (fn []
                     [:pre
                      [:code {:class "clojure"}
                       (str
                         "[dt-views/default-filtering-controls "
                         dt-key
                         \space
                         dt-sub)]])}]
      (fn [dt-def]
        [:div.ui.grid
         [:div.row
          [:div.right.aligned.column
           [dt-views/default-filtering-controls dt-key dt-sub]]]
         [:div.row
          [:div.column
           dt-def]]
         [:div.row
          [:div.right.aligned.column
           [dt-views/default-filtering-controls dt-key dt-sub]]]])]]))

(defn case-insensitive-filtering []
  (let [dt-key :filtering-case-insensitive
        dt-sub [::subs/pagination-data]]

    [:div
     [:div
      [:p
       "It is possible to filter rows case insensitive. There is a built in " [:code.inline-code "case-insensitive-filtering-fn"]
       " in the " [:code.inline-code "utils"] " namespace."]
      [:p "See the example below to see how case-insensitive filtering is defined."]]

     [components/tabs-wrapper
      dt-key
      dt-sub
      [{::dt/column-key   [:index]
        ::dt/column-label "#"}
       {::dt/column-key   [:name]
        ::dt/column-label "Name"}
       {::dt/column-key   [:stats :play_count]
        ::dt/column-label "Play count"}]
      {::dt/filtering     {::dt/enabled? true
                           ::dt/filtering-fn
                           dt-utils/case-insensitive-filtering-fn}
       ::dt/table-classes ["ui" "table"]}
      [{:data-tab  "case-insensitive-filtering-controls-usage"
        :label     "Case-insensitive Filtering Controls Usage"
        :component (fn []
                     [:div
                      [:pre
                       [:code {:class "clojure"}
                        (str
                          "[dt-views/default-filtering-controls "
                          dt-key
                          \space
                          dt-sub)]]])}]
      (fn [dt-def]
        [:div.ui.grid
         [:div.row
          [:div.right.aligned.column
           [dt-views/default-filtering-controls dt-key dt-sub]]]
         [:div.row
          [:div.column
           dt-def]]
         [:div.row
          [:div.right.aligned.column
           [dt-views/default-filtering-controls dt-key dt-sub]]]])

      ]]))

(defn custom-filtering-controls []
  (let [dt-key :filtering-custom-controls
        dt-sub [::subs/pagination-data]]

    [:div
     [:div
      [:p
       "It is possible to build your own custom filtering controls. DataTable exposes single subscription - " [:code.inline-code "::filtering-state"]
       " - that accepts 2 arguments: " [:code.inline-code "datatable-key"] " and " [:code.inline-code "data-sub"] "."
       " That subscription returns a map with following key:"]
      [:ul
       [:li [:code.inline-code "::cur-input-filter-val"] " - a string, which represents current value from input field (defaults to \"\")"]]
      [:p "You should define two actions on your html elements:"]
      [:ul
       [:li [:code.inline-code "on-change"] " - action that dispatches " [:code.inline-code "::set-input-filter-val"]
        " that sets new filter value in the state"]
       [:li [:code.inline-code "on-click"] " - action that dispatches " [:code.inline-code "::filter-table"]
        " that filters table data and updates the table"]]
      [:p "See the example below to see how default and custom filtering components are defined."]]

     [components/tabs-wrapper
      dt-key
      dt-sub
      [{::dt/column-key   [:index]
        ::dt/column-label "#"}
       {::dt/column-key   [:name]
        ::dt/column-label "Name"}
       {::dt/column-key   [:stats :play_count]
        ::dt/column-label "Play count"}]
      {::dt/filtering     {::dt/enabled? true}
       ::dt/table-classes ["ui" "table"]}
      [{:data-tab  "filtering-controls-source"
        :label     "Filtering Controls Source"
        :component (fn []
                     [formatters/formatted-function-def
                      (with-out-str (r/source table-views/basic-filtering-controls))])}]
      (fn [dt-def]
        [:div.ui.two.column.grid
         [:div.column dt-def]
         [:div.column
          [:div {:style {:margin-bottom "1em"}}
           [:h5.ui.herader "Default filtering controls"]
           [dt-views/default-filtering-controls dt-key dt-sub]]

          [:div {:style {:margin-bottom "1em"}}
           [:h5.ui.herader "Basic filtering controls"]
           [table-views/basic-filtering-controls dt-key dt-sub]]

          ]])]]))
