retailplanner
=============

Solution for Retail Event Planning APEX Class for [Cloudspokes](http://www.cloudspokes.com/challenges/1650).

**Functionality:**

Clones promotion event records for the specified planning session
The input is the _current_ planning session, so records will be cloned
for the _next_ half-year period from the previous year's historical data.
Note: the `actual_amount__c` field does not get cloned.

**Signature:**

```java
public static Boolean RetailPlanner.cloneSession(Integer year, Integer half)
```

**For example:**

If the current planning session is 1H2012, then one can call
the cloning for 2H2012 (cloning historic data from 2H2011) with:

```java
RetailPlanner.cloneSession(2012, 1);
```

Unit tests are included in the class, code coverage is 90%.
