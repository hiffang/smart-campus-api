#  Smart Campus REST API

##  Overview

This project implements a **RESTful API using JAX-RS (Jersey)** for managing a university *Smart Campus* system. The API allows administrators and systems to manage:

-  Rooms  
-  Sensors (e.g., Temperature, CO2, Occupancy)  
-  Sensor Readings (historical data)

### 🔧 Key Design Features

- **RESTful Architecture**
  - Resource-based URLs (`/rooms`, `/sensors`)
  - Proper HTTP methods (GET, POST, DELETE)

- **Nested Resources**
  - Sensor readings accessed via:  
    `/api/v1/sensors/{sensorId}/readings`

- **Filtering Support**
  - Query-based filtering:  
    `/api/v1/sensors?type=CO2`

- **Robust Error Handling**
  - 409 Conflict → Room not empty  
  - 422 Unprocessable Entity → Invalid room reference  
  - 403 Forbidden → Sensor unavailable  
  - 500 Internal Server Error → Global fallback

- **In-Memory Storage**
  - Uses `HashMap` and `ArrayList` (no database as per requirements)

- **Logging**
  - Request & response logging via JAX-RS filters

---

## 🚀 How to Build & Run (Using NetBeans + Apache Tomcat)

### ✅ Prerequisites

Before starting, ensure you have:

- Java JDK 11 or higher installed
- Apache NetBeans (latest version recommended)
- Apache Tomcat (v9 or above)
- Internet connection (to download Maven dependencies)

---

### 📥 Step 1: Clone or Download the Project

**Option A: Using Git**
```bash
git clone https://github.com/your-username/smart-campus-api.git
```

**Option B: Download ZIP**
- Download the repository as a `.zip`
- Extract it to a folder on your machine

---

### 📂 Step 2: Open Project in NetBeans

1. Open **NetBeans**
2. Click **File → Open Project**
3. Select the project folder
4. Click **Open Project**

> NetBeans will automatically detect it as a Maven project and begin downloading dependencies.

---

### ⚙️ Step 3: Configure Java Version

1. Right-click the project → **Properties**
2. Navigate to **Libraries → Java Platform**
3. Select **JDK 11 or higher**
4. Click **OK**

---

### 🖥️ Step 4: Add Apache Tomcat Server

1. Go to **Services → Servers**
2. Right-click → **Add Server**
3. Choose **Apache Tomcat**
4. Browse and select your Tomcat installation directory
5. Click **Finish**

---

### 🔗 Step 5: Configure Project to Use Tomcat

1. Right-click the project → **Properties**
2. Go to **Run**
3. Under **Server**, select your configured **Tomcat server**
4. Ensure the **Context Path** is set (e.g., `/smart-campus-api`)
5. Click **OK**

---

### ▶️ Step 6: Build the Project

1. Right-click the project
2. Click **Clean and Build**

This will:
- Compile the application
- Resolve dependencies
- Package it as a `.war` file

---

### ▶️ Step 7: Run the Application

1. Right-click the project
2. Click **Run**

NetBeans will:
- Deploy the `.war` file to Tomcat
- Start the server automatically

---

### 🌐 Step 8: Access the API

Once deployed, access the API at:

```
http://localhost:8080/smart-campus-api/api/v1
```

> Replace `smart-campus-api` with your actual project context path if different.

---

### 🧪 Step 9: Test the API

You can test using Postman or curl.

Example:

```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms
```

---

### API Endpoints Summary

| Resource  | Method | Endpoint                 | Description    |
| --------- | ------ | ------------------------ | -------------- |
| Discovery | GET    | `/api/v1`                | API metadata   |
| Rooms     | GET    | `/rooms`                 | List all rooms |
| Rooms     | POST   | `/rooms`                 | Create room    |
| Rooms     | GET    | `/rooms/{id}`            | Get room       |
| Rooms     | DELETE | `/rooms/{id}`            | Delete room    |
| Sensors   | GET    | `/sensors`               | List sensors   |
| Sensors   | POST   | `/sensors`               | Create sensor  |
| Sensors   | GET    | `/sensors?type=CO2`      | Filter sensors |
| Readings  | GET    | `/sensors/{id}/readings` | Get readings   |
| Readings  | POST   | `/sensors/{id}/readings` | Add reading    |

# Sample cURL Commands
### 1️ Create a Room
```
curl -X POST http://localhost:8080/api/v1/rooms \
-H "Content-Type: application/json" \
-d '{
  "id": "LIB-301",
  "name": "Library Quiet Study",
  "capacity": 50
}'
```
### 2️⃣ Get All Rooms
```
curl -X GET http://localhost:8080/api/v1/rooms
```
### 3️⃣ Create a Sensor
```
curl -X POST http://localhost:8080/api/v1/sensors \
-H "Content-Type: application/json" \
-d '{
  "id": "TEMP-001",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 0,
  "roomId": "LIB-301"
}'
```
### 4️⃣ Filter Sensors by Type
```
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
```
### 5️⃣ Add Sensor Reading
```
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
-H "Content-Type: application/json" \
-d '{
  "id": "reading-001",
  "timestamp": 1713955200000,
  "value": 23.5
}'
```
### 6️⃣ Delete a Room (will fail if sensors exist)
```
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```
# Smart Campus API Report
## Part 1: Service Architecture & Setup 

