<h1>DeepfakeHTTP – Your 100% static dynamic backend</h1>


[![License MIT](https://img.shields.io/badge/license-MIT-blue?style=flat-square)](https://github.com/xnbox/DeepfakeHTTP/blob/master/LICENSE)
[![Version 1.0.5](https://img.shields.io/badge/version-1.0.5-4DC71F?style=flat-square)](https://github.com/xnbox/DeepfakeHTTP/releases)
[![Powered by Tommy](https://img.shields.io/badge/powered_by-Tommy-blueviolet?style=flat-square)](https://github.com/xnbox/tommy)

<br>
<table>
<tr>
<td>
<img src="image.png" height="170rem">
</td>
<td valign="top">
<strong>DeepfakeHTTP</strong> is a web server that uses HTTP dumps as a source for responses.<br><br>
<strong>What are people using it for?</strong>
<ul>
    <li>Creating the product <abbr title="Proof Of Concept">POC</abbr> or demo before even starting out with the backend</li>
    <li>REST, GraphQL, and other APIs prototyping and testing</li>
    <li>Hiding critical enterprise infrastructure behind a simple static facade</li>
    <li>Hacking and fine-tuning HTTP communications on both server and client sides</li>
</ul>
</td>
</tr>
</table>

<h2>Features</h2>
<ul>
    <li>No dependencies</li>
    <li>No installation</li>
    <li>No configuration files</li>
    <li>Single-file executable</li>
</ul>

<h2>Command Line Interface (CLI)</h2>

```text
java -jar df.jar [options] [dump1.txt] [dump2.txt] ...

Options:
        --help           print help message
        --info           print dump files statistics as JSON
        --requests       print dump requests as JSON
        --port           TCP port number, default: 8080
        --collect <file> append live request/response dumps to file
        --no-log         disable request/response console logging
        --no-etag        disable ETag optimization
        --no-watch       disable watch dump files for changes
```

<h2>Prerequisites</h2>
<ul>
	<li>Java 15 or above</li>
</ul>

<h2>Get started</h2>

<ol>
	<li>Download the <a href="https://github.com/xnbox/DeepfakeHTTP/releases/latest">latest release</a> of <code>df.jar</code></li>
	<li>Copy-paste the content of the dump example to the file <code>dump-sample.txt</code>:

```text
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

```text
java -jar df.jar dump-sample.txt
```
</li>
	<li>Use a browser to check whether the server is running: <a href="http://localhost:8080/api/customer/123">http://localhost:8080/api/customer/123</a>
<img src="get-started.png">
</li>
</ol>
For more examples see: <a href="#appendix-bdump-examples">APPENDIX B.</a>
<br><br>

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

<h2>Supports:</h2>

<ul>
    <li>unlimited number of request/response pairs in the dump</li>
    <li>asynchronous requests and responses</li>
    <li>scriptable response body</li>
    <li>all HTTP methods</li>
    <li>multi-line and multi-value headers</li>
    <li>OpenAPI-styled templates in paths</li>
    <li>wildcards ( <code> *</code> and <code> ?</code> with escape <code> /</code> ) in query string and header values</li>
    <li>templates in response body</li>
    <li>response body fetching from external sources like URLs, local files, and data URI</li>
    <li>per entry user-defined request and response delays</li>
    <li>comments in dumps</li>
	<li>live request/response collection</li>
    <li>watching dump files for changes</li>
    <li>ETag optimization</li>
</ul>

<h2>License</h2>
The <strong>DeepfakeHTTP</strong> is released under the <a href="https://github.com/xnbox/DeepfakeHTTP/blob/main/LICENSE">MIT</a> license.
<br><br><br>
<h1></h1>
<br><br>

<h2>
APPENDIX A.
<br>
Optional response headers
</h2>
<table>
	<tr><th width="21%" >Header</th>                                <th>Description</th></tr>
	<tr></tr>
	<tr><td valign="top"><code>X-Body-Type     </code></td>
	<td>
	<p>Tells the server what the content type (media type) of the body content actually is. Value of this header has same rules as value of standard HTTP <code>Content-Type</code> header.</p>
	<p>This header is useful when you want to use binary data, template or script as a response body.</p>
	<i>Examples:</i>
<br><br>

📎 A response body is a character data (default).<br>
No <code>X-Body-Type</code> header is needed.

```text
HTTP/1.1 200 OK
Content-Type: application/json

{"id": 5, "name": "John Doe"}
```

<h2></h2>

📎 Get a response body from a remote server.<br>
Body type is <code>text/uri-list</code> (RFC 2483)

```text
HTTP/1.1 200 OK
Content-Type: application/json
X-Body-Type: text/uri-list

http://example.com/api/car/1234.json
```

<h2></h2>

📎 Get a response body from a file.<br>
Body type is <code>text/uri-list</code> (RFC 2483)

```text
HTTP/1.1 200 OK
Content-Type: image/jpeg
X-Body-Type: text/uri-list

file:///home/john/photo.jpeg
```

<h2></h2>

📎 Get a response body from a data URI.<br>
Body type is <code>text/uri-list</code> (RFC 2483)

```text
HTTP/1.1 200 OK
Content-Type: image/gif
X-Body-Type: text/uri-list

data:image/gif;base64,R0lGODlhAQABAIAAAP...
```

<h2></h2>

📎 Get a response body from a template.<br>
Body type is <code>text/template</code>. Useful for forms processing.

```text
HTTP/1.1 200 OK
Content-Type: text/html
X-Body-Type: text/template

<!DOCTYPE html>
<html lang="en">
    <body>
        <h1>Hello, ${fname[0]} ${lname[0]}!</h1>
    </body>
</html>
```

</td></tr>
<tr></tr>
	<tr><td valign="top"><code>X-Request-Delay</code></td>
	<td><p>Request delay (in milliseconds).</p>
	<i>Example:</i>
	<br>

```text
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

```text
# Two seconds response delay.

HTTP/1.1 200 OK
X-Response-Delay: 2000

{"id": 5, "name": "John Doe"}
```

</td></tr>
</table>
<strong>NOTE:&nbsp;&nbsp;</strong>Optional response headers will <strong>not</strong> be sent to clients.
<br><br>

<h2>
APPENDIX B.
<br>
Dump examples
</h2>
<br>
<h3>Example 1.</h3>

```text
# Comments are welcome! :)
# Please don't miss a single carriage return between headers and body!

GET /form.html HTTP/1.1

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


POST /add_user.php HTTP/1.1
Content-Type: application/x-www-form-urlencoded

# Fake PHP file :)
HTTP/1.1 200 OK
Content-Type: text/html
X-Body-Type: text/template

<!DOCTYPE html>
<html lang="en">
<body>
    <h1>Hello, ${fname[0]} ${lname[0]}!</h1>
</body>
</html>
```
<br>
<h3>Example 2.</h3>

```text
#
# First request-response entry
#

# Client request
GET /api/customer/5 HTTP/1.1
Accept-Language: ru;*

# Server response
HTTP/1.1 200 OK
Content-Type: application/json

{
    "id": 5,
    "name": "Джон Доу"
}


#
# Second request-response entry
#

# Client request
GET /api/customer/5 HTTP/1.1

# Server response
HTTP/1.1 200 OK
Content-Type: application/json

{
    "id": 5,
    "name": "John Doe"
}
```
<br>
<h3>Example 3.</h3>

```text
#
# Work with HTML forms (1)
#

GET /form.html HTTP/1.1

HTTP/1.1 200 OK

<!DOCTYPE html>
<html lang="en">
<body>
    <form action="/action_page.php" method="POST">
        <label for="fname">First name:</label><input type="text" name="fname"><br><br>
        <label for="lname">Last name: </label><input type="text" name="lname"><br><br>
        <input type="submit" value="Submit">
    </form>
</body>
</html>


POST /action_page.php HTTP/1.1
Content-Type: application/x-www-form-urlencoded

HTTP/1.1 200 OK
Content-Type: text/html
X-Body-Type: text/template

<!DOCTYPE html>
<html lang="en">
<body>
    <h1>Hello, ${fname[0]} ${lname[0]}!</h1>
</body>
</html>
```
<br>
<h3>Example 4.</h3>

```text
#
# Work with HTML forms (2)
#

GET /form.html HTTP/1.1

HTTP/1.1 200 OK

<!DOCTYPE html>
<html lang="en">
<body>
    <form action="/action_page.php" method="POST">
        <label for="fname">First name:</label><input type="text" name="fname"><br><br>
        <label for="lname">Last name: </label><input type="text" name="lname"><br><br>
        <p>Only first name 'John' and last name 'Doe' are supported.<br>
        Expected output is: Hello, John Doe!,<br>
        or HTTP status 400 Bad request if first name is not 'John' or last name is not 'Doe'.
        </p><br><br>
        <input type="submit" value="Submit">
    </form>
</body>
</html>


POST /action_page.php HTTP/1.1
Content-Type: application/x-www-form-urlencoded

fname=John&lname=Doe

HTTP/1.1 200 OK
Content-Type: text/html
X-Body-Type: text/template

<!DOCTYPE html>
<body>
    <h1>Hello, ${fname[0]} ${lname[0]}!</h1>
</body>
</html>
```
