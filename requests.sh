curl localhost:8080/health-check
curl -X POST -H "Content-Type: application/json" -d '{"id" : 158, "date" : "2020-12-02T14:49:15.841609", "last" : 0.0, "close": 0.0, "diff": 0.0, "curr": "PESO", "unit": "PESO", "dollarBN": 148.0, "dollarItau": 123.0, "wDiff": 0.0}' localhost:8080/score