### Question 1 In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.
JAX-RS creates a new instance of your resource class for every request. So if ten people hit your API at the same time, you get ten separate RoomResource objects. This is the default behavior and it's different from other frameworks that default to singletons.
The problem is that instance variables get wiped between requests. If you store your rooms in a regular HashMap as an instance variable, you'll create it, add some data, send a response, and then the instance gets garbage collected. Next request comes in, new instance, empty HashMap. Your data is gone.
That's why we use static variables. A static Map lives at the class level, not the instance level. All instances of RoomResource share the same static roomStore. This means your data persists across requests.
### Question 2 -  Why is the provision of ”Hypermedia” (links and navigation within responses)considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?
HATEOAS stands for Hypermedia as the Engine of Application State. The idea is that the API responses include links to related resources and possible next actions. Instead of clients needing to know all URLs ahead of time, they discover them by following links.
For example, when you GET a room, the response includes a link to that room's sensors. The client doesn't need to construct the URL themselves or look it up in documentation. The server tells them where to go next.
This is beneficial because it decouples clients from the URL structure. If you change the endpoint paths, clients still work because they're following links and not building URLs. Documentation can get out of sync with the actual API, but hypermedia links are always current because they come from the running server.
It also makes APIs more discoverable. A client can start at the root endpoint and explore what's available without reading documentation first. New features automatically appear as new links in responses.
The downside is complexity. You need to include link structures in every response. Clients need to be smart enough to follow links instead of hardcoding URLs. For simple APIs, it might be overkill.

## Part 2: Room Management 

### Question 1 - When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.
Returning only IDs means smaller responses. It means less data over the network and faster transmission with lower bandwidth costs, and the client gets the list quickly.
However, the client usually needs more than just IDs. They need the room name, capacity, or other details to display in the UI. With only IDs, they have to make additional requests to fetch each room's details. Returning full room objects means one request gets everything. The client can display the data immediately without additional round trips. This improves performance when the client needs all the details anyway.
The downside is larger response size.. More bandwidth leads to longer transfer times and more memory is needed on the client.
The best approach depends on the use case. If clients always need full details, return full objects. If clients only need IDs for a dropdown or autocomplete, return minimal data. Some APIs offer query parameters to let clients choose what fields they want.

### Question 2 - Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.
Yes, DELETE is idempotent in this implementation. Idempotent means calling the operation multiple times produces the same result as calling it once.
Here's what happens when a client sends DELETE /rooms/LIB-301 multiple times:
First request: The room exists. We check if it has sensors. It doesn't. We remove it from the map and return 204 No Content.
Second request: The room doesn't exist anymore. We check if it's in the map. It's not. We immediately return 204 No Content without throwing an error.
Third request and beyond: Same as the second request. We return 204 No Content.
The key is that we return success (204) whether we actually deleted something or it was already gone. This is intentional. The client's goal is "make sure this room doesn't exist." After any DELETE request, the room doesn't exist, so the goal is achieved.
If we returned 404 Not Found for the second request, the operation wouldn't be idempotent because the first and second calls would return different status codes.
This matches HTTP semantics. DELETE should be safe to retry. If a network error happens and the client isn't sure if the request went through, they can resend it without worrying about side effects.

## Part 3: Sensor Operations & Linking 
### Question 1 -  We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?
When you use @Consumes(MediaType.APPLICATION_JSON), you're telling JAX-RS that this method only accepts JSON. If a client sends a request with a different Content-Type header, JAX-RS rejects it before your method even runs.
If the client sends Content-Type: text/plain or Content-Type: application/xml, JAX-RS returns HTTP 415 Unsupported Media Type. The response body typically includes a message explaining that the media type is not supported.
This happens at the framework level. JAX-RS checks the Content-Type header against all available resource methods. If no method accepts the provided media type, it returns 415. Your code never executes.
This is useful because it prevents garbage data from reaching your method. Without @Consumes, JAX-RS might try to deserialize XML or plain text as JSON, which would fail with a cryptic parsing error. The 415 response is clearer - it tells the client exactly what went wrong.
If you want to support multiple formats, you can specify multiple media types: @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}). JAX-RS will route the request based on the Content-Type header.
### Question 2 -  You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?
Query parameters and path parameters serve different purposes in REST API design.
Path parameters identify a specific resource. /sensors/TEMP-001 means "the sensor with ID TEMP-001." The path points to one thing.
Query parameters filter or modify a collection. /sensors?type=CO2 means "the collection of all sensors, filtered to show only CO2 type." You're still working with the sensors collection, just narrowed down.
Using /sensors/type/CO2 as a path makes "type" look like a resource, which is semantically wrong. The type isn't a thing you're fetching - it's a filter on the sensors collection.
Query parameters are also more flexible. You can combine them: /sensors?type=CO2&status=ACTIVE. With path parameters, you'd need /sensors/type/CO2/status/ACTIVE, which gets messy fast. Add more filters and the URL becomes unusable.
Query parameters are also optional by nature. /sensors returns everything, /sensors?type=CO2 returns filtered results. With path parameters, you'd need separate endpoints for filtered and unfiltered results.
The HTTP specification and REST conventions support this distinction. Paths identify resources, queries modify how you retrieve them. Following conventions makes an API more intuitive for developers.

