<h1>DeepfakeHTTP<br>
Your 100% static dynamic backend</h1>

<a title="License MIT" href="https://github.com/xnbox/DeepfakeHTTP/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue?style=flat-square"></a>
<a title="Release 3.1.1" href="https://github.com/xnbox/DeepfakeHTTP/releases"><img src="https://img.shields.io/badge/release-3.1.1-4DC71F?style=flat-square"></a>
<a title="Powered by Tommy" href="https://github.com/xnbox/tommy"><img src="https://img.shields.io/badge/powered_by-Tommy-blueviolet?style=flat-square"></a>

<p align="center">
<a href="#get-started">Get started</a> | <a href="#usage">Usage</a> | <a href="#usage-exampes">Usage Examples</a> | <a href="#how-does-it-work">How does it work?</a> |<a href="#features"> Features</a> | <a href="#appendix-cdump-examples">Dump Examples</a> | <a href="#appendix-aoptional-request-headers-openapi">Optional Headers</a>
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
<a href="http://localhost:8080/api/customer/123">http://localhost:8080/api/customer/123</a>
<br>
<br>
<img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/get-started.png">
</li>
</ol>
That's it! For more examples see: <a href="#appendix-cdump-examples">APPENDIX C.</a>
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

<h2>
APPENDIX C.
<br>
Dump examples
</h2>
<br>
<h3>Example 1.</h3>

```http
#
# Simple form.
#
# Example:
#
# http://localhost:8080/api/customer/5
#

# Please don't miss a single carriage return between headers and body!

GET /form.html HTTP/1.1

.
# Fake HTML file :)
HTTP/1.1 200 OK

<!DOCTYPE html>
<html lang="en">
<body>
    <form action="/add_user.php" method="POST">
        <label for="fname">First name:</label><input type="text" name="fname"><br><br>
        <label for="lname">Last name: </label><input type="text" name="lname"><br><br>
        <input type="submit" value="Submit">
    </form>
</body>
</html>
.

POST /add_user.php HTTP/1.1
Content-Type: application/x-www-form-urlencoded

.
# Fake PHP file :)
HTTP/1.1 200 OK
Content-Type: text/html

<!DOCTYPE html>
<html lang="en">
<body>
    <h1>Hello ${request.parameters.fname[0]} ${request.parameters.lname[0]}!</h1>
</body>
</html>
.
```

<br>
<h3>Example 2.</h3>

```http
#
# Math headers
#
#
# Example:
#
# http://localhost:8080/api/customer/5
#


# First request-response entry

# Client request
GET /api/customer/5 HTTP/1.1
Accept-Language: ru;*

.
# Server response
HTTP/1.1 200 OK
Content-Type: application/json

{
    "id": 5,
    "name": "Джон Доу"
}
.

# Second request-response entry

# Client request
GET /api/customer/5 HTTP/1.1

.
# Server response
HTTP/1.1 200 OK
Content-Type: application/json

{
    "id": 5,
    "name": "John Doe"
}
.
```

<br>
<h3>Example 3.</h3>

```http
#
# favicon
#
GET /favicon.ico HTTP/1.1

HTTP/1.1 200 OK
Content-Type: image/vnd.microsoft.icon
X-Body-Type: text/uri-list

data:image/vnd.microsoft.icon;base64,AAABAAEAEBAAAAEAGABoAwAAFgAAACgAAAAQAA
 AAIAAAAAEAGAAAAAAAAAMAABILAAASCwAAAAAAAAAAAAASVuwSVuwSVuwSVuwAif8Aif8Aif8A
 if8Aif8Aif8Aif8Aif8SVuwSVuwSVuwSVuwSVuwSVuwAif8Aif8Aif8Aif8Aif8Aif8Aif8Aif
 8Aif8Aif8Aif8Aif8SVuwSVuwSVuwAif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8A
 if8Aif8Aif8SVuwSVuwAif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif
 8SVuwAif8Aif8Aif8Aif8Aif8AAwYAAwYAAwYAAwYAAwYAAwYAif8Aif8Aif8Aif8Aif8Aif8A
 if8Aif8Aif8AAwYAAwYAif8Aif8Aif8Aif8AAwYAAwYAif8Aif8Aif8Aif8Aif8Aif8Aif8Aif
 8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8A
 if8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif
 8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8A
 if8Aif8Aif8Aif8Aif8Aif8Aif8AAwYAif8AAwYAAwYAif8Aif8Aif8AAwYAAwYAif8Aif8AAw
 YAif8Aif8Aif8Aif8Aif8AAwYAAwYAif8Aif8Aif8Aif8Aif8AAwYAAwYAAwYAif8Aif8Aif8S
 VuwAif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8SVuwSVuwAif8Aif
 8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8SVuwSVuwSVuwAif8Aif8Aif8A
 if8Aif8Aif8Aif8Aif8Aif8Aif8Aif8Aif8SVuwSVuwSVuwSVuwSVuwSVuwAif8Aif8Aif8Aif
 8Aif8Aif8Aif8Aif8SVuwSVuwSVuwSVuzwDwAAwAMAAIABAACAAQAAAAAAAAAAAAAAAAAAAAAA
 AAAAAAAAAAAAAAAAAAAAAACAAQAAgAEAAMADAADwDwAA
.
```

<br>
<details>
<summary>
    More Examples&hellip;
</summary>

<br>
<h3>Example 4.</h3>

