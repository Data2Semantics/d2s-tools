<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>

  <link type="text/css" rel="stylesheet" media="screen" href="../css/jquery-ui/jquery-ui-1.8.21.custom.css" />
  <link type="text/css" rel="stylesheet" media="all" href="../css/common.platform.css" />
  <link type="text/css" rel="stylesheet" media="screen" href="../css/screen.platform.css" />
  <link type="text/css" rel="stylesheet" href="http://peterbloem.nl/jqplot/jquery.jqplot.min.css" />
  
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js" type="text/javascript"></script>
  <script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/jquery-ui.min.js" type="text/javascript"></script>
  
  <script src="http://peterbloem.nl/jqplot/jquery.jqplot.min.js" type="text/javascript"></script>
  <script src="http://peterbloem.nl/jqplot/plugins/jqplot.logAxisRenderer.js" type="text/javascript" ></script>
  <script src="http://peterbloem.nl/jqplot/plugins/jqplot.barRenderer.min.js" type="text/javascript" ></script>  
  
  <script src="../javascript/base.platform.js" type="text/javascript"></script>  
  
  
  <title>Instance results</title>
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
	  		<th>inputs</th><th>instance<th>
	  	</tr>
	  <#list instances as instance>
	  	<tr>
	  		<td>${instance.input_string}</td><td><a href="${instance.url}" target="This instance's report">report</a></td>
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
  
  TODO
  
  <h2>Inputs</h2>
  <p class="explanation">
    This module's inputs
  </p>
  
  TODO
  
  </#list>
  
</body>
</html>  