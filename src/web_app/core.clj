(ns web-app.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [clojure.data.json :as json]
            [clojure.pprint :as pprint]
            [compojure.core :as comp]
            [compojure.route :as route]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]))

(defonce server (atom nil))

(comp/defroutes routes
                (comp/GET "/" [] {:status  200
                                  :body    "<h1>Homepage</h1>
                     <ul>
                       <li><a href=\"/echo\">Echo request</a></li>
                       <li><a href=\"/greeting\">Greeting</a></li>
                     </ul>"
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

(defn start-server []
  (reset! server
          (jetty/run-jetty (fn [req] (app req))
                           {:port 3000 :join? false})))
(defn stop-server []
  (when-some [s @server]
    (.stop s)
    (reset! server nil)))

(defn -main [& args]
  (start-server))