```http
#
# Work with HTML forms (1)
#
#
# Example:
#
# http://localhost:8080/form1.html
#

GET /form1.html HTTP/1.1

HTTP/1.1 200 OK

<!DOCTYPE html>
<html lang="en">
<body>
    <form action="/action_page.php" method="POST">
        <label for="fname">First name:</label><input type="text" name="fname"><br>
        <br>
        <label for="lname">Last name: </label><input type="text" name="lname"><br>
        <br>
        <input type="submit" value="Submit">
    </form>
</body>
</html>


POST /action_page.php HTTP/1.1
Content-Type: application/x-www-form-urlencoded

HTTP/1.1 200 OK
Content-Type: text/html

<!DOCTYPE html>
<html lang="en">
<body>
    <h1>Hello ${request.parameters.fname[0]} ${request.parameters.lname[0]}!</h1>
</body>
</html>
.
```

<br>
<h3>Example 5.</h3>

```http
#
# Work with HTML forms (2)
#
#
# Example:
#
# http://localhost:8080/form2.html
#

GET /form2.html HTTP/1.1

HTTP/1.1 200 OK

<!DOCTYPE html>
<html lang="en">
<body>
    <form action="/action_page2.php" method="POST">
        <label for="fname">First name:</label><input type="text" name="fname" value="John"><br>
        <br>
        <label for="lname">Last name: </label><input type="text" name="lname" value="Doe"><br>
        <p>
        Only the first name <i>John</i> and the last name <i>Doe</i> are supported.
        <br>
        Expected result is: <strong>Hello John Doe!</strong>,<br>
        or HTTP status <strong><code>400 Bad request</code></strong><br>
        when first name is not <i>John</i> or last name is not <i>Doe</i>.
        </p>
        <input type="submit" value="Submit">
    </form>
</body>
</html>


POST /action_page2.php HTTP/1.1
Content-Type: application/x-www-form-urlencoded

fname=John&lname=Doe

HTTP/1.1 200 OK
Content-Type: text/html

<!DOCTYPE html>
<body>
    <h1>Hello ${request.parameters.fname[0]} ${request.parameters.lname[0]}!</h1>
</body>
</html>
.
```

<br>
<h3>Example 6.</h3>

```http
#
# Response with PDF file
#
#
# Example:
#
# http://localhost:8080/customers/123456/purchases/2018-07-29/report?format=pdf
#

GET /customers/{id}/purchases/{date}/report?format=pdf HTTP/1.1

HTTP/1.1 200 OK
Content-Type: application/pdf

%PDF-1.3
1 0 obj
<<
/Type /Catalog
/Outlines 2 0 R
/Pages 3 0 R
>>
endobj

2 0 obj
<<
/Type /Outlines
/Count 0
>>
endobj

3 0 obj
<<
/Type /Pages
/Count 2
/Kids [ 4 0 R 6 0 R ] 
>>
endobj

4 0 obj
<<
/Type /Page
/Parent 3 0 R
/Resources <<
/Font <<
/F1 9 0 R 
>>
/ProcSet 8 0 R
>>
/MediaBox [0 0 612.0000 792.0000]
/Contents 5 0 R
>>
endobj

5 0 obj
<< /Length 1074 >>
stream
2 J
BT
0 0 0 rg
/F1 0027 Tf
57.3750 722.2800 Td
( Customer ID: ${request.parameters.id[0]}) Tj
ET
BT
/F1 0010 Tf
69.2500 688.6080 Td
( Date: ${request.parameters.date[0]} ) Tj
ET
BT
/F1 0010 Tf
69.2500 664.7040 Td
( Lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt
 ut labore ) Tj
ET
BT
/F1 0010 Tf
69.2500 652.7520 Td
( et dolore magna aliq ua. Ut enim ad minim veniam quis nostrud exercitation ullamco laboris
 nisi. ) Tj
ET
endstream
endobj

6 0 obj
<<
/Type /Page
/Parent 3 0 R
/Resources <<
/Font <<
/F1 9 0 R 
>>
/ProcSet 8 0 R
>>
/MediaBox [0 0 612.0000 792.0000]
/Contents 7 0 R
>>
endobj

7 0 obj
<< /Length 676 >>
stream
2 J
BT
0 0 0 rg
/F1 0027 Tf
57.3750 722.2800 Td
( Customer ID: ${request.parameters.id[0]}) Tj
ET
BT
/F1 0010 Tf
69.2500 688.6080 Td
( Date: ${request.parameters.date[0]} ) Tj
ET
BT
/F1 0010 Tf
69.2500 664.7040 Td
( More text... ) Tj
ET
endstream
endobj

8 0 obj
[/PDF /Text]
endobj

9 0 obj
<<
/Type /Font
/Subtype /Type1
/Name /F1
/BaseFont /Helvetica
/Encoding /WinAnsiEncoding
>>
endobj

10 0 obj
<<
/Creator (DeepfakeHTTP \(https://github.com/xnbox/DeepfakeHTTP))
/Producer (DeepfakeHTTP)
/CreationDate (D:20210925043107)
>>
endobj

xref
0 11
0000000000 65535 f
0000000019 00000 n
0000000093 00000 n
0000000147 00000 n
0000000222 00000 n
0000000390 00000 n
0000001522 00000 n
0000001690 00000 n
0000002423 00000 n
0000002456 00000 n
0000002574 00000 n

trailer
<<
/Size 11
/Root 1 0 R
/Info 10 0 R
>>

startxref
2714
%%EOF
.
```
</details>