## Part 4: Deep Nesting with Sub- Resources 

### Question 1 -  Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?
The Sub-Resource Locator pattern separates concerns by delegating nested resource logic to dedicated classes. Instead of handling all paths in one controller, you create specialized classes for sub-resources.
In a monolithic approach, SensorResource would handle all sensor-related paths including /sensors/{id}/readings and /sensors/{id}/readings/{rid}. As you add more endpoints, the class grows. You end up with one massive file handling dozens of methods.
With sub-resource locators, SensorResource handles sensor operations and returns a SensorReadingResource instance to handle reading operations. Each class focuses on its own domain.
This provides several benefits. First, it improves code organization. Related methods are grouped together. SensorReadingResource contains all reading-related logic in one place.
Second, it makes the code more maintainable. When you need to modify reading logic, you only touch SensorReadingResource. You don't have to navigate through hundreds of lines in a giant controller.
Third, it enables better testing. You can test SensorReadingResource independently without setting up the entire sensor context.
Fourth, it scales better. As the API grows, you add new sub-resource classes instead of bloating existing ones. Each class remains manageable.
Finally, it promotes reusability. If multiple parent resources need similar sub-resource behavior, you can share the sub-resource class or extend a common base.

## Part 5: Advanced Error Handling, Exception Mapping & Logging 
### Question 1 -  Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?
HTTP status codes communicate different types of errors. Understanding the distinction between 404 and 422 helps clients handle errors appropriately.
404 Not Found means the resource at the requested URL doesn't exist. If you GET /sensors/INVALID-ID and that sensor doesn't exist, 404 is correct. The URL itself is the problem.
422 Unprocessable Entity means the request was well-formed but contains semantic errors. The server understands the format but can't process the content because of business logic violations.
When a client POSTs a new sensor with a valid JSON payload but references a roomId that doesn't exist, the URL is fine. The JSON is syntactically valid. The problem is the data inside - the roomId reference is invalid.
Using 404 here is misleading because it suggests the endpoint is wrong. The client might think they used the wrong URL. But the endpoint is correct; the issue is the referenced room.
422 is more specific. It tells the client: "I received your request, I parsed your JSON, but the roomId you provided doesn't exist." This is a validation error, not a routing error.
This helps client developers debug issues. A 404 suggests checking the URL. A 422 suggests checking the payload data. The error response can include details about which field is invalid.

### Question 2 - From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?
Exposing stack traces to external API consumers creates security vulnerabilities by revealing internal implementation details that attackers can exploit.
A Java stack trace contains several pieces of sensitive information. First, it shows your package structure and class names. An attacker can see exactly how your code is organized.
Second, it reveals library versions and dependencies. The trace shows which frameworks you're using. An attacker can identify outdated libraries with known vulnerabilities and craft targeted exploits.
Third, it exposes file paths and directory structures. The trace shows where your code lives on the server. This information helps attackers understand your deployment environment.
Fourth, it reveals method names and call sequences. Attackers can see your application's control flow and logic, helping them understand how to manipulate the system.
Fifth, it sometimes includes variable names and values, which might contain sensitive data or reveal business logic.
With this information, attackers can perform reconnaissance without interacting directly with your application. They can research known vulnerabilities in your specific library versions, craft payloads targeting your particular framework configuration, or understand your architecture to find weak points.
Instead of exposing stack traces, returning generic error messages to external clients while logging the full error details internally are more suitable for debugging. This provides security while still enabling troubleshooting.

### Question 3 -  Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?
Using filters for logging provides centralization, consistency, and maintainability compared to manual logging in each method.
Centralization means all logging logic lives in one place. With a filter, you implement logging once and it applies to every request. With manual logging, you write Logger.info() in every resource method. If you have 50 methods, you write 50 logging statements.
Consistency ensures uniform log format. A filter logs every request the same way - same format, same level, same information. Manual logging leads to variations. Different developers log different things. Some methods get forgotten.
Maintainability improves because changes happen in one place. Need to add the user ID to logs? Update the filter once. With manual logging, you'd update 50 methods and might miss some.
Filters also handle concerns automatically. You can't forget to log a request because the filter runs for every endpoint. With manual logging, new endpoints might not include logging until someone notices.
Separation of concerns keeps business logic clean. Resource methods focus on their core purpose without logging clutter. The same filter can handle authentication, authorization, rate limiting, or metrics without touching resource code.
Filters execute in a pipeline before and after resource methods. This means they can measure request duration, log responses, or add headers without the resource knowing. The framework handles the orchestration.
