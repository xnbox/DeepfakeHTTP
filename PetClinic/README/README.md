<h1>REST version of the Spring PetClinic built with DeepfakeHTTP</h1>
<p id="start" align="center">
<br>
<table align="center"><tr><td><a href="#start"><img width="250rem" src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/PetClinic/README/pets.png"></a></td></tr></table>
</p>

<h2>About Spring PetClinic</h2>
The Spring PetClinic is an example application designed to show how a particular stack can be used to create simple but powerful database-oriented applications.

<h2>Get started</h2>

<ol>
    <li>Download the <a href="https://github.com/xnbox/DeepfakeHTTP/releases/latest">latest releases</a> of <code>df.jar</code> and <code>df-spring-petclinic.zip</code></li>
    <li>Unzip the <code>df-spring-petclinic.zip</code> archive</li>
<li>Start the server from command line:

<pre>
java -jar df.jar \
--dump <a href="https://github.com/xnbox/DeepfakeHTTP/blob/main/PetClinic/dump.txt">PetClinic/dump.txt</a> \
--js <a href="https://github.com/xnbox/DeepfakeHTTP/blob/main/PetClinic/context.js">PetClinic/context.js</a> \
--dir <a href="https://github.com/xnbox/DeepfakeHTTP/tree/main/PetClinic">PetClinic</a> \
--openapi-title 'Spring Pet Clinic API v1.2.7' \
--db <a href="https://github.com/xnbox/DeepfakeHTTP/blob/main/PetClinic/db.json">PetClinic/db.json</a> \
--openapi-path <a href="#api">/api</a> \
--db-path <a href="#db">/db</a>
</pre>

</li>

<li>Use a browser to check whether the server is running:
<br>
<pre><a href="http://localhost:8080">http://localhost:8080</a></pre>
<table><tr><td>
<img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/PetClinic/README/petclinic-screenshots/1.png">
</td></tr></table>
</li>
</li>

<li><strong>Bonus</strong>: Self-hosted OpenAPI client
<br>
<pre id="api"><a href="http://localhost:8080/api">http://localhost:8080/api</a></pre>
<table><tr><td>
<img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/PetClinic/README/petclinic-screenshots/2.png">
</td></tr></table>
</li>

<li><strong>Bonus</strong>: <code>openapi.json</code>
<br>
<pre><a href="http://localhost:8080/api/openapi.json">http://localhost:8080/api/openapi.json</a></pre>
<table><tr><td>
<img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/PetClinic/README/petclinic-screenshots/3.png">
</td></tr></table>
</li>

<li><strong>Bonus</strong>: <code>openapi.yaml</code>
<br>
<pre><a href="http://localhost:8080/api/openapi.yaml">http://localhost:8080/api/openapi.yaml</a></pre>
<table><tr><td>
<img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/PetClinic/README/petclinic-screenshots/4.png">
</td></tr></table>
</li>

<li><strong>Bonus</strong>: Live data dump
<br>
<pre id="db"><a href="http://localhost:8080/db">http://localhost:8080/db</a></pre>
<table><tr><td>
<img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/PetClinic/README/petclinic-screenshots/5.png">
</td></tr></table>
</li>

</ol>

<p align="right"><a href="#start"><img width="45rem" src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/top.png"></a></p>
