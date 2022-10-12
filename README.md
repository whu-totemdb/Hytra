# Hytra: managing trajectories in a hybrid way

## Supported Queries

Hytra is able to efficiently answer four typical types of queries now:

* Real-time range query
* Real-time kNN query
* Historical range query
* Trajectory similarity search, we support two similarity measures:
  * Longest overlapped road segments (LORS)
  * Longest overlapped cubes (LOC)

## Features of Framework

* Hytra stores trajectories with a LSM-ish component.
  * Optimized storage archtecure.
  * Key-Value store.
* Fast search with
  * Enriched indexes.
  * Various similarity measures.
* Trajectory visulization in New York City
  * http://shengwang.site/bus/

## Getting Started

### 0. Download the NYC Dataset

Our trajectory dataset collected at NYC is available. The dataset is pre-processed. 

### 1. Dependencies

We manage the dependent libraries with Maven. You can easily install those required softwares in pom.xml file.

### 2. Running the sample program

We provide a use case for Real-time RangeQuery. The `main()` method is in the `Engine` class in the InterfaceLay project.

```java
//1. Set the parameters.     
Params.put("city","nyc");     
Params.put("spatialDomain", new double[]{40.502873,-74.252339,40.93372,-73.701241});        
Params.put("resolution",6);        
Params.put("separator", "@");        
Params.put("epsilon", 30);        
Params.put("dataSize",(int) 1.2e7);

//2. Initialize the encoder and the generator with parameters.
 Encoder.setup(Params);
Generator.setup(Params);

//3. Execute query.
buildTrajDB((String) Params.get("city"), "jun");
RealtimeRange.setup(trajDataBase,Params,3000);
RealtimeRange.hytra(PostingList.GT,PostingList.TlP);
```

Hytra provides simple APIs for query processing.

* `buildTrajDB()` loads/builds the index structure of a dataset, encapsulated by `PostingList` class.
* `RealtimeRange.setup()` initializes the query paraemeters, including the length of the spatial range (3000).
* `RealtimeRange.hytra()` invokes API of a real-time range query. APIs for other supported query types are described in the next section.

### Query Types

#### 1) Real-time kNN query

```
RealtimekNN.setup(trajDB, params, K);
RealtimekNN.hytra(GTList);
```

The real-time kNN query is used to retrive k highest ranked trajectories based on the Euclidean distance. 

#### 2) Historical range query

```
HistoricalRange.generateQr(params, s_length, t_length);
HistoricalRange.hytra(planes);
```

The historical range query is used to retrive trajectories passing through a rectangular area and a period of time.

#### 3) Trajectory similarity search

```
Simiarity.LOC(tid, k, CT, TC);
Similarity.topkWithLORS(GT, TG, tid, k);
```

The real-time kNN query is used to retrive k highest ranked trajectories based on the specified similarity measure.

## Main Contributors

* Chen Wu
* Yuanke Hao
* Tiaoyao Wen

