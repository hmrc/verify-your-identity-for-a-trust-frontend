# Verify your identity for a trust frontend

This service is responsible for navigating the user to trusts-relationship-establishment-frontend. It determines if the user has authority to maintain a trust based on information previously provided in the last registration or update.

To run locally using the micro-service provided by the service manager:

***sm2 --start TRUSTS_ALL -r***

If you want to run your local copy, then stop the frontend ran by the service manager and run your local code by using the following (port number is 9789 but is defaulted to that in build.sbt).

`sbt run`

## Testing the service
Run unit and integration tests before raising a PR to ensure your code changes pass the Jenkins pipeline. This runs all the unit tests and integration tests with scalastyle and checks for dependency updates:

`./run_all_tests.sh`


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
