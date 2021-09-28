<h1>DeepfakeHTTP<br>
Your 100% static dynamic backend</h1>

<a title="License MIT" href="https://github.com/xnbox/DeepfakeHTTP/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue?style=flat-square"></a>
<a title="Release 3.1.1" href="https://github.com/xnbox/DeepfakeHTTP/releases"><img src="https://img.shields.io/badge/release-3.1.1-4DC71F?style=flat-square"></a>
<a title="Powered by Tommy" href="https://github.com/xnbox/tommy"><img src="https://img.shields.io/badge/powered_by-Tommy-blueviolet?style=flat-square"></a>

<p align="center">
<a href="#get-started">Get started</a> | <a href="#usage">Usage</a> | <a href="#usage-exampes">Usage Examples</a> | <a href="#how-does-it-work">How does it work?</a> |<a href="#features"> Features</a> | <a href="#appendix-aoptional-request-headers-openapi">Optional Headers</a> | <a href="Cheatsheet.md">Cheatsheet</a>
</p>

<p align="center">
<table>
<tr>
<td>
<img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/image.png" width="290">
</td>
<td>
<h3>What are people using it for?</h3>
<ul>
    <li>Creating the product POC or demo before even starting out with the backend</li>
    <li>REST, GraphQL, and other APIs prototyping and testing</li>
    <li>Hiding critical enterprise infrastructure behind a simple static facade</li>
    <li>Hacking and fine-tuning HTTP communications on both server and client sides</li>
</ul>
<img width="1000" height="0">
</td>
</tr>
</table>
</p>

<h2>Get started</h2>

<ol>
    <li>Download the <a href="https://github.com/xnbox/DeepfakeHTTP/releases/latest">latest release</a> of <code>df.jar</code></li>
    <li>Copy-paste the content of the dump example to the file <code>dump.txt</code>:
<span></span>

```http
GET /api/customer/123 HTTP/1.1

HTTP/1.1 200 OK
Content-Type: application/json

{
    "id": 123,
    "fname": "John",
    "lname": "Doe",
    "email": ["john@example.com", "johndoe@example.com"]
}
```

</li>
    <li>Start the server from command line:
<pre>
java -jar df.jar --dump dump.txt
</pre>
</li>
    <li>Use a browser to check whether the server is running:
<br>
<a href="http://localhost:8080/api/customer/123">http://localhost:8080/api/customer/123</a><br><br>
</li>
<li>Get response:<br>
<img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/get-started.png">
</li>
</ol>
That's it!
<br>
💡 For more examples see <a href="Cheatsheet.md">cheatsheet</a>.
<br><br>

<h2>Usage</h2>

```
java -jar df.jar [OPTIONS] [FLAGS] [COMMANDS]

OPTIONS:
    --port <number>        HTTP TCP port number, default: 8080
    --port-ssl <number>    HTTPS TCP port number, default: 8443
    --dump <file|url>...   dump text file(s) and/or OpenAPI json/yaml file(s)
    --data <file|url>...   json/yaml/csv data file(s) to populate templates
    --openapi-path <path>  serve OpenAPI client at specified context path
    --openapi-title <text> provide custom OpenAPI spec title
    --collect <file>       collect live request/response to file
    --format <json|yaml>   output format for --print-* commands, default: json
    --status <number>      status code for non-matching requests, default: 400

FLAGS:
    --no-log               disable request/response console logging
    --no-cors              disable CORS headers
    --no-etag              disable 'ETag' header
    --no-powered-by        disable 'X-Powered-By' header
    --no-watch             disable watch files for changes
    --no-color             disable ANSI color output for --print-* commands
    --no-pretty            disable prettyprint for --print-* commands
    --no-template          disable template processing
    --no-wildcard          disable wildcard processing
    --strict-json          enable strict JSON comparison
    --redirect             enable redirect HTTP to HTTPS

COMMANDS:
    --help                 print help message
    --print-info           print dump files statistics to stdout as json/yaml
    --print-requests       print dump requests to stdout as json/yaml
    --print-openapi        print OpenAPI specification to stdout as json/yaml
```

