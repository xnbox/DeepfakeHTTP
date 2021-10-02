<h1>DeepfakeHTTP<br>
Your 100% static dynamic backend</h1>

<a title="License MIT" href="https://github.com/xnbox/DeepfakeHTTP/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue?style=flat-square"></a>
<a title="Release 5.1.1" href="https://github.com/xnbox/DeepfakeHTTP/releases"><img src="https://img.shields.io/badge/release-5.1.1-4DC71F?style=flat-square"></a>
<a title="Powered by Tommy" href="https://github.com/xnbox/tommy"><img src="https://img.shields.io/badge/powered_by-Tommy-blueviolet?style=flat-square"></a>

<p align="center">
<a href="#get-started">Get started</a> | <a href="#usage">Usage</a> | <a href="#usage-exampes">Usage Examples</a> | <a href="#how-does-it-work">How does it work?</a> |<a href="#features"> Features</a> | <a href="#appendix-aoptional-request-headers">Optional Headers</a> | <a href="Cheatsheet.md">Cheatsheet</a>
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
    <li>Creating the product PoC or demo before even starting out with the backend</li>
    <li>REST, GraphQL, and other APIs mocking and testing</li>
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
<br>
For more examples, see the <a href="Cheatsheet.md">cheatsheet</a>.
<br><br>

<h2>Usage</h2>

```
java -jar df.jar [OPTIONS] [FLAGS] [COMMANDS]

OPTIONS:                                                                      
   --port <number>         HTTP TCP port number, default: 8080                
   --port-ssl <number>     HTTPS TCP port number, default: 8443               
   --dump <file|url>...    dump text file(s) and/or OpenAPI json/yaml file(s) 
   --data <file|url>...    json/yaml/csv data file(s) to populate templates   
   --openapi-path <path>   serve built-in OpenAPI client at specified context
   --openapi-title <text>  provide custom OpenAPI spec title                  
   --collect <file>        collect live request/response to file              
   --format <json|yaml>    output format for --print-* commands, default: json
   --status <number>       status code for non-matching requests, default: 400
   --max-log-body <number> max body bytes in console log, default: unlimited  
                                                                              
FLAGS:                                                                        
   --no-log                disable request/response console logging           
   --no-log-headers        disable request/response headers in console logging
   --no-log-body           disable request/response body in console logging   
   --no-cors               disable CORS headers                               
   --no-etag               disable 'ETag' header                              
   --no-server             disable 'Server' header                            
   --no-watch              disable watch files for changes                    
   --no-color              disable ANSI color output for --print-* commands   
   --no-pretty             disable prettyprint for --print-* commands         
   --no-template           disable template processing                        
   --no-wildcard           disable wildcard processing                        
   --strict-json           enable strict JSON comparison                      
   --redirect              enable redirect HTTP to HTTPS                      
                                                                              
COMMANDS:                                                                     
   --help                  print help message                                 
   --print-info            print dump files statistics to stdout as json/yaml 
   --print-requests        print dump requests to stdout as json/yaml         
   --print-openapi         print OpenAPI specification to stdout as json/yaml 
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
	<li>Got the client's request.</li>
	<li>Search the dump for corresponded entry (request-response pair) by matching all specified request's parts:<br>
	<i>method</i>, <i>URI</i>, <i>headers</i>, and <i>body</i>.</li>
	<li>If the entry was found, the server sends the appropriate response to the client.</li>
	<li>If the entry was not found, the server sends a status <code>400</code> (400 Bad request).</li>
</ol>
That's all.

<h2>Features</h2>
<ul>
    <li>No dependencies, no installation, no configs</li>
    <li>Crossplatform single-file executable</li>
    <li>Retrieve response data from HTTP dumps and/or OpenAPI JSON/YAML</li>
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
    <li>Supports methods: <code>GET</code>, <code>HEAD</code>, <code>POST</code>, <code>PUT</code>, <code>DELETE</code> etc.</li>
    <li>Multi-line and multi-value headers (RFC 7230).</li>
    <li>OpenAPI-styled templates in paths</li>
    <li>Wildcards ( <code> *</code> and <code> ?</code> with escape <code> /</code> ) in query string and header values</li>
    <li>Templates in URI, headers, body</li>
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

<h2>LICENSE</h2>
The DeepfakeHTTP is released under the <a href="https://github.com/xnbox/DeepfakeHTTP/blob/main/LICENSE">MIT</a> license.
<br><br>
<h1></h1>
<br><br>
<h2>
APPENDIX A.
<br>
Optional request headers
</h2>
<table>
    <tr><th width="220rem">Header</th><th>Description</th></tr>
<tr></tr>
    <tr><td valign="top"><code>X-Delay</code></td>
    <td><p>Request delay (in milliseconds).</p>
    <i>Example:</i>
    <br>

```http
# Two seconds request delay.

HTTP/1.1 200 OK
X-Delay: 2000

{"id": 5, "name": "John Doe"}
```
</td></tr>
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
<strong>NOTE: </strong>Optional request headers will <strong>not</strong> be sent to the server engine.
<br><br>


<h2>
APPENDIX B.
<br>
Optional response headers
</h2>
<table>
    <tr><th width="220rem">Header</th><th>Description</th></tr>
<tr></tr>
    <tr><td valign="top"><code>X-Delay</code></td>
    <td><p>Response delay (in milliseconds).</p>
    <i>Example:</i>
    <br>

```http
# Two seconds response delay.

HTTP/1.1 200 OK
X-Delay: 2000

{"id": 5, "name": "John Doe"}
```

</td></tr>
<tr></tr>
    <tr><td valign="top"><code>X-Content-Source</code></td>
    <td>
    <p>
    The URL of the externally hosted content. The content from the URL will be sent as the response body.
    Supported protocols: <code>http:</code>, <code>https:</code>, <code>file:</code>, <code>data:</code>.<br>
    If the URL provides its own content type and there is no <code>Content-Type</code> header in the dump, the original <code>Content-Type</code> header received from the URL will be sent along with other response headers.
    </p>
    <p>
    This header is useful when you want to send content hosted on a remote server or just send binary data as a response body.
    </p>
    <i>Examples:</i>
<br><br>

Get a response body from a remote server.<br>

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Content-Source: http://example.com/api/car/1234.json
```

<h2></h2>

Get a response body from a file.<br>

```http
HTTP/1.1 200 OK
Content-Type: image/jpeg
X-Content-Source: file:///home/john/photo.jpeg
```

<h2></h2>

Get a response body from a data URI.<br>

```http
HTTP/1.1 200 OK
Content-Type: image/gif
X-Content-Source: data:image/gif;base64,R0lGODlhAQABAIAAAP...
```
</td></tr>
<tr></tr>
        <tr><td valign="top"><code>X-Forward-To</code></td>
    <td>
    <p>Forward client request to specified origin. Acts as a forward proxy.</p>
    <i>Example:</i>
<br>

```http
HTTP/1.1
X-Forward-To: http://example.com:8080
```
</td></tr>
<tr></tr>
        <tr><td valign="top"><code>X-CGI</code></td>
    <td>
    <p>CGI (Common Gateway Interface) program.</p>
    <i>Example:</i>
<br>

```http
HTTP/1.1
X-CGI: /home/john/myprog.sh param1 param2
```
<img width="1000" height="0">
	</td>
	</td></tr>
</table>
<strong>NOTE:  </strong>Optional response headers will <strong>not</strong> be sent to clients.
<br><br>
