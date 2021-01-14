# eviction-cache-lib
  
#### Used tools  
* Maven 3.6.3
* JDK 1.8

---
  
#### Setup instructions  
##### 1. Install using maven 
* `mvn clean install`  

#### Description

Main class - EvictionCache implements Map interface.
You can configure how many working threads it is allowed to have in the pool using concurrencyLevel property.
Life time duration and measurement unit can be configured via expirationTimeValue & expirationTimeUnit.
Most important test cases (including concurrency) located in EvictionCacheTest.java


#### Further improvements

I was trying to keep this solution as simple as possible due to time limitations, however there are many ways to improve it.
Most significant are following:

* Bringing data partitioning mechanism, so a dedicated thread can deal with dedicated partition only (currently runnables are being placed LinkedBlockingQueue and processed via thread pool).
* Passive mode (everything can be processed at main thread, eviction logic could be triggered only when put, get or remove is requested)
