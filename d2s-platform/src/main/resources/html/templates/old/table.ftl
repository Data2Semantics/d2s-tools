<div class="list-section js-tabs">
	
	<ul>
		<li><a href="#tabs-1">Summary</a></li>
		<li><a href="#tabs-2">Full data (raw)</a></li>
	</ul>		
			
	<div id="tabs-1">
		<h4>Tabular data</h4>
	
		<table>
				<tr>
					<th>height</th><td>${height}</td>
				</tr>
				<tr>
					<th>width</th><td>${width}</td>
				</tr>
		</table>
		
		
		<div class="rs-line chart-container ${plottype}" data-source="raw-data-${id}" data-use-index="false"> </div>
	</div>
		
	<div id="tabs-2">
		<table class="raw-data raw-data-${id}">
		
		<#list table as row>
			<tr>
			<#list row as cell>
			  <td><#if cell?is_number >${cell?c}<#else>${cell}</#if></td>
			</#list>
			</tr>
		</#list>
		</table>
	</div>
	
</div>