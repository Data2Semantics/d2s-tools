<div class="rs-line chart-container" data-source=" data-${id}" data-use-index="false">
</div>


<table class="data-${id}">
  <tr>
	<#list row_headers as header>
		<th>${header}</th>
	</#list>
  </tr>
  <#list rows as row>
	<tr>
		<#list row.parameters as parameter>
			<td class="parameter">${parameter}</td>
		</#list>
		<#list row.values as value>
			<td class="value">${value}</td>
		</#list>
	</tr>  
  </#list>
</table>