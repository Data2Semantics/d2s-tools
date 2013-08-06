<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>

  <link type="text/css" rel="stylesheet" media="screen" href="./css/jquery-ui/jquery-ui-1.8.21.custom.css" />
  <link type="text/css" rel="stylesheet" media="all" href="./css/common.lilian.css" />
  <link type="text/css" rel="stylesheet" media="screen" href="./css/screen.lilian.css" />
  <link type="text/css" rel="stylesheet" href="http://peterbloem.nl/jqplot/jquery.jqplot.min.css" />
  
  
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js" type="text/javascript"></script>
  <script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/jquery-ui.min.js" type="text/javascript"></script>
  
  <script src="http://peterbloem.nl/jqplot/jquery.jqplot.min.js" type="text/javascript"></script>
  <script src="http://peterbloem.nl/jqplot/plugins/jqplot.logAxisRenderer.js" type="text/javascript" ></script>
  <script src="http://peterbloem.nl/jqplot/plugins/jqplot.barRenderer.min.js" type="text/javascript" ></script>  
  
  <script src="./javascript/base.lilian.js" type="text/javascript"></script>  
  
  
  <title>Lilian results</title>
</head>
<body class="${tags}">
  <h1>Report: ${short_name}</h1>
  <h2>Run information</h2>
  <p class="explanation">
  	Basic information about this run of the experiment.
  </p>
  
  <dl>
  	<dt>
  		Experiment name
  	</dt>
  	<dd>
  		${name}
  	</dd>
  	
    <dt>
  		description
  	</dt>
  	<dd>
  		${description}
  	</dd>
  	<dt>
  		Start date/time
  	</dt>  	
  	<dd>
  		${start_date_time} (${start_millis})
  	</dd>
  	<dt>
  		End date/time
  	</dt>  	
  	<dd>
  		${end_date_time} (${end_millis})
  	</dd>
  </dl>
  
  <h2>Results</h2>
  <p class="explanation">
    The relevant results of the run, collated and analysed.
  </p>
  
  <#list results as result>
    <h3>Result: ${result.name}</h3>
    <p class="description">
    	${result.description}
    </p>
    <div class="value">
    	${result.value}
    </div>
  </#list>
  
  <h2>Reportables</h2>
   <p class="explanation">
	Extensive information about the state of all software in the classpath at 
	the time of the run
  </p> 
  
</body>
</html>  