# db2eventstore-kafka

## Pre-requisites
```
Install sbt at the version 0.13.16
git clone git@github.com:IBMProjectEventStore/db2eventstore-kafka.git
cd db2eventstore-kafka
```

## Compile
```
sbt clean
sbt compile
```

## Package
```
sbt package assembly
```

## Run the Data Generator

### Kafka Broker
This example comes pre-packaged with an internal broker. However, you can also connect it to an external Kafka cluster. You will need to set the arguments localBroker accordingly
- true for internal Broker
- false for an external Broker

### Run command
```
sbt "dataLoad/run -localBroker true -kafkaBroker localhost:9092 -tableName sensor -topic estopic -group group -metadata sensor -metadataId 238 -batchSize 1000"
```

*Options*
- tableName [String] - The table name that will be created in the IBM Db2 Event Store
- localBroker [Boolean]- true to use of an internal, single node, Kafka broker. False to use an externally configured Broker
- kafkaBroker [String]- Location of the Kafka broker, pass in "localhost:9092" for a local setup or the ip:port location for an external setup
- topic [String]- The Kafka topic to be used for the stream
- group [String]- The Kafka group to be used for the stream
- metadata [String]- The type of metadata for this IoT device, for instance "sensor" or "appliance" or "weatherstation"
- metadataId [Long]- The Long value for this IoT device, for instance "sensor" or "appliance" or "weatherstation"
- batchSize [Int]

### Underlying Json format
The current format for the generator is a JSON string, like this:
s"""{"table":"${tableName}", "payload":{"id": ${numRec}, "${metadata}": ${metadataId}, "timestamp":${System.currentTimeMillis()}, "value":${value}}}"""

## Run the Db2 Event Store Streaming Connector

This connector could be run standalone, without the generator above. However, the row format will match the JSON, mentioned above.

### Run command
```
sbt "eventStream/run -localBroker true -kafkaBroker localhost:9092 -topic estopic -eventStore 9.30.45.34:1101,9.30.45.35:1101,9.30.123.179:1101 -database TESTDB -user admin  -password password -metadata sensor -streamingInterval 5000 -batchSize 1000"
```

*Options*
- localBroker [Boolean]- true to use of an internal, single node, Kafka broker. False to use an externally configured Broker
- kafkaBroker [String]- Location of the Kafka broker, pass in "localhost:9092" for a local setup or the ip:port location for an external setup
- topic [String]- The Kafka topic to be used for the stream
- eventStore [String]- The IP configuration for the IBM Db2 Event Store
- database [String]- The Db2 Event Store Database to create or use
- user [String]- The Db2 Event Store user name to use
- password [String]- The Db2 Event Store password to use
- metadata [String]- The type of metadata for this IoT device, for instance "sensor" or "appliance" or "weatherstation"  
- streamingInterval [Long]- The Long value defining the length of the Apache Spark streaming window
- batchSize [Int] - The size of the batch to send to the IBM Db2 Event Store

## IBM Db2 Event Store Table definition

Currently, the table that will be created in the event store will have the following format. It is designed to support a payload from an IoT device of type "metadata" that sends a value at a frequent interval. The timestamp will be automatically generated by the loader.

```
val tableDefinition = TableSchema(tableName, StructType(Array(
  StructField("id", LongType, nullable = false),
  StructField(s"${metadata}", LongType, nullable = false),
  StructField("timestamp", LongType, nullable = false),
  StructField("value", LongType, nullable = false)
)),
  shardingColumns = Seq("timestamp", s"${metadata}"),
  pkColumns = Seq("timestamp", s"${metadata}"))
val indexDefinition = IndexSpecification("IndexDefinition", tableDefinition, equalColumns = Seq(s"${metadata}"), sortColumns = Seq(SortSpecification("timestamp", ColumnOrder.AscendingNullsLast)), includeColumns = Seq("value"))
```

## IBM Db2 Event Store supported queries

Such a format will allow for running real-time queries such as this one below, where an index is defined on the timestamp and the metadata columns
```
SELECT value, timestamp from ${tableName} where ${metadata}=${metadataId} and timestamp>=${timestampValueinMS} and timestamp<=${timestampValueinMS}
```
