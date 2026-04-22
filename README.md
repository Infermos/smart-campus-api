# Smart Campus Sensor and Rooms Management API

---

## Overview

The project deploys a RESTful API with JAX-RS (Jersey) to operate a Smart Campus system at a university. It aids in the control of Rooms, Sensors and Sensor Readings by appropriate validation, error handling and logging.
The API adheres to the principles of REST such as resource-based design, correct use of the HTTP methods, and meaningful status codes.

## Technology Stack

- Java
- JAX-RS (Jersey)
- Maven
- In memory storage (Hashmaps and ArrayList)

---


## Setup Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/Infermos/smart-campus-api.git
   ```

2. Open in Net Beans

3. Run the server (Tomcat / embedded)

4. Access base endpoint:
   ```
   http://localhost:8080/smart-campus-api/api/v1/
   ```

---

## Sample cURL Commands

### Create Room
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
-H "Content-Type: application/json" \
-d '{"id":"LIB-301","name":"Library","capacity":50}'
```

### Get Rooms
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms
```

### Create Sensor
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
-H "Content-Type: application/json" \
-d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":22.5,"roomId":"LIB-301"}'
```

### Filter Sensors by Type
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/sensors?type=Temperature
```

### Add Sensor Reading
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings \
-H "Content-Type: application/json" \
-d '{"id":"R001","timestamp":1710000000,"value":26.5}'
```

### View Live Logs
```bash
curl http://localhost:8080/smart-campus-api/api/v1/logs
```


---




## Report

---

## Part 1: Service Architecture and Setup

#### 1. Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

By default, JAX-RS creates a **new instance** of a resource class for **every request** (per-request lifecycle). This means that each HTTP request triggers the instantiation of a fresh resource object, which is then used to handle that specific request and discarded afterward.

However, in-memory data structures (the HashMaps and ArrayLists in `DataStore.java`) are declared as **static** fields, which means they exist at the class level and are shared across all instances. This design choice has important implications:

**Impact on Data Management:**
- **Persistence Across Requests:** Because DataStore fields are static, data persists between requests even though resource instances are recreated
- **Thread Safety Concerns:** Multiple concurrent requests could access the same HashMap simultaneously, creating potential race conditions
- **Synchronization Requirements:** For production use, I would need to use `ConcurrentHashMap` instead of `HashMap`, or synchronize access to these data structures

In the current implementation, I use standard HashMap which is acceptable for demonstration purposes but would require synchronization mechanisms (like `Collections.synchronizedMap()` or `ConcurrentHashMap`) in a production environment with high concurrency.

---

#### 2. Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

HATEOAS (Hypermedia as the Engine of Application State) is a constraint of REST architecture where the server provides clients with links to related resources within API responses, enabling dynamic navigation without hardcoding URLs.

**Benefits for Client Developers:**

- **Discoverability:** Clients can navigate the API by following links rather than constructing URLs manually, reducing the risk of errors
- **Loose Coupling:** Clients depend on link relationships rather than URL structure, allowing the server to change endpoint paths without breaking clients
- **Self-Documentation:** The API becomes partially self-documenting as responses indicate available actions and related resources
- **Reduced Dependency on Documentation:** Clients don't need to reference external documentation to understand what operations are possible from a given state

In the implementation, the Discovery endpoint (`GET /api/v1/`) provides a map of primary resource collections, demonstrating basic hypermedia principles by telling clients where to find rooms and sensors endpoints rather than requiring them to guess or read documentation.

---

### Part 2: Room Management

#### 3. When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.

**Returning Full Objects:**
- **Pros:** Client gets all data in one request, reducing the need for additional API calls
- **Cons:** Larger payload size, more network bandwidth consumption, may include unnecessary data the client doesn't need

**Returning Only IDs:**
- **Pros:** Minimal bandwidth usage, faster response times, clients fetch only what they need
- **Cons:** Requires additional requests to get full details, increases total number of HTTP requests (N+1 problem)

**Implementation:**
I return full room objects in `GET /api/v1/rooms` because:
- Room objects are relatively small (ID, name, capacity, sensor ID list)
- Clients typically need this basic information for display purposes
- The performance cost is minimal compared to the convenience

For larger datasets or objects with heavy nested data, a hybrid approach might be better: return summary information in lists and provide full details via individual GET requests.

---

#### 4. Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

Yes, DELETE implementation is **idempotent**.

**Here's what happens with repeated DELETE requests:**

**First DELETE request:** `DELETE /api/v1/rooms/LIB-301`
- Room exists and has no sensors
- `DataStore.rooms.remove("LIB-301")` removes the room
- Returns `200 OK`