<h2>Usage Exampes</h2>
Start server on dump file:
<pre>
java -jar df.jar --dump dump.txt
</pre>
Start server on OpenAPI file:
<pre>
java -jar df.jar --dump openapi.json
</pre>
Start server with built-in OpenAPI client:
<pre>
java -jar df.jar --openapi-path /api --dump dump.txt
</pre>
<details>
<summary>
    more examples&hellip;
</summary>
<br>
Start server on few dump files:
<pre>
java -jar df.jar --dump dump1.txt dump2.txt dump3.txt
</pre>
Start server on mix of dump and OpenAPI files:
<pre>
java -jar df.jar --dump dump1.txt openapi2.json dump3.txt openapi4.yaml
</pre>
Start server with built-in OpenAPI client with custom title:
<pre>
java -jar df.jar --openapi-path /api --openapi-title 'My Killer REST API v18.2.1' --dump dump.txt
</pre>
</details>
<details>
<summary>
	even more examples&hellip;
</summary>
<br>
Collect live request/response to file:
<pre>
java -jar df.jar --collect /home/john/live.txt --dump dump.txt
</pre>
Specify JSON data file to populate templates:
<pre>
java -jar df.jar --data /home/john/data.json --dump dump.txt
</pre>
Print dump files statistics to stdout as JSON:
<pre>
java -jar df.jar --print-info --dump dump.txt
</pre>
Print dump requests to stdout as JSON:
<pre>
java -jar df.jar --print-requests --dump dump.txt
</pre>
Print OpenAPI specification to stdout as JSON:
<pre>
java -jar df.jar --print-openapi --dump dump.txt
</pre>
</details>

<h2>Prerequisites</h2>
<ul>
    <li>Java 15 or above</li>
</ul>

<h2>How does it work?</h2>
<ol>
    <li>Got client request</li>
    <li>Search dump entries (request-response pairs) for appropriate entry by matching all specified request parts:<br>
    method, URI, headers, and body</li>
    <li>If entry is found, the server generates a corresponded response and sends it to the client</li>
    <li>If entry is not found, the server search dump entries for response with status <code>400</code> (Bad request).</li>
    <li>If entry is found, the server send entry to the client
    <li>If entry is not found, the server sends status <code>400</code> with no body.</li>
</ol>
That's all.

<h2>Features</h2>
<ul>
    <li>No dependencies</li>
    <li>No installation</li>
    <li>No configuration files</li>
    <li>Single-file executable</li>
    <li>Retrieve response data from HTTP dumps and/or OpenAPI json/yaml</li>
</ul>
<details>
<summary>
    more features&hellip;
</summary>
<br>
<ul>
    <li>Optional built-in OpenAPI client</li>
    <li>Asynchronous requests and responses</li>
    <li>HTTP message formats (RFC 7230)</li>
    <li>Unlimited number of request/response pairs in the dump</li>
    <li>Scriptable response body</li>
    <li>Supports methods: <code>GET</code>, <code>HEAD</code>, <code>POST</code>, <code>PUT</code>, <code>DELETE</code> etc.</li>
    <li>Multi-line and multi-value headers (RFC 7230).</li>
    <li>OpenAPI-styled templates in paths</li>
    <li>Wildcards ( <code> *</code> and <code> ?</code> with escape <code> /</code> ) in query string and header values</li>
    <li>Templates in response body</li>
    <li>JSON/YAML/CSV data files to populate templates</li>
    <li>Response body fetching from external sources like URLs, local files, and data URI</li>
    <li>Per entry user-defined request and response delays</li>
    <li>Comments <code> #</code> in dumps</li>
    <li>Live request/response collection</li>
    <li>Optional watching dump files for changes</li>
    <li>Optional <code>ETag</code> support</li>
    <li>Optional CORS support</li>
    <li>Optional live request/response logging</li>
    <li>TLS(SSL) connections and HTTP to HTTPS redirect</li>
    <li>Customizable OpenAPI client path</li>
    <li>Latest OpenAPI specification (v3.0.3) in JSON and YAML format</li>
    <li>Colorized console output</li>
    <li>Disabling color via <code>NO_COLOR</code> environment variable</li>
</ul>
</details>

