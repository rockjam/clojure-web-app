(ns web-app.dev
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]
            [clojure.repl.deps :refer [sync-deps]]))

(comment
  (sync-deps) ;; reloads deps.edn

  (def ds (jdbc/get-datasource {:dbtype "sqlite" :dbname "./db/dev"}))
  (def ds-opts (jdbc/with-options ds {:builder-fn rs/as-unqualified-kebab-maps}))

  (jdbc/execute! ds ["drop table address"])
  (jdbc/execute! ds ["create table address (id INTEGER PRIMARY KEY, name varchar(32), last_name varchar(32))"])

  (jdbc/execute! ds ["insert into address (name, last_name) values('Jesse', 'Faden')"])
  (sql/insert! ds :address {:name "Alan" :last_name "Wake"})

  (sql/query ds-opts ["select * from address"])
  (sql/query ds-opts ["select count(*) as cnt from address"])

  (sql/find-by-keys ds-opts :address {:name "Jesse"})
  (sql/find-by-keys ds-opts :address ["name = ?", "Jesse"])
  (def address (jdbc/execute-one! ds-opts ["select * from address where name = ?", "Jesse"]))

  (:id address)
  (:name address)
  (:last-name address)

  (sql/get-by-id ds-opts :address 1)

  ; reusing the database connection
  (with-open [con (jdbc/get-connection ds-opts)]
    [(sql/get-by-id ds-opts :address 1) (sql/get-by-id ds-opts :address 2)])


  ,)