**Second DELETE request:** `DELETE /api/v1/rooms/LIB-301`
- Room no longer exists (`room` is null)
- The null check fails, so the sensor check is skipped
- `DataStore.rooms.remove("LIB-301")` is called but has no effect (removing a non-existent key)
- Returns `200 OK`

The operation is idempotent because making the same request multiple times produces the same result: the room is deleted. Whether the room was deleted by the first request or was already absent doesn't matter - the final state is identical (room does not exist), satisfying the idempotency requirement.

**Note:** A more RESTful approach might return `404 Not Found` on subsequent requests to indicate the resource no longer exists, but returning `200 OK` is also acceptable and maintains idempotency.

---

### Part 3: Sensor Operations & Linking

#### 5. I explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

When a client sends data with a `Content-Type` header that doesn't match the `@Consumes` annotation, JAX-RS performs content negotiation and rejects the request.

**What Happens:**

1. Client sends: `Content-Type: text/plain` or `Content-Type: application/xml`
2. JAX-RS examines the method's `@Consumes(MediaType.APPLICATION_JSON)` annotation
3. JAX-RS determines there's a mismatch between what the client is sending and what the method accepts
4. JAX-RS returns **`415 Unsupported Media Type`** HTTP status code
5. The method is never invoked

**Technical Benefits:**

- **Early Validation:** Request is rejected at the framework level before reaching application code
- **Clear Error Messaging:** Standard HTTP 415 status indicates exactly what went wrong
- **Type Safety:** Ensures only properly formatted JSON reaches Java objects for deserialization
- **Security:** Prevents potential parsing vulnerabilities from unexpected data formats

This is part of JAX-RS's declarative approach to API design, where annotations specify contracts that the framework enforces automatically.

---

#### 6. You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

**Query Parameter Approach:** `GET /api/v1/sensors?type=CO2`

**Advantages:**
- **Optional Filtering:** Easy to make filtering optional - clients can omit the parameter to get all sensors
- **Multiple Filters:** Can easily add more filters: `?type=CO2&status=ACTIVE&roomId=LIB-301`
- **RESTful Semantics:** Query parameters are semantically designed for filtering, sorting, and searching
- **URL Clarity:** The base resource (`/sensors`) remains clean and represents the collection
- **Backward Compatibility:** Adding new filter parameters doesn't break existing clients

**Path Parameter Approach:** `GET /api/v1/sensors/type/CO2`

**Disadvantages:**
- **Rigid Structure:** Makes filtering mandatory - harder to represent "all sensors"
- **Complexity with Multiple Filters:** Would require awkward paths like `/sensors/type/CO2/status/ACTIVE`
- **Misleading Hierarchy:** Suggests "type" is a sub-resource rather than a filter criterion
- **Poor Scalability:** Each new filter requires a new endpoint path

**Conclusion:**
Query parameters are superior for filtering because they maintain clear resource hierarchy, support optional and combinable filters, and follow RESTful conventions where paths represent resources and query parameters represent operations on those resources.

---

### Part 4: Deep Nesting with Sub-Resources

#### 7. Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

The Sub-Resource Locator pattern provides significant architectural advantages for managing complex, nested resource hierarchies.

**Implementation in the API:**
```java
@Path("/{id}/readings")
public SensorReadingResource getReadings() {
    return new SensorReadingResource();
}
```

**Key Benefits:**

- **Separation of Concerns:** Each resource class handles its own domain logic. `SensorResource` manages sensors, while `SensorReadingResource` manages readings. This keeps classes focused and maintainable.

- **Reduced Complexity:** Instead of having one massive `SensorResource` class with methods for sensors AND all reading operations, I split them into separate classes. This prevents controller classes from becoming thousands of lines long.

- **Improved Maintainability:** When bugs occur in reading operations, developers know to look in `SensorReadingResource` rather than searching through a large controller file.

- **Reusability:** The `SensorReadingResource` class could potentially be reused for other similar patterns (e.g., if I had device readings or equipment readings).

- **Clear Resource Hierarchy:** The code structure mirrors the URL structure (`/sensors/{id}/readings`), making the API intuitive to understand and navigate.

- **Contextual Scope:** The sub-resource has implicit context about its parent (the sensor ID from the path), allowing it to operate within that scope automatically.

**Alternative (Without Sub-Resources):**
I would need to define paths like:
```java
@Path("/sensors/{sensorId}/readings")
@Path("/sensors/{sensorId}/readings/{readingId}")
```
All in `SensorResource`, leading to a bloated, hard-to-maintain class.

The sub-resource locator pattern scales elegantly as APIs grow, keeping code organized and manageable.

---

### Part 5: Advanced Error Handling & Logging

#### 8. Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

**HTTP 404 (Not Found):** Indicates the requested **URL/resource** doesn't exist
**HTTP 422 (Unprocessable Entity):** Indicates the request was understood but cannot be processed due to **semantic errors in the payload**

