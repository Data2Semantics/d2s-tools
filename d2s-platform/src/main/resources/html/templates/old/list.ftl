<div class="list-section js-tabs">
	
	<ul>
		<li><a href="#tabs-1">Summary</a></li>
		<li><a href="#tabs-2">Full data (raw)</a></li>
		<li><a href="#tabs-3">Full data (histogram)</a></li>
	</ul>		
			
	<div id="tabs-1">
		<table>
			<#if is_numeric>
				<tr>
					<th>mean</th><td>${mean}</td>
				</tr>
				<tr>
					<th>std dev</th><td>${dev}</td>
				</tr>
				<tr>
					<th>median</th><td>${median}</td>
				</tr>
				<tr>
					<th>mode</th><td>${mode}</td>
				</tr>
			</#if>
		</table>
		
		<#if is_numeric>
		
			<h4>progression</h4>
		
			<div class="rs-line chart-container" data-source="raw-data-${id}" data-use-index="true">
			</div>
		
			<h4>histogram</h4>
			
			<div class="rs-histogram chart-container" data-source="histogram-data-${id}" data-use-index="true">
			</div>
			
		</#if>
			
	</div>
		
	<div id="tabs-2">
		<table class="raw-data raw-data-${id}">
		<#list list as item>
			<tr><td>${item}</td></tr>
		</#list>
		</table>
	</div>
	
	<div id="tabs-3">
		
		<table class="histogram-data histogram-data-${id}">
		<tr><th>token</th><th>frequency</th></tr>
		<#list histogram as pair>
			<tr><td>${pair[0]}</td><td>${pair[1]}</td></tr>
		</#list>
		</table>
	</div>
	
</div>