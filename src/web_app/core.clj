(ns web-app.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [clojure.pprint :as pprint]
            [clojure.repl.deps :refer [sync-deps]]
            [compojure.core :as comp]
            [compojure.route :as route]
            [hiccup2.core :as h]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]))

(defonce server (atom nil))
(defonce datasource (atom nil))

(defn make-datasource [db-path]
  (jdbc/with-options (jdbc/get-datasource {:dbtype "sqlite" :dbname db-path})
                     {:builder-fn rs/as-unqualified-kebab-maps}))

(defn prepare-database [ds]
  (jdbc/execute! ds
                 ["create table if not exists address (
                    id INTEGER PRIMARY KEY,
                    name varchar(32),
                    last_name varchar(32))"]))

(defn home-page []
  (str (h/html [:h1 "Homepage"]
               [:ul
                [:li [:a {:href "/users"} "Show users"]]
                [:li [:a {:href "/echo"} "Echo request"]]
                [:li [:a {:href "/greeting"} "Greeting"]]])))

(defn users-page []
  (let [users (sql/query @datasource ["select * from address"])]
    (str (h/html [:h1 "Users"]
                 [:div (for [u users] [:div [:p (:name u) " " (:last-name u)]])]))))

(comp/defroutes routes
                (comp/GET "/" [] {:status  200
                                  :body    (home-page)
                                  :headers {"Content-Type" "text/html; charset=UTF-8"}})
                (comp/GET "/users" [] {:status  200
                                       :body    (users-page)
                                       :headers {"Content-Type" "text/html; charset=UTF-8"}})
                (comp/ANY "/echo" req {:status  200
                                       :body    (with-out-str (pprint/pprint req))
                                       :headers {"Content-Type" "text/plain"}})
                (comp/GET "/greeting" [] {:status  200
                                          :body    (json/write-str {:greeting "Hello world"})
                                          :headers {"Content-Type" "application/json"}})
                (route/not-found "Not Found."))

(def app
  (-> routes
      wrap-keyword-params
      wrap-params))

(defn start-server [db-path]
  (let [ds (make-datasource db-path)
        s (jetty/run-jetty (fn [req] (app req))
                           {:port 3000 :join? false})]
    (do
      (prepare-database ds)
      (reset! server s)
      (reset! datasource ds))))

(defn stop-server []
  (when-some [s @server]
    (.stop s)
    (reset! server nil)
    (reset! datasource nil)))

(defn -main [& [db-path]]
  (start-server db-path))

(comment
  (sync-deps)

  (stop-server)
  (start-server "./db/dev")

  (sql/query @datasource ["select * from address"])
  (users-page)

  {})
