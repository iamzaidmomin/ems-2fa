Plan for Spring Boot + Kafka + Avro + OpenBao + GitLeaks

1. Add Maven deps and Avro schema
   - Add `spring-kafka`, `avro`, and Confluent Avro serializer to `employee-mgmt/pom.xml`.
   - Add `employee_event.avsc` under `employee-mgmt/src/main/avro` and configure `avro-maven-plugin`.
2. Implement Kafka integration in the Java app
   - Add `KafkaConfig` to configure producer and consumer factories using Schema Registry.
   - Add `ProducerService` to publish `EmployeeEvent` Avro messages.
   - Add `ConsumerService` to consume `employee-events` and log receipts.
   - Wire `ProducerService` into `EmployeeService` to publish on create.
3. Multi-container Docker environment
   - Update `docker-compose.yml` to add `zookeeper`, `kafka`, `schema-registry`, `kafka-ui`, `openbao`, `twofa`, `employee-mgmt`, and `gitleaks`.
4. Run and demo
   - Build and start containers with `docker compose up --build`.
   - Open dashboards:
     - Kafka UI: http://localhost:9000 (topics, partitions, consumers)
     - Schema Registry: http://localhost:8081
     - OpenBao: http://localhost:8200
   - Use REST to create an employee (POST /api/employees) to produce an Avro event.
   - Observe events in Kafka UI and consumer logs.
5. Secrets scanning
   - `gitleaks` runs a repo scan and writes `/reports/gitleaks-report.json` to the `gitleaks-reports` volume.

Files changed/added
- employee-mgmt/pom.xml (deps + avro plugin)
- employee-mgmt/src/main/avro/employee_event.avsc
- employee-mgmt/src/main/java/com/twofa/ems/config/KafkaConfig.java
- employee-mgmt/src/main/java/com/twofa/ems/kafka/ProducerService.java
- employee-mgmt/src/main/java/com/twofa/ems/kafka/ConsumerService.java
- employee-mgmt/src/main/java/com/twofa/ems/service/EmployeeService.java (publish events)
- employee-mgmt/src/main/resources/application.yml (kafka properties)
- docker-compose.yml (add kafka stack, kafka-ui, gitleaks)

Run/verify commands

1) Build & start all services

```bash
docker compose up --build -d
```

2) Verify services

- Kafka UI: http://localhost:9000
- Schema Registry: http://localhost:8081
- OpenBao: http://localhost:8200
- Employee API: http://localhost:8080/api/employees

3) Create an employee (example)

```bash
curl -s -X POST http://localhost:8080/api/employees \
  -H 'Content-Type: application/json' \
  -d '{"name":"Alice","email":"alice@example.com","department":"Eng","position":"SWE"}' | jq
```

4) Watch Kafka UI for `employee-events` topic and check `employee-mgmt` logs:

```bash
docker compose logs -f employee-mgmt
```

5) Get gitleaks report

```bash
docker compose run --rm gitleaks detect --source /repo --report-format json --report-path /reports/gitleaks-report.json
```

Notes
- The Avro Java classes are generated during `mvn package` by the `avro-maven-plugin` included in the `employee-mgmt` build stage of its Dockerfile. The `employee-mgmt` image build will run Maven and produce generated sources.
- `kafka-ui` provides a web UI to inspect topics and partitions. Schema Registry UI is limited but accessible on port 8081.
- This setup uses Confluent community images for Kafka and Schema Registry and `provectuslabs/kafka-ui` for the dashboard.

Next steps (optional)
- Add transactional producer and exactly-once semantics testing.
- Add consumer group partition rebalancing tests and multiple consumer instances.
- Add monitoring (Prometheus + Grafana) for Kafka metrics.
