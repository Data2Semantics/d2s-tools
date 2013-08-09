<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>

  <link type="text/css" rel="stylesheet" media="screen" href="../../css/jquery-ui/jquery-ui-1.8.21.custom.css" />
  <link type="text/css" rel="stylesheet" media="all" href="../../css/common.platform.css" />
  <link type="text/css" rel="stylesheet" media="screen" href="../../css/screen.platform.css" />
  <link type="text/css" rel="stylesheet" href="http://peterbloem.nl/jqplot/jquery.jqplot.min.css" />
  
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js" type="text/javascript"></script>
  <script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/jquery-ui.min.js" type="text/javascript"></script>
  
  <script src="http://peterbloem.nl/jqplot/jquery.jqplot.min.js" type="text/javascript"></script>
  <script src="http://peterbloem.nl/jqplot/plugins/jqplot.logAxisRenderer.js" type="text/javascript" ></script>
  <script src="http://peterbloem.nl/jqplot/plugins/jqplot.canvasTextRenderer.min.js" type="text/javascript" ></script> 
  <script src="http://peterbloem.nl/jqplot/plugins/jqplot.canvasAxisLabelRenderer.min.js" type="text/javascript" ></script>
  <script src="http://peterbloem.nl/jqplot/plugins/jqplot.barRenderer.min.js" type="text/javascript" ></script>  
  
  <script src="../../javascript/base.platform.js" type="text/javascript"></script>  
  
  
  <title>Module results</title>
</head>
<body class="${tags}">
  <h1>Report: ${name}</h1>
  <h2>Module information</h2>
  
  <dl>
  	<dt>
  		Module
  	</dt>
  	<dd>
  		${name}
  	</dd>
  </dl>
  
  <h2>Instances</h2>
  <p class="explanation">
  	This module's instances, and their results pages.
  </p>
  <#if instantiated >
	  <table class="instances">
	  	<tr>
	  		<#list input_names as input_name>
	  		  <td>
	  		    ${input_name}
	  		  </td>
	  		</#list>

			<th>instance<th>
	  	</tr>
	  <#list instances as instance>
	  	<tr>
	  		<#list instance.inputs as input>
	  		  <td>
	  		    ${input}
	  		  </td>
	  		</#list>
	  		<td><a href="${instance.url}" target="This instance's report">report</a></td>
	  	</tr>
	  </#list>
	  </table>
  <#else>
    Module not yet instantiated.
  </#if>
  
  <h2>Outputs</h2>
  <p class="explanation">
    This module's outputs and its collected values
  </p>
  
  <#list outputs as output>
    <div class="section output">
    <h3>Result: <span class="output-name">${output.name}</span></h3>
    <p class="description">
    	${output.description}
    </p>
    
    <div class="output js-tabs">
	
		<ul>
			<li><a href="#tab-summary">Summary</a></li>
			<li><a href="#tab-full">Full data</a></li>
		</ul>	
    
    	<div id="tab-summary">
    	
    		<div class="graph chart-container" data-source="data-table-${output.name}">
			</div>
    	
	    	<table>
	    			<tr>
						<th>mode</th><td>${output.mode} (${output.mode_frequency})</td>
					</tr>
	    			<tr>
						<th>instances</th><td>${output.num_instances}</td>
					</tr>
				<#if output.is_numeric>
					<tr>
						<th>mean</th><td>${output.mean}</td>
					</tr>
					<tr>
						<th>std dev</th><td>${output.dev}</td>
					</tr>
					<tr>
						<th>median</th><td>${output.median}</td>
					</tr>
				</#if>

					<tr>
						<th>entropy</th><td>${output.entropy}</td>
					</tr>
			</table>
			
    	</div>
    	
    	<div id="tab-full"> 
			<table class="data-table-${output.name}">
				<tr class="names">
					<#list input_names as input_name>
						<th class="input">
							${input_name}
						</th>
					</#list>
					<th class="output">
						${output.name}
					</th>
				</tr>
				
				<#list output.instances as instance>
				<tr>
					<#list instance.inputs as input>
						<td class="input">
							${input}
						</td>
					</#list>
					<td class="output">
						${instance.output}
					</td>
				</tr>
				</#list>
			</table>
		</div>
	</div>
	</div>
  </#list>
  
  <h2>Inputs</h2>
  <p class="explanation">
    This module's inputs
  </p>
  
   <#list inputs as input>
     <div class="section input">
    <h3>In: ${input.name}</h3>
    <p class="description">
    	${input.description}
    </p>
    <ul class="values">
	    <#list input.values as value>
		    <li class="value">
		    	${value}
		    </li>
	    </#list>
    </ul>
    </div>
  </#list>
  
</body>
</html>  