**In the Scenario:**
```json
POST /api/v1/sensors
{
    "id": "TEMP-001",
    "roomId": "FAKE-ROOM-999"
}
```

**Why 422 is More Accurate:**

- **The Endpoint Exists:** `POST /api/v1/sensors` is a valid endpoint (unlike 404 which suggests the URL is wrong)

- **The JSON is Valid:** The JSON syntax is correct and can be parsed successfully

- **The Semantic Error:** The problem is that `roomId: "FAKE-ROOM-999"` references a room that doesn't exist - this is a **data validation error**, not a routing error

- **Client Guidance:** 422 tells the client "your request structure is fine, but the data inside has problems," helping them understand they need to fix the content, not the URL

**If I Used 404:**
- Misleading: Suggests the `/sensors` endpoint doesn't exist
- Confusing: Client might think they have the wrong URL
- Less Specific: Doesn't distinguish between "endpoint not found" and "referenced resource not found"

**Conclusion:**
422 provides more precise error semantics: the request reached the right place and was syntactically valid, but the business logic couldn't process it due to invalid data relationships.

---

#### 9. From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

Exposing stack traces to external clients creates multiple security vulnerabilities:

**Information Disclosure Risks:**

1. **Technology Stack Exposure:**
   - Reveals exact versions of libraries and frameworks (e.g., "org.glassfish.jersey.message.internal.ReaderInterceptorExecutor")
   - Attackers can search for known vulnerabilities (CVEs) in those specific versions

2. **Application Structure:**
   - Exposes package names (`smartcampus.resource`, `smartcampus.repository`)
   - Reveals class and method names, showing internal architecture
   - Gives attackers a map of the application's organization

3. **File System Paths:**
   - Stack traces often include absolute file paths (e.g., "D:\Projects\smart-campus\src\...")
   - Reveals server directory structure and deployment locations

4. **Database Connection Details:**
   - SQL exception traces can expose database type, connection strings, and table/column names
   - Helps attackers craft SQL injection attacks

5. **Business Logic Insights:**
   - Method names and parameter types reveal how the application works internally
   - Helps attackers understand validation logic and find ways to bypass it

**Example Attack Scenario:**
```
java.lang.NullPointerException
    at smartcampus.repository.DataStore.getUserPassword(DataStore.java:45)
```
This tells an attacker:
- There's a password storage mechanism
- It's in the DataStore class
- There's insufficient null checking at line 45
- Potential injection point for exploitation

**Protection:**
The `GlobalExceptionMapper` returns generic messages: `"An unexpected error occurred. Please contact support."` while logging the full stack trace server-side for developers. This provides security for production while maintaining debuggability.

---

#### 10. Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

Using JAX-RS filters for logging provides significant architectural and maintenance advantages:

**Advantages of Filter-Based Logging:**

- **Centralization:**
  - All logging logic exists in one place (`LoggingFilter.java`)
  - Changes to log format or output only require updating one class
  - Eliminates code duplication across 15+ resource methods

- **Consistency:**
  - Every endpoint is logged the same way automatically
  - No risk of forgetting to add logging to new methods
  - Uniform log format across the entire API

- **Separation of Concerns:**
  - Resource classes focus on business logic
  - Logging is handled by infrastructure code
  - Cleaner, more maintainable resource methods

- **Automatic Coverage:**
  - The filter intercepts ALL requests/responses
  - Even endpoints added in the future get logged automatically
  - No manual intervention needed for new features

- **Easier Testing:**
  - Resource methods remain simple and focused
  - Can disable logging by removing the filter without touching business code
  - Logging behavior can be tested independently

- **Performance Monitoring:**
  - Can easily add request timing by calculating time between request and response filters
  - Centralized location for adding metrics collection

**Without Filters:**
```java
// Every method would need:
@POST
public Response create(Room room) {
    logger.info("POST /rooms called");  // Duplicated everywhere
    // business logic
    logger.info("POST /rooms completed with 201");  // Duplicated everywhere
}
```

**With Filters:**
```java
// Clean resource methods:
@POST
public Response create(Room room) {
    // Only business logic
    DataStore.rooms.put(room.getId(), room);
    return Response.status(201).entity(room).build();
}
```

This is the Aspect-Oriented Programming (AOP) principle: cross-cutting concerns should be handled separately from core business logic, making code cleaner, more maintainable, and less error-prone.

---

## Video Demonstration

https://drive.google.com/file/d/1e-CrJqt6ncdOhfI6GeYNqRpePcFqUqAH/view?usp=sharing
---

## Contributors

- Student Name: Warnakulasuriya Moditha Prabhashana Fernando
- Student ID: 20231413
- Module: 5COSC022W - Client-Server Architectures