<h2>Legal</h2>
<ul>
<li>The DeepfakeHTTP is released under the <a href="https://github.com/xnbox/DeepfakeHTTP/blob/main/LICENSE">MIT</a> license.</li>
<li>Third-party products: <a href="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/THIRD-PARTY">THIRD-PARTY</a></li>
</ul>
<br><br>
<h1></h1>
<br><br>
<h2>
APPENDIX A.
<br>
Optional request headers (OpenAPI)
</h2>
<table>
    <tr><th width="220rem">Header</th><th>Description</th></tr>
    <tr></tr>
    <tr><td valign="top"><code>X-OpenAPI-Summary</code></td>
    <td>
    <p>OpenAPI request summary text.</p>
    <i>Example:</i>

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-OpenAPI-Summary: Get customer information

{"id": 5, "name": "John Doe"}
```

</td></tr>
<tr></tr>
    <tr><td valign="top"><code>X-OpenAPI-Description</code></td>
    <td>
    <p>OpenAPI request description text.</p>
    <i>Example:</i>

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-OpenAPI-Summary: Get customer information
X-OpenAPI-Description: This API extracts customer info from db

{"id": 5, "name": "John Doe"}
```

</td></tr>
<tr></tr>
    <tr><td valign="top"><code>X-OpenAPI-Tags</code></td>
    <td>
    <p>OpenAPI request comma-separated tag list.</p>
    <i>Example:</i>

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-OpenAPI-Summary: Get customer information
X-OpenAPI-Description: This API extracts customer info from db
X-OpenAPI-Tags: Work with customer, Buyers, Login info

{"id": 5, "name": "John Doe"}
```
<img width="1000" height="0">
</td></tr>
</table>
<strong>NOTE:  </strong>Optional request headers are used as OpenAPI annotations and will <strong>not</strong> be sent to the server engine.
<br><br>


<h2>
APPENDIX B.
<br>
Optional response headers
</h2>
<table>
    <tr><th width="220rem">Header</th><th>Description</th></tr>
    <tr></tr>
    <tr><td valign="top"><code>X-Body-Type     </code></td>
    <td>
    <p>Tells the server what the content type (media type) of the body content actually is. Value of this header has same rules as value of standard HTTP <code>Content-Type</code> header.</p>
    <p>This header is useful when you want to use binary data or script as a response body.</p>
    <i>Examples:</i>
<br><br>

A response body is a character data (default).<br>
No <code>X-Body-Type</code> header is needed.

```http
HTTP/1.1 200 OK
Content-Type: application/json

{"id": 5, "name": "John Doe"}
```

<h2></h2>

Get a response body from a remote server.<br>
Body type is <code>text/uri-list</code> (RFC 2483)

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Body-Type: text/uri-list

http://example.com/api/car/1234.json
```

<h2></h2>

Get a response body from a file.<br>
Body type is <code>text/uri-list</code> (RFC 2483)

```http
HTTP/1.1 200 OK
Content-Type: image/jpeg
X-Body-Type: text/uri-list

file:///home/john/photo.jpeg
```

<h2></h2>

Get a response body from a data URI.<br>
Body type is <code>text/uri-list</code> (RFC 2483)

```http
HTTP/1.1 200 OK
Content-Type: image/gif
X-Body-Type: text/uri-list

data:image/gif;base64,R0lGODlhAQABAIAAAP...
```
</td></tr>
<tr></tr>
    <tr><td valign="top"><code>X-Request-Delay</code></td>
    <td><p>Request delay (in milliseconds).</p>
    <i>Example:</i>
    <br>

```http
# Two seconds request delay.

HTTP/1.1 200 OK
X-Request-Delay: 2000

{"id": 5, "name": "John Doe"}
```
</td></tr>
<tr></tr>
    <tr><td valign="top"><code>X-Response-Delay</code></td>
    <td><p>Response delay (in milliseconds).</p>
    <i>Example:</i>
    <br>

```http
# Two seconds response delay.

HTTP/1.1 200 OK
X-Response-Delay: 2000

{"id": 5, "name": "John Doe"}
```
<img width="1000" height="0">
</td></tr>
</table>
<strong>NOTE:  </strong>Optional response headers will <strong>not</strong> be sent to clients.
<br><br>
