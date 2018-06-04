# Merchant Offers Service

A simple RESTful software service that allows a merchant to create a new simple offer. Offers, once created, may be
queried. Offers expire after the period of time defined on the offer. Expired offers when queried have the 'expire' field set to 'Yes'.

The service allows a merchant to explicitly cancel an offer even before its expiry.

## Assumptions made
- Offer description are String/Text format for simplicity reasons. However in a fill fledged application, it may be a form modelled with a case class
- Cancelled offers are deleted
- Expired offers can still be viewed but have a Json field expired set to Yes,   

## Getting Started

To start up a local instance of the service use:
```bash
sbt run
```

The server would then be listening on port 8080 or whichever port is set in application.conf file

To create an offer:
```bash
curl -v -X POST \
  http://127.0.0.1:8080/offers \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{
  "offerId": "N/A",
  "description": "a description that exceeds 20 characters",
  "price": {
      "cost": "GBP 20"
  },
  "validity": {
      "startDate": "2018-06-03T16:11:39+01:00",
      "endDate": "2018-07-03T21:46:38+01:00"
  }
}'
```

To retrieve created offer:
```bash
curl -v -X GET \
  http://127.0.0.1:8080/offers/e069427a-4a0d-40cf-8018-534a1d9705c5 \
  -H 'cache-control: no-cache'
```

To update an existing offer:
```bash
curl -v -X PUT \
  http://127.0.0.1:8080/offers/e069427a-4a0d-40cf-8018-534a1d9705c5 \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{
  "offerId": "N/A",
  "description": "an updated description of the offer also exceeding 20 characters",
  "price": {
      "cost": "GBP 50"
  },
  "validity": {
      "startDate": "2018-06-03T16:11:39+01:00",
      "endDate": "2018-08-03T21:46:38+01:00"
  }
}'
```

To cancel an offer:
```bash
curl -v -X DELETE http://127.0.0.1:8080/offers/e069427a-4a0d-40cf-8018-534a1d9705c5 \
  -H 'cache-control: no-cache'
```

To get all offers:
```bash
curl -v http://127.0.0.1:8080/offers \
  -H 'cache-control: no-cache'
```

To cancel all offers:
```bash
curl -v -X DELETE http://127.0.0.1:8080/offers \
  -H 'cache-control: no-cache'
```

### Prerequisites 

* [Java 8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Scala 2.12.4](https://www.scala-lang.org/download/)
* [Sbt 1.1.4](https://www.scala-lang.org/download/)


## Running the tests

```bash
sbt clean test
```

### Break down of tests

Explain what these tests test and why

```
Give an example
```

### And coding style

Coding style adopted from: https://docs.scala-lang.org/style/


## Built With

* [Akka-http](http://www.dropwizard.io/1.0.2/docs/) - The web framework used
* [Scaffeine](https://maven.apache.org/) - In memory cache. Used instead of a database to store the offers for simplicity reasons
* [Sbt](https://rometools.github.io/rome/) - Build tool and dependency management

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

Semantic versioniog is not used   

## Authors

* **Kolade Adewuyi**


## Acknowledgments

* Hat tip to anyone whose code was used
* Inspiration
* etc
 