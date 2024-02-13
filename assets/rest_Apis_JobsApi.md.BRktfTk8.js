import{_ as a,o,c as r,m as t,a as e,V as s}from"./chunks/framework.JWZrZsxi.js";const J=JSON.parse('{"title":"JobsApi","description":"","frontmatter":{},"headers":[],"relativePath":"rest/Apis/JobsApi.md","filePath":"rest/Apis/JobsApi.md","lastUpdated":1707862450000}'),n={name:"rest/Apis/JobsApi.md"},h=t("h1",{id:"jobsapi",tabindex:"-1"},[e("JobsApi "),t("a",{class:"header-anchor",href:"#jobsapi","aria-label":'Permalink to "JobsApi"'},"​")],-1),l=t("p",null,[e("All URIs are relative to "),t("em",null,[t("a",{href:"http://localhost",target:"_blank",rel:"noreferrer"},"http://localhost")])],-1),i=t("table",null,[t("thead",null,[t("tr",null,[t("th",null,"Method"),t("th",null,"HTTP request"),t("th",null,"Description")])]),t("tbody",null,[t("tr",null,[t("td",null,[t("a",{href:"./JobsApi.html#deleteJob"},[t("strong",null,"deleteJob")])]),t("td",{jobName:""},[t("strong",null,"DELETE"),e(" /system/jobs/{jobGroup}/")]),t("td",null,"Delete Job")]),t("tr",null,[t("td",null,[t("a",{href:"./JobsApi.html#executeJob"},[t("strong",null,"executeJob")])]),t("td",{jobName:""},[t("strong",null,"POST"),e(" /system/jobs/{jobGroup}/")]),t("td",null,"Execute Job")]),t("tr",null,[t("td",null,[t("a",{href:"./JobsApi.html#jobsList"},[t("strong",null,"jobsList")])]),t("td",null,[t("strong",null,"GET"),e(" /system/jobs")]),t("td",null,"Registered Jobs")]),t("tr",null,[t("td",null,[t("a",{href:"./JobsApi.html#possibleJobsList"},[t("strong",null,"possibleJobsList")])]),t("td",null,[t("strong",null,"GET"),e(" /system/jobs/classes")]),t("td",null,"Possible Jobs")]),t("tr",null,[t("td",null,[t("a",{href:"./JobsApi.html#registerJob"},[t("strong",null,"registerJob")])]),t("td",null,[t("strong",null,"PUT"),e(" /system/jobs")]),t("td",null,"Register Job")]),t("tr",null,[t("td",null,[t("a",{href:"./JobsApi.html#updateJob"},[t("strong",null,"updateJob")])]),t("td",{jobName:""},[t("strong",null,"PATCH"),e(" /system/jobs/{jobGroup}/")]),t("td",null,"Update Job")])])],-1),d=s(`<p><a name="deleteJob"></a></p><h1 id="deletejob" tabindex="-1"><strong>deleteJob</strong> <a class="header-anchor" href="#deletejob" aria-label="Permalink to &quot;**deleteJob**&quot;">​</a></h1><blockquote><p>JsonValue_Boolean deleteJob(jobGroup, jobName)</p></blockquote><p>Delete Job</p><pre><code>Delete Job and reload all Job Information
</code></pre><h3 id="parameters" tabindex="-1">Parameters <a class="header-anchor" href="#parameters" aria-label="Permalink to &quot;Parameters&quot;">​</a></h3><table><thead><tr><th>Name</th><th>Type</th><th>Description</th><th>Notes</th></tr></thead><tbody><tr><td><strong>jobGroup</strong></td><td><strong>String</strong></td><td>Group Name of the Job</td><td>[default to Default]</td></tr><tr><td><strong>jobName</strong></td><td><strong>String</strong></td><td>Name of the Job</td><td>[default to null]</td></tr></tbody></table><h3 id="return-type" tabindex="-1">Return type <a class="header-anchor" href="#return-type" aria-label="Permalink to &quot;Return type&quot;">​</a></h3><p><a href="./../Models/JsonValue_Boolean.html"><strong>JsonValue_Boolean</strong></a></p><h3 id="authorization" tabindex="-1">Authorization <a class="header-anchor" href="#authorization" aria-label="Permalink to &quot;Authorization&quot;">​</a></h3><p><a href="./../README.html#httpAuth1">httpAuth1</a>, <a href="./../README.html#httpAuth">httpAuth</a>, <a href="./../README.html#apiKeyAuth">apiKeyAuth</a></p><h3 id="http-request-headers" tabindex="-1">HTTP request headers <a class="header-anchor" href="#http-request-headers" aria-label="Permalink to &quot;HTTP request headers&quot;">​</a></h3><ul><li><strong>Content-Type</strong>: Not defined</li><li><strong>Accept</strong>: application/json</li></ul><p><a name="executeJob"></a></p><h1 id="executejob" tabindex="-1"><strong>executeJob</strong> <a class="header-anchor" href="#executejob" aria-label="Permalink to &quot;**executeJob**&quot;">​</a></h1><blockquote><p>JsonValue_Boolean executeJob(jobGroup, jobName)</p></blockquote><p>Execute Job</p><pre><code>Execute scheduled Job manually
</code></pre><h3 id="parameters-1" tabindex="-1">Parameters <a class="header-anchor" href="#parameters-1" aria-label="Permalink to &quot;Parameters&quot;">​</a></h3><table><thead><tr><th>Name</th><th>Type</th><th>Description</th><th>Notes</th></tr></thead><tbody><tr><td><strong>jobGroup</strong></td><td><strong>String</strong></td><td>Group Name of the Job</td><td>[default to Default]</td></tr><tr><td><strong>jobName</strong></td><td><strong>String</strong></td><td>Name of the Job</td><td>[default to null]</td></tr></tbody></table><h3 id="return-type-1" tabindex="-1">Return type <a class="header-anchor" href="#return-type-1" aria-label="Permalink to &quot;Return type&quot;">​</a></h3><p><a href="./../Models/JsonValue_Boolean.html"><strong>JsonValue_Boolean</strong></a></p><h3 id="authorization-1" tabindex="-1">Authorization <a class="header-anchor" href="#authorization-1" aria-label="Permalink to &quot;Authorization&quot;">​</a></h3><p><a href="./../README.html#httpAuth1">httpAuth1</a>, <a href="./../README.html#httpAuth">httpAuth</a>, <a href="./../README.html#apiKeyAuth">apiKeyAuth</a></p><h3 id="http-request-headers-1" tabindex="-1">HTTP request headers <a class="header-anchor" href="#http-request-headers-1" aria-label="Permalink to &quot;HTTP request headers&quot;">​</a></h3><ul><li><strong>Content-Type</strong>: Not defined</li><li><strong>Accept</strong>: application/json</li></ul><p><a name="jobsList"></a></p><h1 id="jobslist" tabindex="-1"><strong>jobsList</strong> <a class="header-anchor" href="#jobslist" aria-label="Permalink to &quot;**jobsList**&quot;">​</a></h1><blockquote><p>List jobsList()</p></blockquote><p>Registered Jobs</p><pre><code>Returns the List of all registered Jobs with full information
</code></pre><h3 id="parameters-2" tabindex="-1">Parameters <a class="header-anchor" href="#parameters-2" aria-label="Permalink to &quot;Parameters&quot;">​</a></h3><p>This endpoint does not need any parameter.</p><h3 id="return-type-2" tabindex="-1">Return type <a class="header-anchor" href="#return-type-2" aria-label="Permalink to &quot;Return type&quot;">​</a></h3><p><a href="./../Models/JobInformation.html"><strong>List</strong></a></p><h3 id="authorization-2" tabindex="-1">Authorization <a class="header-anchor" href="#authorization-2" aria-label="Permalink to &quot;Authorization&quot;">​</a></h3><p><a href="./../README.html#httpAuth1">httpAuth1</a>, <a href="./../README.html#httpAuth">httpAuth</a>, <a href="./../README.html#apiKeyAuth">apiKeyAuth</a></p><h3 id="http-request-headers-2" tabindex="-1">HTTP request headers <a class="header-anchor" href="#http-request-headers-2" aria-label="Permalink to &quot;HTTP request headers&quot;">​</a></h3><ul><li><strong>Content-Type</strong>: Not defined</li><li><strong>Accept</strong>: application/json</li></ul><p><a name="possibleJobsList"></a></p><h1 id="possiblejobslist" tabindex="-1"><strong>possibleJobsList</strong> <a class="header-anchor" href="#possiblejobslist" aria-label="Permalink to &quot;**possibleJobsList**&quot;">​</a></h1><blockquote><p>List possibleJobsList()</p></blockquote><p>Possible Jobs</p><pre><code>Returns the List of possible job classes
</code></pre><h3 id="parameters-3" tabindex="-1">Parameters <a class="header-anchor" href="#parameters-3" aria-label="Permalink to &quot;Parameters&quot;">​</a></h3><p>This endpoint does not need any parameter.</p><h3 id="return-type-3" tabindex="-1">Return type <a class="header-anchor" href="#return-type-3" aria-label="Permalink to &quot;Return type&quot;">​</a></h3><p><strong>List</strong></p><h3 id="authorization-3" tabindex="-1">Authorization <a class="header-anchor" href="#authorization-3" aria-label="Permalink to &quot;Authorization&quot;">​</a></h3><p><a href="./../README.html#httpAuth1">httpAuth1</a>, <a href="./../README.html#httpAuth">httpAuth</a>, <a href="./../README.html#apiKeyAuth">apiKeyAuth</a></p><h3 id="http-request-headers-3" tabindex="-1">HTTP request headers <a class="header-anchor" href="#http-request-headers-3" aria-label="Permalink to &quot;HTTP request headers&quot;">​</a></h3><ul><li><strong>Content-Type</strong>: Not defined</li><li><strong>Accept</strong>: application/json</li></ul><p><a name="registerJob"></a></p><h1 id="registerjob" tabindex="-1"><strong>registerJob</strong> <a class="header-anchor" href="#registerjob" aria-label="Permalink to &quot;**registerJob**&quot;">​</a></h1><blockquote><p>JobInformation registerJob(JobConfig)</p></blockquote><p>Register Job</p><pre><code>Register an Job and return the JobInformation with next schedule information
</code></pre><h3 id="parameters-4" tabindex="-1">Parameters <a class="header-anchor" href="#parameters-4" aria-label="Permalink to &quot;Parameters&quot;">​</a></h3><table><thead><tr><th>Name</th><th>Type</th><th>Description</th><th>Notes</th></tr></thead><tbody><tr><td><strong>JobConfig</strong></td><td><a href="./../Models/JobConfig.html"><strong>JobConfig</strong></a></td><td></td><td></td></tr></tbody></table><h3 id="return-type-4" tabindex="-1">Return type <a class="header-anchor" href="#return-type-4" aria-label="Permalink to &quot;Return type&quot;">​</a></h3><p><a href="./../Models/JobInformation.html"><strong>JobInformation</strong></a></p><h3 id="authorization-4" tabindex="-1">Authorization <a class="header-anchor" href="#authorization-4" aria-label="Permalink to &quot;Authorization&quot;">​</a></h3><p><a href="./../README.html#httpAuth1">httpAuth1</a>, <a href="./../README.html#httpAuth">httpAuth</a>, <a href="./../README.html#apiKeyAuth">apiKeyAuth</a></p><h3 id="http-request-headers-4" tabindex="-1">HTTP request headers <a class="header-anchor" href="#http-request-headers-4" aria-label="Permalink to &quot;HTTP request headers&quot;">​</a></h3><ul><li><strong>Content-Type</strong>: application/json</li><li><strong>Accept</strong>: application/json, text/plain</li></ul><p><a name="updateJob"></a></p><h1 id="updatejob" tabindex="-1"><strong>updateJob</strong> <a class="header-anchor" href="#updatejob" aria-label="Permalink to &quot;**updateJob**&quot;">​</a></h1><blockquote><p>JobInformation updateJob(jobGroup, jobName, JobConfig)</p></blockquote><p>Update Job</p><pre><code>Add Job and get JobInformation back
</code></pre><h3 id="parameters-5" tabindex="-1">Parameters <a class="header-anchor" href="#parameters-5" aria-label="Permalink to &quot;Parameters&quot;">​</a></h3><table><thead><tr><th>Name</th><th>Type</th><th>Description</th><th>Notes</th></tr></thead><tbody><tr><td><strong>jobGroup</strong></td><td><strong>String</strong></td><td>Group Name of the Job</td><td>[default to Default]</td></tr><tr><td><strong>jobName</strong></td><td><strong>String</strong></td><td>Name of the Job</td><td>[default to null]</td></tr><tr><td><strong>JobConfig</strong></td><td><a href="./../Models/JobConfig.html"><strong>JobConfig</strong></a></td><td></td><td></td></tr></tbody></table><h3 id="return-type-5" tabindex="-1">Return type <a class="header-anchor" href="#return-type-5" aria-label="Permalink to &quot;Return type&quot;">​</a></h3><p><a href="./../Models/JobInformation.html"><strong>JobInformation</strong></a></p><h3 id="authorization-5" tabindex="-1">Authorization <a class="header-anchor" href="#authorization-5" aria-label="Permalink to &quot;Authorization&quot;">​</a></h3><p><a href="./../README.html#httpAuth1">httpAuth1</a>, <a href="./../README.html#httpAuth">httpAuth</a>, <a href="./../README.html#apiKeyAuth">apiKeyAuth</a></p><h3 id="http-request-headers-5" tabindex="-1">HTTP request headers <a class="header-anchor" href="#http-request-headers-5" aria-label="Permalink to &quot;HTTP request headers&quot;">​</a></h3><ul><li><strong>Content-Type</strong>: application/json</li><li><strong>Accept</strong>: application/json, text/plain</li></ul>`,78),u=[h,l,i,d];function p(b,c,m,g,f,q){return o(),r("div",null,u)}const P=a(n,[["render",p]]);export{J as __pageData,P as default};
